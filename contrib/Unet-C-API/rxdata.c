///////////////////////////////////////////////////////////////////////////////
//
// Script to receive data.
//
// In terminal window (an example):
//
// $ make
// $ make rxdata
// $ ./rxdata 192.168.1.119
//
// Pass the actual IP address of the modem in use for receiving data.
////////////////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <math.h>
#include "unet.h"


static int error(const char *msg)
{
    printf("\n*** ERROR: %s\n\n", msg);
    return -1;
}

static int checkdata(uint8_t *data, int nbytes)
{
    for (int i = 0; i < nbytes ; i++)
    {
        printf("%d\n", data[i]);
    }
    return 0;
}

static void rxcb(int from, int to, modem_packet_t type, void *data, int nbytes, long time)
{
    printf("RxFrameNtf from:%d to:%d type:%d time:%ld (%d bytes)\n", from, to, type, time, nbytes);
    checkdata(data, nbytes);
}

int main(int argc, char *argv[])
{

    modem_t modem;

    if (argc < 2)
    {
        printf("Usage rxdata <ip-address> \n");
        return -1;
    }

    // Open a connection to modem
    modem = modem_open_eth(argv[1], 1100);
    if (modem == NULL) return error("Couldn't open modem");
    modem_set_rx_callback(modem, rxcb);
    while(1)
    {
        sleep(1);
    };

    // close the connection to modem
    modem_close(modem);

    return 0;
}
