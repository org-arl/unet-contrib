///////////////////////////////////////////////////////////////////////////////
//
// Transmit a signal read from a wav file.
//
// In terminal window :
//
// $ make samples
// $ ./txwav <ip_address> <file.wav> [port]
//
////////////////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>
#include "../unet.h"
#include "../unet_ext.h"
#include "wav_file.h"

#ifndef _WIN32
#include <unistd.h>
#include <netdb.h>
#include <sys/time.h>
#endif

#define BUF_LENGTH 1024

static int error(const char *msg) {
  printf("\n*** ERROR: %s\n\n", msg);
  return -1;
}

int main(int argc, char *argv[]) {
  WAV_FILE_INFO wavinfo;
  unetsocket_t sock;
  char *fname;
  int port = 1100;
  int rv;
  if (argc <= 2) {
    return printf("Usage : txwav <ip_address> <file.wav> [port] \n"
      "ip_address: IP address of the transmitter modem. \n"
      "file.wav: Passband audio file to transmit. \n"
      "port: port number of transmitter modem. \n"
      "A usage example: \n"
      "txdata 192.168.1.20 passband.wav 1100\n");
  } else {
    fname = argv[2];
    if (argc > 3) port = (int)strtol(argv[3], NULL, 10);
  }

  // check if file exists
  FILE *fp = fopen(fname, "rb");
  if (fp == NULL) return error("File does not exist\n");

  // open and parse the wavefile
  wavinfo = wav_read_header (fp);
  if (wavinfo.NumberOfChannels == 0) return error("Error reading wav file\n");
  else if (wavinfo.NumberOfSamples <= 0) return error("Empty wav file\n");

  #ifndef _WIN32
  // Check valid ip address
    struct hostent *server = gethostbyname(argv[1]);
    if (server == NULL) return error("Enter a valid ip addreess\n");
  #endif

  // Open a unet socket connection to modem
  printf("Connecting to %s:%d\n",argv[1],port);

  sock = unetsocket_open(argv[1], port);
  if (sock == NULL) return error("Couldn't open unet socket");

  // Get bb.dacrate parameter
  float txsamplingfreq = 0;
  if (unetsocket_ext_fget(sock, -1, "org.arl.unet.Services.BASEBAND", "dacrate", &txsamplingfreq) < 0) {
    return error("Failed to get dacrate parameter \n");
  }

  if ((int)txsamplingfreq != wavinfo.SampleRate) {
    printf("\n*** ERROR: Sampling frequency of wav file does not match dacrate parameter %f != %d \n\n", txsamplingfreq, wavinfo.SampleRate);
    return -1;
  }
  double* input = malloc(BUF_LENGTH*sizeof(double));
  float* signal = malloc((unsigned long)wavinfo.NumberOfSamples*sizeof(float));
  float maxvalue = (float)(((int)1) << wavinfo.BytesPerSample*8);

  // read the wav file
  int s=0;
  int count=0;
  while ((count = (int)wav_read_data (input, fp, wavinfo, BUF_LENGTH)) == BUF_LENGTH) {
    for (int i = 0; i < count; i++) signal[s++] = (float)(input[i]/maxvalue);
  }

  rv = unetsocket_ext_tx_signal(sock, signal, s, 0, NULL);
  if (rv == 0) {
    printf("Transmitted %d samples\n", wavinfo.NumberOfSamples);
    return 0;
  } else {
    return error("Failed to transmit signal\n");
  }
}
