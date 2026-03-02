@echo off
cd /d "%~dp0"
set JULIA_LOAD_PATH=@;@stdlib
julia +1.12.4 --project=usbl-julia --startup-file=no usbl-julia/main.jl %*