using ArgParse
using Fjage
using FFTW
using LinearAlgebra
using LRUCache

include("maths.jl")
include("messages.jl")

function (@main)(plain_args)
    parser = ArgParseSettings(;
        prog = "./usbl",
        description = """
          Turn the specified multi-receiver modem into a USBL transceiver. 

          While running, this script will listen for `DetectionNtf` and publish
          corresponding `BearingNtf` to `topic("bearings")`.  In addition, if
          any ranging agent publishes a matching `RangeNtf` then this script
          will combine the bearing and range into a `PeerLocationNtf` published
          to `topic("locations")`.
          """,
        preformatted_description = true,
        epilog = """
          examples:
            ./usbl 192.168.42.123 geometry.txt
            ./usbl 192.168.42.123:1234 geometry.txt

          geometry file format:
            The geometry file must contain exactly four non-empty lines, each of
            which must contain exactly three whitespace-separated floating-point
            numbers. The first line specifies the (x, y, z) coordinates of the
            transducer, and the remaining three lines specify the (x, y, z)
            coordinates of the hydrophones. All coordinates must be given in meters.

          example geometry file:
            0 0 0
            1 0 0
            0 1 0
            0 0 1

          example usage from UnetStack shell
            > subscribe topic("bearings")
            > subscribe ranging
            > subscribe topic("locations")
            > range 2;
            usbl-script >> BearingNtf:INFORM[azimuth:5.322475759063779E-16 elevation:-5.051187579317586 rxStartTime:162525959]
            usbl-script >> BearingNtf:INFORM[azimuth:5.322475759063779E-16 elevation:-5.051187579317586 rxStartTime:164021959]
            ranging >> RangeNtf:INFORM[from:1 to:2 range:100.91175 offset:3610 rxStartTime:162525959]
            usbl-script >> PeerLocationNtf:INFORM[rxStartTime:162525959 peerLocation:100.519854,0,-8.884846 peer:2]
          """,
        preformatted_epilog = true,
    )
    @add_arg_table! parser begin
        "modem-address"
        help = "Address of the multi-receiver modem. May contain a port number separated by ':'."
        arg_type = String
        required = true

        "geometry-file"
        help = "File specifying the receiver geometry (see below)."
        arg_type = String
        required = true
    end

    parsed_args = parse_args(plain_args, parser)
    host, port = parse_host_string(parsed_args["modem-address"])
    x0, dx = parse_geometry_file(parsed_args["geometry-file"])
    run(; host, port, x0, dx)
    return 0
end

function parse_host_string(host::String)
    port = 1100
    if occursin(":", host)
        host, port_str = split(host, ":")
        try
            port = parse(Int, port_str)
        catch
            error("Invalid host address: expected the part after ':' to be a port number, found '$(port_str)'")
        end
    end
    return host, port
end

function parse_geometry_file(geometry_file::String)
    lines = try
        readlines(geometry_file)
    catch
        error("Could not read geometry file: $(geometry_file)")
    end
    filter!(!isempty, lines .= strip.(first.(split.(lines, "#"))))
    if length(lines) != 4
        error("""
              Invalid geometry file: expected 4 lines, \
              found $(length(lines)) lines in $(geometry_file)
              """)
    end
    receiver_positions = [
        begin
            parts = split(line, r"\s+")
            if length(parts) != 3
                error("""
                      Invalid geometry file: expected 3 whitespace-separated values per line, \
                      found $(length(parts)) values on line $(geometry_file):$(i)
                      """)
            end
            [
                try
                    parse.(Float64, part)
                catch
                    error("""
                          Invalid geometry file: could not parse '$(part)' as a number \
                          on line $(geometry_file):$(i)
                          """)
                end for part in parts
            ]
        end for (i, line) in enumerate(lines)
    ]
    x0 = receiver_positions[1]
    dx = hcat(receiver_positions[2] .- x0, receiver_positions[3] .- x0, receiver_positions[4] .- x0)
    return x0, dx
end

@kwdef struct CachedRange
    peer::Int32
    range::Float32
end

