///////////////////////////////////////////////////////////////////////////////
//
// Measure distance to another node.
//
// In terminal window (an example):
//
// $ make samples
// $ ./range <ip_address> <rx_node_address> [port]
//
////////////////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>
#include "../unet.h"
#include "../unet_ext.h"
#include "../fjage.h"

#ifndef _WIN32
#include <unistd.h>
#include <netdb.h>
#include <sys/time.h>
#endif

static int error(const char *msg) {
  printf("\n*** ERROR: %s\n\n", msg);
  return -1;
}

int main(int argc, char *argv[]) {
  unetsocket_t sock;
  int address = 0;
  int port = 1100;
  int rv;
  float* range = calloc(1,sizeof(float));
  if (argc <= 3) {
    error("Usage : range <ip_address> <rx_node_address> <port> \n"
      "ip_address: IP address of the transmitter modem. \n"
      "rx_node_address: Node address of the modem to which range is to be measured. \n"
      "port: port number of transmitter modem. \n"
      "A usage example: \n"
      "range 192.168.1.20 31 1101\n");
    return -1;
  } else {
  	address = (int)strtol(argv[2], NULL, 10);
    port = (int)strtol(argv[3], NULL, 10);
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

  // ranging
  rv = unetsocket_ext_get_range(sock, address, range);
  if (rv == 0) {
    printf("Range measured is : %f \n", *range);
  } else {
    error("Raging not successful");
  }

  // Close the unet socket
  unetsocket_close(sock);

  printf("Ranging Complete\n");

  return 0;
}
