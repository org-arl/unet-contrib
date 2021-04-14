///////////////////////////////////////////////////////////////////////////////
//
// Script to test gpio.
//
// In terminal window (an example):
//
// $ make samples
// $ ./gpio <ip_address> <pin_1> <pin_2>
//
////////////////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>
#include "../unet.h"
#include <math.h>

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
  int port = 1100;
  int pin_1 = 1;
  int pin_2 = 2;
  int rv = -1;
  int value = 0;

  if (argc <= 3) {
    error("Usage : gpio <ip_address> <pin_1> <pin_2>\n"
      "ip_address: IP address of the transmitter modem. \n"
      "pin_1: GPIO pin 1. \n"
      "pin_2: GPIO pin 2. \n"
      "A usage example: \n"
      "gpio 192.168.1.20 1 2\n");
    return -1;
  } else {
  	pin_1 = (int)strtol(argv[2], NULL, 10);
  	pin_2 = (int)strtol(argv[3], NULL, 10);
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

  // create an agentid
  fjage_aid_t gpio = fjage_aid_create("gpio");
  rv = unetsocket_bset(sock, 1, gpio, "enable", 1);
  if (rv == 0) printf("DEBUG1\n");
  rv = unetsocket_bset(sock, 2, gpio, "enable", 1);
  if (rv == 0) printf("DEBUG2\n");
  rv = unetsocket_sset(sock, 1, gpio, "mode", "out");
  if (rv == 0) printf("DEBUG3\n");
  rv = unetsocket_sset(sock, 2, gpio, "mode", "in");
  if (rv == 0) printf("DEBUG4\n");
	rv = unetsocket_iset(sock, 1, gpio, "value", 1);
  if (rv == 0) printf("DEBUG5\n");
  rv = unetsocket_iget(sock, 2, gpio, "value", &value);
  if (value == 1) printf("DEBUG6\n");
  fjage_aid_destroy(gpio);

	return 0;

}
