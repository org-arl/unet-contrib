///////////////////////////////////////////////////////////////////////////////
//
// Transmit data.
//
// In terminal window (an example):
//
// $ make samples
// $ ./txdata <ip_address> <rx_node_address> [port]
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
  int address = 0;
  int port = 1100;
  uint8_t data[NBYTES] = {1, 2, 3, 4, 5, 6, 7, 8, 9};
  int rv;
  if (argc <= 2) {
    error("Usage : txdata <ip_address> <rx_node_address> [port] \n"
      "ip_address: IP address of the transmitter modem. \n"
      "rx_node_address: Node address of the receiver modem. \n"
      "port: port number of transmitter modem. \n"
      "A usage example: \n"
      "txdata 192.168.1.20 5 1100\n");
    return -1;
  } else {
    address = (int)strtol(argv[2], NULL, 10);
    if (argc > 3) port = (int)strtol(argv[3], NULL, 10);
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

// Transmit data
  printf("Transmitting %d bytes of data to %d\n", NBYTES, address);
  rv = unetsocket_send(sock, data, NBYTES, address, DATA);

  if (rv != 0) return error("Error transmitting data");

// Close the unet socket
  unetsocket_close(sock);

  printf("Transmission Complete\n");

  return 0;

}
