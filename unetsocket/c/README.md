# Unet C APIs

## Build Unet C API library

This folder contains the files `unet.h` and `unet.c`. The header file includes the definition and documentation of the C APIs and the source code is provided in the `unet.c` file. `Makefile` is used to collate the necessary files and compile the source code and run tests.

To build Unet C APIs library, run

```bash
make
```

This will generate a library (`libunet.a`) which can be used to link.

## Build samples

To build the object files for the samples, run

```bash
make samples
```

## Test Unet C API library

To compile and run the tests, the following steps need to be performed:

1. Set up the test environment. Folows the steps below to setup the test environment:

	a. To run tests, run the unet simulator with two nodes (or) if you have modems, power on two modems and set them up in water.

	b. Make sure an ethernet connection to one of the modem is available.

2. Run the tests as following:

In terminal window (an example):

```bash
$ make test
$ test/test_unet <ip_address> <peer_node_address> <port>
```
where `<ip_address>` argument must be the IP address of the modem to connect to, `<port>` is the port number of the Unet service on that modem and `<peer_node_address>` is the node address of the other modem in the water for ranging tests.

Upon completion of the tests, the test results will be summarized.

**NOTE**: To run the simulator with two nodes, download the unet community edition from [UnetStack](https://unetstack.net/) and run the following:

```bash
$ bin/unet samples/2-node-network.groovy
```

For more details on using the unet simulator to deploy 2 node network, follow the instructions in [unet handbook](https://unetstack.net/handbook/unet-handbook_getting_started.html)

## Clean

To clean all the built files and the depencencies, run

```bash
make clean
```