///////////////////////////////////////////////////////////////////////////////
// In terminal window (an example):
//
// > make test IP=localhost
//
// Set the actual IP address address of the modem when using modem.
////////////////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <math.h>
#include "unet.h"


static void txcb(const char* id, modem_packet_t type, long time) {
  printf("TxFrameNtf type:%d time:%ld id:%s\n", type, time, id);
}

static int error(const char* msg) {
  printf("\n*** ERROR: %s\n\n", msg);
  return -1;
}

static int tx(modem_t modem, int to, void* data, int nbytes, modem_packet_t type, char* id) {
  int x = modem_tx_data(modem, to, data, nbytes, type, id);
  if (x != 0) return -1;
  return 0;
}

static int tx_signal(modem_t modem, float* signal, int nsamples, float fc, char* id) {
  int x = modem_tx_signal(modem, signal, nsamples, fc, id);
  if (x != 0) return -1;
  return 0;
}

int main(int argc, char* argv[]) {
  char id[FRAME_ID_LEN];
  int data[7] = {1,2,3,4,5,6,7};
  int addressofDestination = (int)argv[2];
  float basebandsignal[20000] = {[0 ... 19999] = 1};

  // Open a connection to modem
  modem_t modem = modem_open_eth(argv[1], 1100);
  if (modem == NULL) return error("Couldn't open modem");
  modem_set_tx_callback(modem, txcb);

  /******************** Transmission of CONTROL and DATA Packet ******************/

  // Transmit a CONTROL frame
  tx(modem, addressofDestination, data, 7, 1, id);
  sleep(3);

  // Transmit a DATA frame
  tx(modem, addressofDestination, data, 7, 2, id);
  sleep(3);

  /******************** Transmission of Baseband Signal ****************************/

  // Transmit a baseband signal
  tx_signal(modem, basebandsignal, 10000, 12000, id);
  sleep(3);

  modem_close(modem);

  return 0;
}




