///////////////////////////////////////////////////////////////////////////////
//
// Script to transmit a signal.
//
// In terminal window (an example):
//
// $ make samples
// $ ./txsignal <ip_address> [port]
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

#define FREQ    5000 // change this as per the need
#define SIGLEN  192000 // change this as per the need

static int error(const char *msg) {
  printf("\n*** ERROR: %s\n\n", msg);
  return -1;
}

int main(int argc, char *argv[]) {
	unetsocket_t sock;
	char id[FRAME_ID_LEN];
  int port = 1100;
  float signal[SIGLEN];
  int rv;
  if (argc <= 1) {
    error("Usage : txsignal <ip_address> [port] \n"
      "ip_address: IP address of the transmitter modem. \n"
      "port: port number of transmitter modem. \n"
      "A usage example: \n"
      "txsignal 192.168.1.20 1100\n");
    return -1;
  } else {
    if (argc > 2) port = (int)strtol(argv[2], NULL, 10);
  }
#ifndef _WIN32
// Check valid ip address
  struct hostent *server = gethostbyname(argv[1]);
  if (server == NULL) {
    error("Enter a valid ip addreess\n");
    return -1;
  }
#endif
  for(int i = 0; i < SIGLEN; i++) {
    signal[i] = (float)sin(FREQ * (2 * M_PI) * (i / (float)TXSAMPLINGFREQ));
  }
  // Open a unet socket connection to modem
  printf("Connecting to %s:%d\n",argv[1],port);

  sock = unetsocket_open(argv[1], port);
  if (sock == NULL) return error("Couldn't open unet socket");

  // Transmit data
  printf("Transmitting a CW\n");
  rv = unetsocket_tx_signal(sock, signal, SIGLEN, TXSAMPLINGFREQ, 0, id);
  if (rv == 0) printf("TX: %s\n", id);
  if (rv != 0) return error("Error transmitting signal");

	// Close the unet socket
  unetsocket_close(sock);

  printf("Transmission Complete\n");

  return 0;
}
