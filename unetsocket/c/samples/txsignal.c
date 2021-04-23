///////////////////////////////////////////////////////////////////////////////
//
// Transmit a signal.
//
// In terminal window (an example):
//
// $ make samples
// $ ./txsignal <ip_address> <signal_filename> [port]
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
	unetsocket_t sock;
	char id[FRAME_ID_LEN];
  int port = 1100;
  FILE *sigfile;
  char *filename = NULL;
  int ch, txbufsize = 0;
  float txsamplingfreq = 192000;
  int rv;
  if (argc <= 2) {
    error("Usage : txsignal <ip_address> <signal_filename> [port] \n"
      "ip_address: IP address of the transmitter modem. \n"
      "signal_filename: path to the file containing the signal to transmit. \n"
      "port: port number of transmitter modem. \n"
      "A usage example: \n"
      "txsignal 192.168.1.20 sample-tx-signal.txt 1100\n");
    return -1;
  } else {
    if (argc > 3) port = (int)strtol(argv[2], NULL, 10);
  }
  if (argc < 3) filename = "sample-tx-signal.txt";
#ifndef _WIN32
// Check valid ip address
  struct hostent *server = gethostbyname(argv[1]);
  if (server == NULL) {
    error("Enter a valid ip addreess\n");
    return -1;
  }
#endif

  // read the signal to be transmitted into an array
  filename = argv[2];
  sigfile = fopen(filename, "r");
  if (!sigfile)
  {
    error("Couldn't find signal file.\n");
    fclose(sigfile);
    return -1;
  }

  while(!feof(sigfile))
  {
    ch = fgetc(sigfile);
    if(ch == '\n')
    {
      txbufsize++;
    }
  }

  rewind(sigfile);
  float *txbuf = malloc((unsigned long)txbufsize * sizeof(float));
  if (txbuf == NULL)
  {
    error("Unable to allocate required memory.\n");
    return -1;
  }
  for (int i = 0; i < txbufsize; i++)
  {
    int rv = fscanf(sigfile, "%f", txbuf + i);
    if (rv < 1) {
      error("Signal file format error.\n");
      return -1;
    }
  }
  fclose(sigfile);

  // Open a unet socket connection to modem
  printf("Connecting to %s:%d\n",argv[1],port);

  sock = unetsocket_open(argv[1], port);
  if (sock == NULL) return error("Couldn't open unet socket");

  // read dac rate
  if (unetsocket_ext_fget(sock, -1, "org.arl.unet.Services.PHYSICAL", "dacrate", &txsamplingfreq) < 0) {
    return error("Failed to get dacrate parameter \n");
  } else {
    printf("Using %1.1f samples/s\n", txsamplingfreq);
  }

  // Transmit signal
  printf("Transmitting a CW\n");
  printf("Using signal from %s file \n", filename);
  rv = unetsocket_ext_tx_signal(sock, txbuf, txbufsize, 0, id);
  if (rv != 0) return error("Error transmitting signal");

	// Close the unet socket
  unetsocket_close(sock);

  printf("Transmission Complete\n");

  return 0;
}
