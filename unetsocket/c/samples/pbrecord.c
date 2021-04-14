///////////////////////////////////////////////////////////////////////////////
//
// Script to record a signal.
//
// In terminal window (an example):
//
// $ make samples
// $ ./pbrecord <ip_address> [port]
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

#define SIGLEN  48000*6 // change this as per the need

static int error(const char *msg) {
  printf("\n*** ERROR: %s\n\n", msg);
  return -1;
}

int main(int argc, char *argv[]) {
  FILE *fptr;
  fptr = (fopen("samples/recordedsignal.txt", "w"));
  if(fptr == NULL) return -1;
  unetsocket_t sock;
  int port = 1100;
  float buf[SIGLEN];
  int rv;
  if (argc <= 1) {
    error("Usage : record <ip_address> [port] \n"
      "ip_address: IP address of the transmitter modem. \n"
      "port: port number of transmitter modem. \n"
      "A usage example: \n"
      "record 192.168.1.20 1100\n");
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
  // Open a unet socket connection to modem
  printf("Connecting to %s:%d\n",argv[1],port);

  sock = unetsocket_open(argv[1], port);
  if (sock == NULL) return error("Couldn't open unet socket");

  // Record
  printf("Starting a record\n");
  rv = unetsocket_pbrecord(sock, buf, SIGLEN);
  if (rv == 0) {
    for(int i = 0; i < SIGLEN; i++) {
      // printf("%f", buf[i]);
      fprintf(fptr,"%f\n", buf[i]);
    }
  }
  if (rv != 0) return error("Error recording signal");

  // Close the unet socket
  unetsocket_close(sock);

  printf("\nRecording Complete\n");

  fclose(fptr);
  return 0;
}
