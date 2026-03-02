Fjage.@message "org.arl.fjage.shell.PutFileReq" :INFORM struct PutFileReq <: Fjage.Message
    filename::Union{String, Nothing} = nothing
    contents::Union{Vector{UInt8}, Nothing} = nothing
    offset::Int64 = 0
end

Fjage.@message "org.arl.fjage.shell.ShellExecReq" :INFORM struct ShellExecReq <: Fjage.Message
    command::Union{String, Nothing} = nothing
    script::Union{String, Nothing} = nothing
    scriptArgs::Union{Vector{String}, Nothing} = nothing
    ans::Bool = false
end