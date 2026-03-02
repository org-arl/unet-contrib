abstract type UnetMessage <: Fjage.Message end
abstract type BasebandSignal <: UnetMessage end

Fjage.@message "org.arl.unet.bb.DetectionNtf" :INFORM struct DetectionNtf <: UnetMessage
    captureStartTime::Union{Int64, Nothing} = nothing
    rxStartTime::Union{Int64, Nothing} = nothing
    rxDuration::Union{Int32, Nothing} = nothing
    preamble::Int32 = -1
    detector::Float32 = 0.0
end

Fjage.@message "org.arl.unet.bb.RecordBasebandSignalReq" :REQUEST struct RecordBasebandSignalReq <: UnetMessage
    recStartTime::Union{Int64, Nothing} = nothing
    recLength::Union{Int32, Nothing} = nothing
end

Fjage.@message "org.arl.unet.bb.RxBasebandSignalNtf" :INFORM struct RxBasebandSignalNtf <: BasebandSignal
    rxStartTime::Union{Int64, Nothing} = nothing
    location::Union{Vector{Float64}, Nothing} = nothing
    rssi::Float32 = NaN
    preambleLength::Int32 = 0
    signal::Union{Union{Vector{Complex{Float32}}, Vector{Float32}}, Nothing} = nothing
    fc::Float32 = -1.0
    fs::Float32 = -1.0
    channels::Int32 = 1
    preamble::Int32 = 0
end

Fjage.@message "org.arl.unet.localization.RangeNtf" :INFORM struct RangeNtf <: UnetMessage
    from::Int32 = 0
    to::Int32 = 0
    rxStartTime::Union{Int64, Nothing} = nothing
    range::Union{Float32, Nothing} = nothing
    offset::Union{Int64, Nothing} = nothing
    location::Union{Vector{Float64}, Nothing} = nothing
    peerLocation::Union{Vector{Float64}, Nothing} = nothing
    data::Union{Vector{UInt8}, Nothing} = nothing
end

Fjage.@message "org.arl.unet.localization.BearingNtf" :INFORM struct BearingNtf <: UnetMessage
    azimuth::Union{Float64, Nothing} = nothing
    elevation::Union{Float64, Nothing} = nothing
    worldAzimuth::Union{Float64, Nothing} = nothing
    worldElevation::Union{Float64, Nothing} = nothing
    rxStartTime::Union{Int64, Nothing} = nothing
    pitch::Union{Float64, Nothing} = nothing
    roll::Union{Float64, Nothing} = nothing
    yaw::Union{Float64, Nothing} = nothing
    location::Union{Vector{Float64}, Nothing} = nothing
end

Fjage.@message "org.arl.unet.localization.PeerLocationNtf" :INFORM struct PeerLocationNtf <: UnetMessage
    peer::Int32 = -1
    peerLocation::Union{Vector{Float64}, Nothing} = nothing
    rxStartTime::Union{Int64, Nothing} = nothing
    location::Union{Vector{Float64}, Nothing} = nothing
end