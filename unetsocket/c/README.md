# Unet C APIs

This folder contains the files `unet.h`, `unet_ext.h` and `unet.c`, `unet_ext.c`. The header files includes the definition and documentation of the C APIs and the source code is provided in the `unet.c` and `unet_ext.c` file.

The APIs defined in `unet.h` are standard UnetSocket APIs. These APIs have similar functionalities as the UnetSocket APIs provided in other languages such as [Python](https://github.com/org-arl/unet-contrib/tree/stp/unetsocket/python) and [Julia](https://github.com/org-arl/UnetSockets.jl). The APIs defined in `unet_ext.h` are extra functionalities that are implemented using the standard UnetSocket APIs. Some of these APIs in `unet_ext.h` are only supported on [Unet SDOAMs](https://unetstack.net/handbook/unet-handbook_introduction.html).

## Instructions for building and using Unet C API library on Linux / macOS

### Build unet library

For building on Linux/macOS operating systems a `Makefile` is provided to collate the necessary files and compile the source code and run tests.

To build Unet C APIs library, run

```bash
make
```

This will generate a library (`libunet.a`) which can be used to link.

### Build samples

To build the object files for the samples, run

```bash
make samples
```

### Clean

To clean all the built files and the depencencies, run

```bash
make clean
```

## Instructions for building and using Unet C API library on Windows

*Prerequisites:*

1. You can build C applications on the command line by using tools that are included in Visual Studio. We use the Developer command prompt for Visual Studio to build from command line. To install these tools visit the following [link](https://docs.microsoft.com/en-us/dotnet/framework/tools/developer-command-prompt-for-vs).

2. Build the fjage C library using the instructions provided on this [link](https://github.com/org-arl/fjage/tree/dev/gateways/c). This should provide you with a `fjage.lib` library which can be used to link.

3. Copy the `fjage.lib` and `fjage.h` file to this directory.

### Build unet library

To build unet library, run

```powershell
$ cl /LD fjage.lib *.c
$ lib unet.obj unet_ext.obj pthreadwindows.obj /out:unet.lib
```

This will generate a library (`unet.lib`) which can be used to link.

### Build samples

To build the executable files for the samples, run

```powershell
$ cl fjage.lib unet.lib samples\txdata.c /link /out:samples\txdata.exe
$ cl fjage.lib unet.lib samples\rxdata.c /link /out:samples\rxdata.exe
```

### Clean

To clean all the built files and the depencencies, run

```powershell
del *.obj *.dll unet.lib test\*.obj test\*.exe samples\*.obj samples\*.exe 2>nul
```

## Instructions for creating API package for distribution

To create a package for distribution of these APIs, simply type

```bash
make package
```

This will create a `c-api.zip` inside the `build` folder which can be used for distribution.

## Test Unet C API library

To compile and run the tests, the following steps need to be performed:

1. Set up the test environment. Folows the steps below to setup the test environment:
    a. To run tests, run the unet simulator with two nodes (or) if you have modems, power on two modems and set them up in water.
    b. Make sure an ethernet connection to one of the modem is available.

2. Run the tests as following:

On Linux / macOS, in terminal window (an example):

```bash
$ make test
$ test/test_unet <ip_tx> <ip_rx> <port_tx> <port_rx>
```

On Windows, in Developer command prompt for Visual Studio

```powershell
$ cl fjage.lib unet.lib test\test_unet.c /link /out:test\test_unet.exe
$ test/test_unet.exe <ip_tx> <ip_rx> <port_tx> <port_rx>
```

where `<ip_tx>` argument is the IP address of the transmitter modem to connect to and `<ip_rx>` argument is the IP address of the receiver modem to connect to, `<port_tx>` and `<port_rx>` is the port numbers of the Unet service on transmitter and receiver modems respectively.

Upon completion of the tests, the test results will be summarized.

**NOTE**: To run the simulator with two nodes, download the unet community edition from [UnetStack](https://unetstack.net/) and run the following:

on Linux / macOS :

```powershell
$ bin/unet samples/2-node-network.groovy
```

on Windows:

```bash
$ bin\unet.bat samples\2-node-network.groovy
```

For more details on using the unet simulator to deploy 2 node network, follow the instructions in [unet handbook](https://unetstack.net/handbook/unet-handbook_getting_started.html)