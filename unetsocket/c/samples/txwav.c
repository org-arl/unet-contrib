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

#define DR_WAV_IMPLEMENTATION
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wimplicit-int-conversion"
#pragma GCC diagnostic ignored "-Wformat"
#pragma GCC diagnostic ignored "-Wsign-conversion"
#include "dr_wav.h"
#pragma GCC diagnostic pop


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
  char *ipaddr;
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
    ipaddr = argv[1];
    fname = argv[2];
    if (argc > 3) port = (int)strtol(argv[3], NULL, 10);
  }

  // check if file exists
  FILE *fp = fopen(fname, "rb");
  if (fp == NULL) return error("File does not exist\n");
  fclose(fp);

  unsigned int channels;
  unsigned int sampleRate;
  drwav_uint64 totalPCMFrameCount;
  float* pSampleData = drwav_open_file_and_read_pcm_frames_f32(fname, &channels, &sampleRate, &totalPCMFrameCount, NULL);
  if (pSampleData == NULL) {
    return error("Error opening and reading wav file\n");
  }

  printf("Wav file [%s] : fs=%d, nchannels=%d, nsamples=%llu\n", fname, sampleRate, channels, totalPCMFrameCount);

  if (channels != 1) return error("Only mono wav files are supported\n");

  #ifndef _WIN32
  // Check valid ip address
    struct hostent *server = gethostbyname(ipaddr);
    if (server == NULL) return error("Enter a valid ip addreess\n");
  #endif

  sock = unetsocket_open(ipaddr, port);
  if (sock == NULL) return error("Couldn't open unet socket");

  // Get bb.dacrate parameter
  float txsamplingfreq = 0;
  if (unetsocket_ext_fget(sock, -1, "org.arl.unet.Services.BASEBAND", "dacrate", &txsamplingfreq) < 0) {
    return error("Failed to get dacrate parameter \n");
  }

  printf("UnetStack [%s:%d] : bb.dacrate=%d \n", ipaddr, port, (int)txsamplingfreq);

  if ((unsigned int)txsamplingfreq != sampleRate) printf("Wavfile samplerate and bb.dacrate must match! %d vs %d \n", (int)txsamplingfreq, sampleRate);

  rv = unetsocket_ext_tx_signal(sock, pSampleData, (int)totalPCMFrameCount, 0, NULL);
  if (rv == 0) {
    printf("Transmitted %llu samples\n", totalPCMFrameCount);
    return 0;
  } else {
    return error("Failed to transmit signal\n");
  }

  drwav_free(pSampleData, NULL);
  unetsocket_close(sock);
}
