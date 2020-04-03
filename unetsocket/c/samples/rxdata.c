///////////////////////////////////////////////////////////////////////////////
//
// Script to receive data.
//
// In terminal window (an example):
//
// $ make samples
// $ ./rxdata <ip_address> <port>
//
////////////////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>
#include "../unet.h"

#ifndef _WIN32
#include <unistd.h>
#include <netdb.h>
#include <sys/time.h>
#endif

static int error(const char *msg)
{
    printf("\n*** ERROR: %s\n\n", msg);
    return -1;
}

int main(int argc, char *argv[])
{
    unetsocket_t sock;
    fjage_msg_t ntf;
    uint8_t data[7];
    int port = 1100;
	if (argc < 2)
    {
        error("Usage : rxdata <ip_address> <port>\n"
              "ip_address: IP address of the receiver modem. \n"
              "port: port number of the receiver modem \n"
              "A usage example: \n"
              "rxdata 192.168.1.10 1100\n");
        return -1;
    }
    if (argc > 2) {
        port = (int)strtol(argv[2], NULL, 10);
    }

#ifndef _WIN32
    // Check valid ip address
    struct hostent *server = gethostbyname(argv[1]);
    if (server == NULL) {
      error("Enter a valid ip addreess\n");
      return -1;
    }
#endif

    // Open a unet socket connection to modem
    sock = unetsocket_open(argv[1], port);
    if (sock == NULL) return error("Couldn't open unet socket");

    // Bind to protocol DATA
    unetsocket_bind(sock, DATA);

    // Set a timeout of 10 seconds
    unetsocket_set_timeout(sock, 10000);

    // Receive and display data
    ntf = unetsocket_receive(sock);
    if (fjage_msg_get_clazz(ntf) != NULL) {
        printf("clazz is : %s\n", fjage_msg_get_clazz(ntf));
    }
    else {
        fjage_msg_destroy(ntf);
        return -1;
    }
    fjage_msg_get_byte_array(ntf, "data", data, 7);
    for (int i = 0; i<7; i++) {
        printf("%d\n", data[i]);
    }

    // sleep(2);
    // Close the unet socket
    unetsocket_close(sock);
    return 0;
}