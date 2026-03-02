using ArgParse
using Fjage
using Sockets
using UnderwaterAcoustics
using VirtualAcousticOcean

include("messages.jl")

function (@main)(args)
    parser = ArgParseSettings(;
        prog = "./vao",
        description = """
          Launch the VirtualAcousticOcean simulator.
          """,
        preformatted_description = true,
        epilog = """
                Both IP addresses may contain a port number separated by ':'.

                example:
                  ./vao 192.168.42.123 192.168.42.45

                note: 
                  This script reboots the modems and permanently reconfigures
                  them to use the VirtualAcousticOcean simulator instead of
                  their onboard hardware. You can undo this configuration by
                  deleting the `modem.toml` files in their `scripts` folders and
                  rebooting the modems. 
                """,
        preformatted_epilog = true,
    )
    @add_arg_table! parser begin
        "transceiver-address"
        help = "Address of the transceiver modem"
        arg_type = String
        required = true

        "transponder-address"
        help = "Address of the transceiver modem"
        arg_type = String
        required = true
    end
    parsed_args = parse_args(args, parser)

    @info "Launching VirtualAcousticOcean simulator..."
    transceiver_vao_port = 9809
    transponder_vao_port = 9819
    launch_vao(transceiver_vao_port, transponder_vao_port)

    @info "Configuring modems..."
    configure_modems(
        (parse_host_string(parsed_args["transceiver-address"]), transceiver_vao_port),
        (parse_host_string(parsed_args["transponder-address"]), transponder_vao_port),
    )

    @info "Ready!"
    wait()
    return 0
end

function launch_vao(transceiver_vao_port, transponder_vao_port)
    modem_frequency = 24000.0  # nominal frequency in Hz
    transceiver_location = (0.0, 0.0, -10.0)  # x, y, z in meters
    transponder_location = (100.0, 0.0, -10.0)  # x, y, z in meters

    transceiver_offsets = [  # x, y, z in meters
        0.15 .* (0.0, 0.0, 0.0),
        0.15 .* (1 / sqrt(3), 0.0, -sqrt(6) / 3),
        0.15 .* (-sqrt(3) / 6, 0.5, -sqrt(6) / 3),
        0.15 .* (-sqrt(3) / 6, -0.5, -sqrt(6) / 3),
    ]

    environment = UnderwaterEnvironment(;
        seabed = SandyClay,
        bathymetry = 40.0,  # water depth in meters
    )

    model = PekerisRayTracer(environment)
    sim = Simulation(model, modem_frequency)
    addnode!(sim, transceiver_location, UASP2, transceiver_vao_port, ip"0.0.0.0"; relpos = transceiver_offsets)
    addnode!(sim, transponder_location, UASP2, transponder_vao_port, ip"0.0.0.0")
    return run(sim)
end

function configure_modems(
    ((transceiver_host, transceiver_port), transceiver_vao_port),
    ((transponder_host, transponder_port), transponder_vao_port),
)
    for (host, port, vao_port) in (
        (transceiver_host, transceiver_port, transceiver_vao_port),
        (transponder_host, transponder_port, transponder_vao_port),
    )
        gw = Gateway(host, port)
        try
            vao_ip, _ = getsockname(gw.sock[])
            put_file_req = PutFileReq(; #
                recipient = agent(gw, "websh"),
                filename = "scripts/modem.toml",
                contents = Vector{UInt8}("""
                                    [input]
                                    analoginterface = "UASP2DAQ"
                                    ip = "$(vao_ip)"
                                    port = $(vao_port)

                                    [output]
                                    analoginterface = "UASP2DAQ"
                                    """),
            )
            put_file_rsp = request(gw, put_file_req)
            if isnothing(put_file_rsp) || put_file_rsp.performative != Performative.AGREE
                error("Failed to upload configuration to modem at $(host):$(port)")
            end

            shell_exec_req = ShellExecReq(; #
                recipient = agent(gw, "websh"),
                command = "reboot",
            )
            Fjage.send(gw, shell_exec_req)
        finally
            close(gw)
        end
    end
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