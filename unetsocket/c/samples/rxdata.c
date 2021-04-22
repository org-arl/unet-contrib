///////////////////////////////////////////////////////////////////////////////
//
// Receive data.
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

#define NBYTES 9

static int error(const char *msg) {
    printf("\n*** ERROR: %s\n\n", msg);
    return -1;
}

int main(int argc, char *argv[]) {
    unetsocket_t sock;
    fjage_msg_t ntf;
    uint8_t data[NBYTES];
    int port = 1100;
    int rv;
	if (argc <= 2) {
        error("Usage : rxdata <ip_address> <port>\n"
              "ip_address: IP address of the receiver modem. \n"
              "port: port number of the receiver modem \n"
              "A usage example: \n"
              "rxdata 192.168.1.10 1100\n");
        return -1;
    } else {
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
    printf("Connecting to %s:%d\n",argv[1],port);
    sock = unetsocket_open(argv[1], port);
    if (sock == NULL) return error("Couldn't open unet socket");

    // Bind to protocol DATA
    rv = unetsocket_bind(sock, DATA);

    if (rv != 0) return error("Error binding socket");

    // Set a timeout of 10 seconds
    unetsocket_set_timeout(sock, 20000);

    // Receive and display data
    printf("Waiting for a Datagram\n");

    ntf = unetsocket_receive(sock);
    if (fjage_msg_get_clazz(ntf) != NULL) {
        printf("Received a %s : [", fjage_msg_get_clazz(ntf));
        fjage_msg_get_byte_array(ntf, "data", data, NBYTES);
        for (int i = 0; i<9; i++) {
            printf("%d,", data[i]);
        }
        printf("]\n");
    } else {
        fjage_msg_destroy(ntf);
        return error("Error receiving data");
    }

    // sleep(2);
    // Close the unet socket
    unetsocket_close(sock);
    printf("Reception Complete\n");
    return 0;
}
