///////////////////////////////////////////////////////////////////////////////
//
// Script to receive data.
//
// In terminal window (an example):
//
// $ make samples
// $ ./rxdata <ip_address>
//
// Pass the actual IP address of the modem in use for receiving data.
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
	unetsocket_t sock;

	if (argc < 2)
    {
        error("Usage : rxdata <ip_address> \n"
              "ip_address: IP address of the receiver modem. \n"
              "A usage example: \n"
              "rxdata 192.168.1.10 \n");
        return -1;
    }

    // Open a unet socket connection to modem
    sock = unetsocket_open(argv[1], 1102);
    if (sock == NULL) return error("Couldn't open unet socket");

    unetsocket_bind(sock, 0);
    unetsocket_receive(sock);

    sleep(2);

    unetsocket_close(sock);

    return 0;
}