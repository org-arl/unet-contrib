/*
 * range : measure the range to a modem
 *
 * Usage: range [-i ipaddress] [-p port] [node]
 *
 * Example : range 42
 *           range -p 1101 -i localhost 32
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include "../unet.h"
#include "../unet_ext.h"

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
    int node = optind < argc ? atoi(argv[optind]) : 1;      // Get the node address to range to
    int timeout = 1000;                                     // The timeout in milliseconds

    unetsocket_t sock = unetsocket_open(ip, port);
    if (sock == NULL) return error("Couldn't open unet socket");
    unetsocket_set_timeout(sock, timeout);

    // ranging
    float range;
    int rv = unetsocket_ext_get_range(sock, node, &range);
    if (rv == 0) printf("%0.1f m", range);
    else error("Raging not successful");

    // Close the unet socket
    unetsocket_close(sock);
    return 0;
}
