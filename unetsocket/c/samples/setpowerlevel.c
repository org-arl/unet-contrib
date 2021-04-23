///////////////////////////////////////////////////////////////////////////////
//
// Set the transmission power level.
//
// In terminal window (an example):
//
// $ make samples
// $ ./setpowerlevel <ip_address> <power_value> [port]
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
  float power_value = -42.0;
  int port = 1100;
  int rv;
  if (argc <= 3) {
    error("Usage : setpowerlevel <ip_address> <power_value> <port> \n"
      "ip_address: IP address of the transmitter modem. \n"
      "power_value: Transmission power level to set. \n"
      "port: port number of transmitter modem. \n"
      "A usage example: \n"
      "setpowerlevel 192.168.1.20 -6 1101\n");
    return -1;
  } else {
  	power_value = strtof(argv[2], NULL);
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

  // set power level of the CONTROL frame
  rv = unetsocket_ext_set_powerlevel(sock, 1, power_value);
  if (rv == 0) printf("Transmission power level of CONTROL frame set to : %f \n", power_value);

  // set power level of the DATA frame
  rv = unetsocket_ext_set_powerlevel(sock, 2, power_value);
  if (rv == 0) printf("Transmission power level of DATA frame set to : %f \n", power_value);

  // Close the unet socket
  unetsocket_close(sock);

  printf("Setting transmission power level complete\n");

  return 0;
}
