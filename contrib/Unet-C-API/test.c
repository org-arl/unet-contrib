/******************************************************************************
Copyright (c) 2018, Prasad Anjangi
This file is released under Simplified BSD License.
Go to http://www.opensource.org/licenses/BSD-3-Clause for full license details.
******************************************************************************/

///////////////////////////////////////////////////////////////////////////////
//
// To run tests, power on two modems, and connect to one of the modems and then
// run the tests as following:
//
// In terminal window (an example):
//
// > make
// > ./test <IP> <PORT> <NODE_ID>
//
// Pass the actual IP address, port number & destination address of the modems above.
////////////////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <math.h>
#include "unet.h"

#define FREQ          25000
#define SAMPFREQ      8*FREQ
#define SIGLEN        2000
#define RECLEN        2000
#define M_PI          3.14159265358979323846264338327

static int passed = 0;
static int failed = 0;

static void test_assert(const char* name, int pass) {
  if (pass) {
    printf("%s: PASSED\n", name);
    passed++;
  } else {
    printf("%s: FAILED\n", name);
    failed++;
  }
}

static void test_summary(void) {
  printf("\n*** %d test(s) PASSED, %d test(s) FAILED ***\n\n", passed, failed);
}

static int error(const char* msg) {
  printf("\n*** ERROR: %s\n\n", msg);
  return -1;
}

static void rxcb(int from, int to, modem_packet_t type, void* data, int nbytes, long time) {
  printf("RxFrameNtf from:%d to:%d type:%d time:%ld (%d bytes)\n", from, to, type, time, nbytes);
}

static void txcb(const char* id, modem_packet_t type, long time) {
  printf("TxFrameNtf type:%d time:%ld id:%s\n", type, time, id);
}

int main(int argc, char* argv[]) {
  int x;
  int addressofDestination = 0;
  char * host = "localhost";
  int port = 1100;
  char id[FRAME_ID_LEN];
  float buf[RECLEN*2];
  uint8_t data[7] = {1,2,3,4,5,6,7};
  float basebandsignal[SIGLEN] = {[0 ... SIGLEN-1] = 1};
  float passbandsignal[SIGLEN];
  float fs = SAMPFREQ;
  float f = FREQ;

  if (argc >= 2){
    host = argv[1];
  }

  if (argc >= 3){
    port = strtol(argv[2], NULL, 10);
    if (port > 65535) port = 1100;
  }

  if (argc == 4){
    addressofDestination = strtol(argv[3], NULL, 10);
  }

  printf("Connecting to UnetStack on %s:%d\n", host, port);
  printf("Testing ranging with Node : %d\n\n", addressofDestination);

  for(int i = 0; i < SIGLEN; i++) {
    passbandsignal[i] = sin(f * (2 * M_PI) * (i / fs));
  }
  float* range = calloc(1,sizeof(float));
  int* intval = calloc(1,sizeof(int));
  bool* boolval = calloc(1,sizeof(bool));
  float* floatval = calloc(1,sizeof(float));
  char stringval[3];

  // Open a connection to modem
  modem_t modem = modem_open_eth(host, port);
  if (modem == NULL) return error("Couldn't open modem");
  modem_set_rx_callback(modem, rxcb);
  modem_set_tx_callback(modem, txcb);
  sleep(1);

  // Test packet transmission of different types
  for (int i = 1; i <= 3 ; i++) {
    x = modem_tx_data(modem, addressofDestination, data, 7, i, id);
    if (x == 0) printf("TX: %s\n", id);
    test_assert("Packet transmission", x == 0);
    sleep(3);
  }

  // Test transmission of signals
  for (int i = 1; i <= 3; i++) {
    x = modem_tx_signal(modem, basebandsignal, SIGLEN/2, f, id);
    if (x == 0) printf("TX: %s\n", id);
    test_assert("Baseband signal transmission", x == 0);
    sleep(3);
  }

  for (int i = 1; i <= 3; i++) {
    x = modem_tx_signal(modem, passbandsignal, SIGLEN, 0, id);
    if (x == 0) printf("TX: %s\n", id);
    test_assert("Passband signal transmission", x == 0);
    sleep(3);
  }

  sleep(3);

  // Test ranging
  x = modem_get_range(modem, addressofDestination, range);
  if (x == 0) printf("Range measured is : %f \n", *range);
  test_assert("Ranging", x == 0);

  sleep(3);

  // Test recording
  x = modem_record(modem, buf, RECLEN);
  if (x == 0) {
    for(int i = 0; i < RECLEN*2; i++) {
      printf("%f\n", buf[i]);
    }
  }
  test_assert("\nRecording", x == 0);

  // Test setter and getter for integer valued modem parameter
  if (modem_iset(modem, 2, "org.arl.unet.Services.PHYSICAL", "nc", 256)==0) printf("Integer parameter is set.\n");
  sleep(1);
  if (modem_iget(modem, 2, "org.arl.unet.Services.PHYSICAL", "nc", intval)==0) printf("Integer parameter is read.\n");
  test_assert("Integer valued parameter setting", *intval == 256);

  // Test setter and getter for boolean valued modem parameter
  if (modem_bset(modem, 0, "org.arl.unet.Services.PHYSICAL", "rxEnable", true)==0) printf("Boolean parameter is set.\n");
  sleep(1);
  if (modem_bget(modem, 0, "org.arl.unet.Services.PHYSICAL", "rxEnable", boolval)==0) printf("Boolean parameter is read.\n");
  test_assert("Boolean valued parameter setting", *boolval == true);

  // Test setter and getter for floating point valued modem parameter
  if (modem_fset(modem, 1, "org.arl.unet.Services.PHYSICAL", "fstep", 384.0)==0) printf("Float parameter is set.\n");
  sleep(1);
  if (modem_fget(modem, 1, "org.arl.unet.Services.PHYSICAL", "fstep", floatval)==0) printf("Float parameter is read.\n");
  test_assert("Float valued parameter setting", *floatval == 384.0);

  // Test setter and getter for string valued modem parameter
  test_assert("String valued parameter setting", modem_sset(modem, 0, "org.arl.unet.Services.LINK", "mac", "mac")==0);
  sleep(1);
  if (modem_sget(modem, 0, "org.arl.unet.Services.LINK", "mac",stringval, 3)==0) printf("The value of stringval is %s\n", stringval);
  test_assert("String valued parameter getting", strcmp(stringval, "mac") == 0);

  test_summary();

  modem_close(modem);

  return 0;
}
