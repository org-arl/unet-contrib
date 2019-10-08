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

1. Two modems are needed to be setup in the water for communication.

2. Connect to one of the modem via ethernet or wifi.

3. Open a terminal window and compile `test.c` using :

```bash
make test
```

4. Run test as follows:

```bash
test/test_unet <IP> <NODE_ID>
```
where `<IP>` argument must be the IP address of the modem to connect to, `<PORT>` is the port number of the Unet service on that modem and `<NODE_ID>` is the node address of the other modem in the water for ranging tests.

Upon completion of the tests, the test results will be summarized.

## Clean

To clean all the built files and the depencencies, run

```bash
make deepclean
```

To clean only the built files and not the dependencies, run

```bash
make clean
```