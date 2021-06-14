///////////////////////////////////////////////////////////////////////////////
//
// Transmit n pulses with specified pulse repitition interval (pri)
//
// In terminal window (an example):
//
// $ make samples
// $ ./npulses <ip_address> <n> <pri> [siglen] [frequency] [port]
//
////////////////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>
#include "../unet.h"
#include "../unet_ext.h"
#include <math.h>

#ifndef _WIN32
#include <unistd.h>
#include <netdb.h>
#include <sys/time.h>
#endif

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

static int error(const char *msg) {
  printf("\n*** ERROR: %s\n\n", msg);
  return -1;
}

int main(int argc, char *argv[]) {
	unetsocket_t sock;
  int port = 1100;
  int siglen = 1920*5;
  int frequency = 5000;
  int n = 8;
  int pri = 1000;
  float txsamplingfreq = 192000;
  int rv;
  if (argc <= 3) {
    error("Usage : npulses <ip_address> <n> <pri> [siglen] [frequency] [port] \n"
      "ip_address: IP address of the transmitter modem. \n"
      "n: number of pulses to transmit. \n"
      "pri: pulse repitition interval (ms). \n"
      "siglen: number of passband samples in one pulse. \n"
      "frequency: frequency of the pulse (Hz). \n"
      "port: port number of transmitter modem. \n"
      "A usage example: \n"
      "npulses 192.168.1.20 1100\n");
    return -1;
  } else if (argc >= 4) {
    n = (int)strtol(argv[2], NULL, 10);
    pri = (int)strtol(argv[3], NULL, 10);
    if (argc > 4) siglen = (int)strtol(argv[4], NULL, 10);
    if (argc > 5) frequency = (int)strtol(argv[5], NULL, 10);
    if (argc > 6) port = (int)strtol(argv[6], NULL, 10);
  }

#ifndef _WIN32
// Check valid ip address
  struct hostent *server = gethostbyname(argv[1]);
  if (server == NULL) {
    error("Enter a valid ip addreess\n");
    return -1;
  }
#endif

  float* signal = malloc((unsigned long)siglen*sizeof(float));

  // Open a unet socket connection to modem
  printf("Connecting to %s:%d\n",argv[1],port);

  sock = unetsocket_open(argv[1], port);
  if (sock == NULL) {
    free(signal);
    return error("Couldn't open unet socket");
  }

  // read dac rate
  if (unetsocket_ext_fget(sock, -1, "org.arl.unet.Services.PHYSICAL", "dacrate", &txsamplingfreq) < 0) {
    free(signal);
    return error("Failed to get dacrate parameter \n");
  } else {
    printf("Using %1.1f samples/s\n", txsamplingfreq);
  }

  // create the signal
  for(int i = 0; i < siglen; i++) {
    signal[i] = (float)sin(frequency * (2 * M_PI * i) / txsamplingfreq);
  }

  // Transmit signal
  printf("Transmitting a CW\n");
  rv = unetsocket_ext_npulses(sock, signal, siglen, (int)txsamplingfreq, n, pri);
  if (rv != 0) {
    free(signal);
    return error("Error transmitting signal");
  }

	// Close the unet socket
  unetsocket_close(sock);

  free(signal);

  printf("Transmission Complete\n");

  return 0;
}
