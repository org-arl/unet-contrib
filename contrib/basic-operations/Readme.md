Basic Operations
=============

Examples of how to use various Unet APIs (Python, Groovy/Java, C) to connect to a modem or simulator running Unet.


## Bundle

The examples can be bundled into a `tgz` archive to be installed into a modem or co-processor.

The Makefile command `make bundle` creates a bundle `Examples.tgz` with all the compiled example files and the required libraries.

> Note: The C-API requires native libraries `libfjage.a` and `libunet.a` which are architecture dependent. Please ensure they are bundled on the same architecture (x86 or ARM, etc) as where the bundle is intended to be run

The C language examples require `unet-framework-1.4.jar` which can be compiled form Unet sourcecode if required. For this the Makefile needs to be pointed to the directory with Unet sourcecode using the `UNET_DIR=` argument.

For eg.
 
```bash
make bundle UNET_DIR="/home/ubuntu/unet"
```

