@echo off
cd /d "%~dp0"
echo.
echo ##############################################
echo # Installing juliaup
echo.
winget list --id 9NJNWW8PVKMN -e >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    winget install --name Julia --id 9NJNWW8PVKMN -e -s msstore || exit /b 1
) else (
    echo juliaup already installed.
)
echo.
echo.

echo ##############################################
echo # Installing julia 1.12.4
echo.
juliaup add 1.12.4 || exit /b 1
echo.
echo.

echo ##############################################
echo # Installing packages
echo.
set JULIA_LOAD_PATH=@;@stdlib
julia +1.12.4 --project=usbl-julia --startup-file=no -e "using Pkg; Pkg.instantiate()" || exit /b 1
julia +1.12.4 --project=vao-julia --startup-file=no -e "using Pkg; Pkg.instantiate()" || exit /b 1
echo.
echo Done!