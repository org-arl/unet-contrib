/*
 * rxdatagram : receive a datagram to a remote host
 *
 * Usage: rxdatagram [-i ipaddress] [-p port]
 *
 * Example : rxdatagram
 *           rxdatagram -p 1101
 *           rxdatagram -p 1101 -i localhost
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include "../unet.h"

#ifndef _WIN32
#include <unistd.h>
#include <netdb.h>
#include <sys/time.h>
#endif

static int error(const char *msg) {
  fprintf(stderr, "\n*** ERROR: %s\n", msg);
  exit(EXIT_FAILURE);
}

void parse_args(int argc, char *argv[], char **ip, int *port) {
    int opt;
    while ((opt = getopt(argc, argv, "p:i:")) != -1) {
        switch (opt) {
        case 'p':
            *port = atoi(optarg);
            break;
        case 'i':
            *ip = optarg;
            break;
        default: /* '?' */
            fprintf(stderr, "Usage: %s [-i ipaddress] [-p port] [command]\n", argv[0]);
            exit(EXIT_FAILURE);
        }
    }
}

int main(int argc, char *argv[]) {

    // Parse the command line arguments (IP/Port)
    char *ip = "localhost";
    int port = 1101;
    parse_args(argc, argv, &ip, &port);

    /******** Parameters ********/
    // Set the values request parameters here!
    uint protocol = 0;                       // The protocol to listen to [0, 32-64]
    uint data_len = 8192;                    // The maximum length of the data to receive
    int timeout = 10000;                      // The timeout in milliseconds

    // Open unet socket
    unetsocket_t sock = unetsocket_open(ip, port);
    if (sock == NULL) return error("Couldn't open unet socket");


    // A UnetSocket is already subscribed to all Agents that provide the Datagram service
    // So nothing to do here.

    // Receive a datagram
    unetsocket_bind(sock, (int)protocol);
    unetsocket_set_timeout(sock, timeout);
    fjage_msg_t datagram = unetsocket_receive(sock);

    if (datagram == NULL) {
        unetsocket_close(sock);
        error("Error receiving datagram\n");
    }

    uint8_t* data = malloc(data_len);  // Buffer to store the received data
    char* hex = malloc(data_len*2+1);  // Buffer to store the received data in hexadecimal format
    int l = fjage_msg_get_byte_array(datagram, "data", data, (int)data_len);
    for (int i=0; i<l; i++) sprintf(hex+i*2, "%02X", data[i]);
    printf("%s\n", hex);
    free(data);
    free(hex);
    return 0;
}