function run(; host, port, x0, dx)
    gw = Gateway("usbl-script", host, port)

    local bb
    while true
        bb = @something(
            agentforservice(gw, "org.arl.unet.Services.BASEBAND"),
            begin
                @warn("Could not look up BASEBAND agent. Trying again...")
                continue
            end,
        )
        break
    end

    local fs
    while true
        fs = @something( #
            bb.basebandRate,
            begin
                @warn("Could not fetch baseband sampling rate. Trying again...")
                continue
            end
        )
        break
    end

    local node_address
    while true
        node_address = @something(
            @something(
                agentforservice(gw, "org.arl.unet.Services.NODE_INFO"),
                begin
                    @warn("Could not find NODE_INFO agent. Trying again...")
                    continue
                end
            ).address,
            begin
                @warn("Could not fetch node address. Trying again...")
                continue
            end
        )
        break
    end

    max_dx = maximum(norm.(eachcol(dx)))
    min_sound_speed = 1000.0 # m/s
    extra_samples = ceil(Int, max_dx / min_sound_speed * fs)

    bearing_cache = LRU{Int64, Vector{Float64}}(; maxsize = 20)
    range_cache = LRU{Int64, CachedRange}(; maxsize = 20)

    subscribe(gw, bb)
    for ranging_agent in agentsforservice(gw, "org.arl.unet.Services.RANGING")
        subscribe(gw, ranging_agent)
    end

    @info "Ready!"
    while true
        ntf = receive(gw, msg -> msg isa Union{DetectionNtf, RangeNtf}, BLOCKING)
        if ntf isa DetectionNtf
            @debug "Received DetectionNtf"
            detection_ntf = ntf

            # Look up preamble
            plain_preamble = @something(
                bb[Int64(detection_ntf.preamble)].preamble,
                begin
                    @warn("""
                          Could not look up preamble for detection at $(detection_ntf.rxStartTime). \
                          I will not compute a bearing for this detection.
                          """)
                    continue
                end
            )
            preamble = reinterpret(ComplexF32, convert(Vector{Float32}, plain_preamble))

            # Look up recording
            recording_req = RecordBasebandSignalReq(;
                recStartTime = detection_ntf.rxStartTime - ceil(Int, extra_samples / fs),
                recLength = length(preamble) + 2 * extra_samples,
            )
            send(bb, recording_req)
            recording_ntf = @something(
                receive(gw, msg -> msg isa RxBasebandSignalNtf && msg.inReplyTo == recording_req.messageID, 5000),
                begin
                    @warn("""
                          Could not look up baseband recording for detection at $(detection_ntf.rxStartTime). \
                          I will not compute a bearing for this detection.
                          """)
                    continue
                end
            )
            if recording_ntf.channels != 4
                @warn("""
                      Expected 4-channel recording for detection at $(detection_ntf.rxStartTime), \
                      found $(recording_ntf.channels) channels. \
                      I will not compute a bearing for this detection.
                      """)
                continue
            end
            recording = transpose(reshape(recording_ntf.signal, (4, :)))

            # Compute and publish bearing
            u = arrival_vector(preamble, recording, dx)
            norm_u = norm(u)
            sound_speed = fs / norm_u
            u ./= norm_u
            if !(1200 <= sound_speed <= 1800)
                @warn("""
                      Bad sound speed: $(sound_speed) m/s for detection at $(detection_ntf.rxStartTime). \
                      I will not publish a bearing for this detection.
                      """)
                continue
            end
            azimuth, elevation = angles_from_vector(u)
            send(topic(gw, "bearings"), BearingNtf(; azimuth, elevation, rxStartTime = detection_ntf.rxStartTime))

            # Compute location if possible
            rxStartTime = detection_ntf.rxStartTime
            bearing_cache[detection_ntf.rxStartTime] = u
            maybe_cached_range = get(range_cache, rxStartTime, nothing)
            if !isnothing(maybe_cached_range)
                (; peer, range) = maybe_cached_range
                send(topic(gw, "locations"), PeerLocationNtf(; rxStartTime, peer, peerLocation = x0 + range * u))
            end
        end
        if ntf isa RangeNtf
            @debug "Received RangeNtf"
            (; rxStartTime, from, to, range) = ntf
            peer = to
            if from != node_address
                continue
            end
            range_cache[rxStartTime] = CachedRange(; peer, range)
            u = @something(get(bearing_cache, rxStartTime, nothing), continue)
            send(topic(gw, "locations"), PeerLocationNtf(; rxStartTime, peer, peerLocation = x0 + range * u))
        end
    end
end
