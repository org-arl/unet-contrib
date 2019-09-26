///////////////////////////////////////////////////////////////////////////////
//
// Script to wakeup a modem over RS232.
//
// In terminal window (an example):
//
// $ make
// $ make rs3232_wakeup
// $ ./rs232_wakeup <devname>
//
////////////////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <math.h>
#include <inttypes.h>
#include "unet.h"

static int error(const char *msg)
{
    printf("\n*** ERROR: %s\n\n", msg);
    return -1;
}

int main(int argc, char *argv[])
{

    if (argc < 2)
    {
        printf("Usage rs232_wakeup <dev_name> \n");
        return -1;
    }

    modem_rs232_wakeup(argv[1], 115200, "N81");

    printf("Done..\n");

    return 0;
}