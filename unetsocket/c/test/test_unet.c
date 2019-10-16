///////////////////////////////////////////////////////////////////////////////
//
// Steps to setup the test environment:
//
// 1. To run tests, run the unet simulator with two nodes
//    (or)
//    Power on two modems and set them up in water
//
// 2. Make sure a ethernet connection to one of the modem is available
//
// 3. Run the tests as following:
//
// In terminal window (an example):
//
// $ make test
// $ test/test_unet <ip_address> <peer_node_address> <port>
//
// NOTE: To run the simulator with 2 nodes, download the unet community edition
// from https://unetstack.net/ and run the following:
//
// >> bin/unet samples/2-node-network.groovy
//
// For more details on using the unet simulator to deploy 2 node network, follow
// the link below:
//
// https://unetstack.net/handbook/unet-handbook_getting_started.html
////////////////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <math.h>
#include <netdb.h>
#include "../unet.h"

#define SIGLEN     2000
#define RECBUFSIZE 17000

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
  printf("\n");
  int x;
  int port = 1100;
  char id[FRAME_ID_LEN];
  float buf[SIGLEN];
  float recbuf[RECBUFSIZE];
  int data[7] = {1,2,3,4,5,6,7};
  float basebandsignal[SIGLEN] = {[0 ... SIGLEN-1] = 1};
  float passbandsignal[SIGLEN];
  float fs = 192000;
  float f = 24000;
  int npulses = 5;
  uint64_t txsamples[npulses];
  int pri = 25;
  for(int i = 0; i < SIGLEN; i++) {
    passbandsignal[i] = sin(f * (2 * M_PI) * (i / fs));
  }
  float* range = calloc(1,sizeof(float));
  int* intval = calloc(1,sizeof(int));
  bool* boolval = calloc(1,sizeof(bool));
  float* floatval = calloc(1,sizeof(float));
  char stringval[5];
  modem_t modem;

  if (argc < 3) {
    error("Usage : test_unet <ip_address> <peer_node_address> <port> \n"
          "ip_address: IP address of the transmitter modem. \n"
          "peer_node_address: Node address of the receiver modem. Set this to 0 for broadcast. \n"
          "port: port number of the Unet service on the modem (default value used is 1100)"
          "A usage example: \n"
          "test_unet 192.168.1.20 231 1101\n");
    return -1;
  }

  if (argc > 3) {
    port = (int)strtol(argv[3], NULL, 10);
  }

  struct hostent *server = gethostbyname(argv[1]);
  if (server == NULL) {
    error("Enter a valid ip addreess\n");
    return -1;
  }

  int addressofDestination = (int)strtol(argv[2], NULL, 10);

  // Open a connection to modem
  modem = modem_open_eth(argv[1], port);
  if (modem == NULL) return error("Couldn't open modem");

  // register callbacks
  modem_set_rx_callback(modem, rxcb);
  modem_set_tx_callback(modem, txcb);
  sleep(1);

  // Test packet transmission of different types
  for (int i = 1; i <= 2 ; i++) {
    x = modem_tx_data(modem, addressofDestination, data, 7, i, id);
    if (x == 0) printf("TX: %s\n", id);
    test_assert("Packet transmission", x == 0);
    sleep(3);
  }

  // Test transmission of signals
  for (int i = 1; i <= 2; i++) {
    x = modem_tx_signal(modem, basebandsignal, 1000, 192000 ,24000, id);
    if (x == 0) printf("TX: %s\n", id);
    test_assert("Baseband signal transmission", x == 0);
    sleep(3);
  }

  for (int i = 1; i <= 2; i++) {
    x = modem_tx_signal(modem, passbandsignal, 2000, 192000, 0, id);
    if (x == 0) printf("TX: %s\n", id);
    test_assert("Passband signal transmission", x == 0);
    sleep(3);
  }

  sleep(3);

  // Test ranging
  x = modem_get_range(modem, addressofDestination, range);
  if (x == 0) printf("Range measured is : %f \n", *range);
  test_assert("Ranging", x == 0);

  // Test recording
  x = modem_record(modem, buf, 1000);
  test_assert("\nRecording", x == 0);

  // Test setter and getter for integer valued modem parameter
  if (modem_iset(modem, 2, "org.arl.unet.Services.PHYSICAL", "fec", 0)==0) printf("Integer parameter is set.\n");
  sleep(1);
  if (modem_iget(modem, 2, "org.arl.unet.Services.PHYSICAL", "fec", intval)==0) printf("Integer parameter is read.\n");
  test_assert("Integer valued parameter setting", *intval == 0);

  // Test setter and getter for boolean valued modem parameter
  if (modem_bset(modem, 0, "org.arl.unet.Services.PHYSICAL", "rxEnable", true)==0) printf("Boolean parameter is set.\n");
  sleep(1);
  if (modem_bget(modem, 0, "org.arl.unet.Services.PHYSICAL", "rxEnable", boolval)==0) printf("Boolean parameter is read.\n");
  test_assert("Boolean valued parameter setting", *boolval == true);

  // Test setter and getter for floating point valued modem parameter
  if (modem_fset(modem, 1, "org.arl.unet.Services.PHYSICAL", "powerLevel", -10.0)==0) printf("Float parameter is set.\n");
  sleep(1);
  if (modem_fget(modem, 1, "org.arl.unet.Services.PHYSICAL", "powerLevel", floatval)==0) printf("Float parameter is read.\n");
  test_assert("Float valued parameter setting", *floatval == -10.0);

  test_summary();

  modem_close(modem);

  return 0;
}
