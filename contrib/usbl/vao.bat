@echo off
cd /d "%~dp0"
set JULIA_LOAD_PATH=@;@stdlib
julia +1.12.4 --project=vao-julia --startup-file=no -t auto vao-julia/main.jl %*