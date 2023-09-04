///////////////////////////////////////////////////////////////////////////////
//
// Record the received data to a WAV file
//
// In terminal window (an example):
//
// $ make samples
// $ ./rxwav <ip_address> <length> <filename> [port]
//
////////////////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>
#include "../unet.h"
#include "wav_file.h"

int main(int argc, char *argv[]) {
    printf("Hello, World! : %d, %s \n", argc, argv[0]);
    return 0;
}
