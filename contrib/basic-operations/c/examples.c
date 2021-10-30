#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <math.h>
#include "include/unet.h"

#define SIGLEN        10000
#define RECLEN        12000
#define FREQ          24000

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
  int dest_address = 9; // an example, set this to destination node address
  float signal[SIGLEN*2] = {[0 ... SIGLEN-1] = 1};
  float rec_signal[RECLEN*2];

  /******************** Open connection to modem *********************************/
  // Get the IP address of the modem from argument passed
  if (argc < 2) {
    printf("Please provide the IP address of the modem as argument!\n");
    return -1;
  }
  // Open a connection to modem
  modem_t modem = modem_open_eth(argv[1], 1100);
  if (modem == NULL) return error("Couldn't open modem");
  modem_set_tx_callback(modem, txcb);

  /******************** Transmission of CONTROL and DATA packet ******************/

  // Transmit a CONTROL frame
  tx(modem, dest_address, data, 7, CONTROL_FRAME, id);
  sleep(3);

  // Transmit a DATA frame
  tx(modem, dest_address, data, 7, DATA_FRAME, id);
  sleep(3);

  /******************** Transmission of baseband signal ****************************/

  // Transmit a baseband signal
  tx_signal(modem, signal, SIGLEN, FREQ, id);
  sleep(3);

  /******************** Record of baseband signal **********************************/

  // Recording a baseband signal
  if (modem_record(modem, rec_signal, RECLEN) < 0) {
    printf("Recording not successfull, try again!\n");
  } else {
    printf("Recorded signal successfully!\n");
    // The recorded signal saved in `rec_signal` variable
    // can be processed as required by the user.
  }

  /******************** Close connection to modem ****************************/

  modem_close(modem);

  return 0;
}




