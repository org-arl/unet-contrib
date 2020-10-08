@echo off
setlocal enableextensions enabledelayedexpansion

rem # figure out home directory
if not "%UNET_HOME%"=="" goto cont0
set _f=%0
for %%F in ("%_f%\..") do set UNET_HOME=%%~dpF
:cont0

rem # figure out lib directory
if exist %UNET_HOME%\lib (
	set UNET_LIB=%UNET_HOME%lib
) else (
	set UNET_LIB=%UNET_HOME%\app
)

rem # use packaged java runtime or java on path
if exist %UNET_HOME%\runtime\bin (
  set JAVA=%UNET_HOME%\runtime\bin\java
) else (
  set JAVA=java
)

rem # compose classpath
set CP=
for %%a in (%UNET_LIB%\*.jar) do set CP=!CP!;%%a
set CP=!CP!;.;%UNET_HOME%\classes
for %%a in (%UNET_HOME%\jars\*.jar) do set CP=!CP!;%%a

rem # list of supported unet commands
set UNET_CMDS=sim,audio

rem # run unet
if not exist %UNET_HOME%\logs mkdir %UNET_HOME%\logs
if not exist %UNET_HOME%\store mkdir %UNET_HOME%\store
if not exist %UNET_HOME%\tmp mkdir %UNET_HOME%\tmp
%JAVA% -cp "%CP%" -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true -Djava.library.path="%UNET_LIB%" -Dunet.home="%UNET_HOME:~0,-1%" -Dunet.cmds="%UNET_CMDS%" org.arl.unet.UnetBoot %*
