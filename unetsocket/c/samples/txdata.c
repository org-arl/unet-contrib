///////////////////////////////////////////////////////////////////////////////
//
// Script to transmit data.
//
// In terminal window (an example):
//
// $ make samples
// $ ./txdata <ip_address> <peer_node_address>
//
// Pass the actual IP address of transmitter and node address of the receiver.
////////////////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <math.h>
#include "../unet.h"

static int error(const char *msg)
{
    printf("\n*** ERROR: %s\n\n", msg);
    return -1;
}

int main(int argc, char *argv[])
{
	int address = 0;
	uint8_t data[7] = {1, 2, 3, 4, 5, 6, 7};
	unetsocket_t sock;
	if (argc < 2)
    {
        error("Usage : txdata <ip_address> <peer_node_address> \n"
              "ip_address: IP address of the transmitter modem. \n"
              "peer_node_address: Node address of the receiver modem. Set this to 0 for broadcast. \n"
              "A usage example: \n"
              "txdata 192.168.1.20 5\n");
        return -1;
    }
    if (argc > 2)
    {
        address = (int)strtol(argv[2], NULL, 10);
    }
    // Open a unet socket connection to modem
    sock = unetsocket_open(argv[1], 1101);
    if (sock == NULL) return error("Couldn't open unet socket");

    unetsocket_send(sock, data, 7, 31, 0);

    sleep(2);

    unetsocket_close(sock);

    return 0;

}