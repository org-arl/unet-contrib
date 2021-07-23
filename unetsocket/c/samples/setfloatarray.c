///////////////////////////////////////////////////////////////////////////////
//
// Set a parameter whose value is a float array
//
// In terminal window (an example):
//
// $ make samples
// $ ./setfloatarray <ip_address> [port]
//
////////////////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>
#include "../unet.h"
#include "../unet_ext.h"

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
  float location[3] = {1.0, 2.0, 3.0};
  int port = 1100;
  if (argc <= 2) {
    error("Usage : setfloatarray <ip_address> [port] \n"
      "ip_address: IP address of the transmitter modem. \n"
      "port: port number of transmitter modem. \n"
      "A usage example: \n"
      "setfloatarray 192.168.1.20 1100\n");
    return -1;
  } else if (argc >= 3) {
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

  // unetsocket_ext_fset_array(sock, 0, "org.arl.unet.Services.NODE_INFO", "location", location, 3);

  // set float array
  if (unetsocket_ext_fset_array(sock, 0, "org.arl.unet.Services.NODE_INFO", "location", location, 3) < 0) {
    return error("Failed to set node location \n");
  }

  // Close the unet socket
  unetsocket_close(sock);

  printf("Node location set succesfully.\n");

  return 0;
}
