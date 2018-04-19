## Run Tests Utilizing Unet C API

This folder contains the files `unet.h` and `unet.c`. The header file includes the definition and documentation of the C APIs and the source code is provided in the `unet.c` file. Examples of usage of these APIs can be found in `test_unet.c`. `Makefile` is used to collate the necessary files and compile the source code and run tests.

To compile and run the tests, the following steps need to be performed:

- Two modems are needed to be setup in the water for communication.

- Connect to one of the modem via ethernet or wifi.

- Open a terminal window and run

`make IP="192.168.0.7" ADDR=9 test`

where `IP` argument must be the IP address of the modem you are aiming to run the test on and `ADDR` argument is the node address of the other modem in the water for ranging tests.

Upon completion of the tests, the test results will be summarized.

- To delete the files and recompile from scratch, run `make clean`.








