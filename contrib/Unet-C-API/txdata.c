///////////////////////////////////////////////////////////////////////////////
//
// Script to transmit data.
//
// In terminal window (an example):
//
// $ make
// $ make txdata
// $ ./txdata 192.168.1.119 2
//
// Pass the actual IP address of transmitter and node address of the receiver.
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

static void txcb(const char *id, modem_packet_t type, long time)
{
    printf("TxFrameNtf type:%d time:%ld id:%s\n", type, time, id);
}

int main(int argc, char *argv[])
{

    int x;
    int address = 0;
    char id[FRAME_ID_LEN];
    uint8_t data[7] = {1, 2, 3, 4, 5, 6, 7};
    modem_t modem;

    if (argc < 2)
    {
        printf("Usage txdata <ip-address> [node-address] \n");
        return -1;
    }

    if (argc > 2)
    {
        address = (int)strtol(argv[2], NULL, 10);
    }

    // Open a connection to modem
    modem = modem_open_eth(argv[1], 1100);
    if (modem == NULL) return error("Couldn't open modem");

    modem_set_tx_callback(modem, txcb);

    // Test packet transmission of different types
    for (int i = 1; i <= 3 ; i++)
    {
        x = modem_tx_data(modem, address, data, sizeof(data), i, id);
        if (x == 0) printf("TX: %s\n", id);
        sleep(3);
    }

    // close the connection to modem
    modem_close(modem);

    return 0;
}
