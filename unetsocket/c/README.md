# Unet C APIs

This folder contains the files `unet.h` and `unet.c`. The header file includes the definition and documentation of the C APIs and the source code is provided in the `unet.c` file. 

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

```bash
$ cl /LD fjage.lib *.c
$ lib unet.obj pthreadwindows.obj /out:unet.lib
```

This will generate a library (`unet.lib`) which can be used to link.


### Build samples

To build the executable files for the samples, run

```bash
$ cl fjage.lib unet.lib samples\txdata.c /link /out:samples\txdata.exe
$ cl fjage.lib unet.lib samples\rxdata.c /link /out:samples\rxdata.exe
```

### Clean

To clean all the built files and the depencencies, run

```bash
del *.obj *.dll unet.lib test\*.obj test\*.exe samples\*.obj samples\*.exe 2>nul
```

## Test Unet C API library

To compile and run the tests, the following steps need to be performed:

1. Set up the test environment. Folows the steps below to setup the test environment:

	a. To run tests, run the unet simulator with two nodes (or) if you have modems, power on two modems and set them up in water.

	b. Make sure an ethernet connection to one of the modem is available.

2. Run the tests as following:

On Linux / macOS, in terminal window (an example):

```bash
$ make test
$ test/test_unet <ip_address> <rx_node_address> <port>
```

On Windows, in Developer command prompt for Visual Studio

```bash
$ cl fjage.lib unet.lib test\test_unet.c /link /out:test\test_unet.exe
$ test/test_unet.exe <ip_address> <rx_node_address> <port>
```

where `<ip_address>` argument must be the IP address of the modem to connect to, `<port>` is the port number of the Unet service on that modem and `<rx_node_address>` is the node address of the other modem in the water for ranging tests.

Upon completion of the tests, the test results will be summarized.

**NOTE**: To run the simulator with two nodes, download the unet community edition from [UnetStack](https://unetstack.net/) and run the following:

```bash
$ bin/unet samples/2-node-network.groovy
```
on Linux / macOS and
```bash
$ bin\unet.bat samples\2-node-network.groovy
```
on Windows.

For more details on using the unet simulator to deploy 2 node network, follow the instructions in [unet handbook](https://unetstack.net/handbook/unet-handbook_getting_started.html)