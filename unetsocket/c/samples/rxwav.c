///////////////////////////////////////////////////////////////////////////////
//
// Record the received data to a WAV file
//
// In terminal window (an example):
//
// $ make samples
// $ ./rxwav <ip_address> <filename> <length> [port]
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
  float length = 0;
  char *fname;
  int port = 1100;
  int rv;
  if (argc <= 3) {
    return printf("Usage : rxwav <ip_address> <file.wav> <length> [port]\n"
      "ip_address: IP address of the transmitter modem. \n"
      "file.wav: Passband audio file to transmit. \n"
      "length: length of the recording in seconds. \n"
      "port: port number of transmitter modem. \n"
      "A usage example: \n"
      "txdata 192.168.1.20 passband.wav 1100\n");
  } else {
    ipaddr = argv[1];
    fname = argv[2];
    length = (float)strtof(argv[3], NULL);
    if (argc > 4) port = (int)strtol(argv[4], NULL, 10);
  }

  #ifndef _WIN32
  // Check valid ip address
    struct hostent *server = gethostbyname(ipaddr);
    if (server == NULL) return error("Enter a valid ip addreess\n");
  #endif

  sock = unetsocket_open(ipaddr, port);
  if (sock == NULL) return error("Couldn't open unet socket");

  // Get bb.dacrate parameter
  float rxsamplingfreq = 0;
  if (unetsocket_ext_fget(sock, -1, "org.arl.unet.Services.BASEBAND", "adcrate", &rxsamplingfreq) < 0) {
    return error("Failed to get dacrate parameter \n");
  }

  printf("UnetStack [%s:%d] : bb.adcrate=%d \n", ipaddr, port, (int)rxsamplingfreq);

  int nsamples = (int)(length * rxsamplingfreq);
  float* buf = malloc((unsigned long)nsamples*sizeof(float));
  printf("Recording %d samples [%fs] to %s \n", nsamples, length, fname);

  rv = unetsocket_ext_pbrecord(sock, buf, nsamples);
  if (rv == 0) {
    int16_t* pSamples = malloc((unsigned long)nsamples*sizeof(int16_t));
    for (int i = 0; i < nsamples; i++) pSamples[i] = (int16_t)(buf[i] * 32767);
    drwav wav;
    drwav_data_format format;
    format.container = drwav_container_riff;     // <-- drwav_container_riff = normal WAV files, drwav_container_w64 = Sony Wave64.
    format.format = DR_WAVE_FORMAT_PCM;          // <-- Any of the DR_WAVE_FORMAT_* codes.
    format.channels = 1;
    format.sampleRate = (unsigned int)rxsamplingfreq;
    format.bitsPerSample = 16;
    printf("Writing to file [%s]\n", fname);
    drwav_init_file_write(&wav, fname, &format, NULL);
    drwav_uint64 framesWritten = drwav_write_pcm_frames(&wav, (drwav_uint64) nsamples, pSamples);
    printf("Created WAV file [%s] with %d samples\n", fname, (int)framesWritten);
    drwav_uninit(&wav);
  } else {
    free(buf);
    return error("Error recording signal");
  }
}
