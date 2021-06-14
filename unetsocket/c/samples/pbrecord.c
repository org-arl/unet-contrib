///////////////////////////////////////////////////////////////////////////////
//
// Record a passband signal.
//
// In terminal window (an example):
//
// $ make samples
// $ ./pbrecord <ip_address> <siglen> [port]
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

static int error(const char *msg) {
  printf("\n*** ERROR: %s\n\n", msg);
  return -1;
}

int main(int argc, char *argv[]) {
  FILE *fptr;
  fptr = (fopen("samples/pbrecordedsignal.txt", "w"));
  if(fptr == NULL) return -1;
  unetsocket_t sock;
  int port = 1100;
  int siglen = -1;
  int rv;
  if (argc <= 2) {
    error("Usage : pbrecord <ip_address> <siglen> [port] \n"
      "ip_address: IP address of the transmitter modem. \n"
      "siglen: number of passband samples to record. \n"
      "port: port number of transmitter modem. \n"
      "A usage example: \n"
      "pbrecord 192.168.1.20 48000 1100\n");
    return -1;
  } else if (argc >= 3) {
    siglen = (int)strtol(argv[2], NULL, 10);
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

  float* buf = malloc((unsigned long)siglen*sizeof(float));

  // Open a unet socket connection to modem
  printf("Connecting to %s:%d\n",argv[1],port);

  sock = unetsocket_open(argv[1], port);
  if (sock == NULL) {
    free(buf);
    return error("Couldn't open unet socket");
  }

  // Record
  printf("Starting a record\n");
  rv = unetsocket_ext_pbrecord(sock, buf, siglen);
  if (rv == 0) {
    for(int i = 0; i < siglen; i++) {
      fprintf(fptr,"%f\n", buf[i]);
    }
  } else {
    free(buf);
    return error("Error recording signal");
  }

  // Close the unet socket
  unetsocket_close(sock);

  free(buf);

  printf("\nRecording Complete (samples/pbrecordedsignal.txt saved) \n");

  fclose(fptr);
  return 0;
}
