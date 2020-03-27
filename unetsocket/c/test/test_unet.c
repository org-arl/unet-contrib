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
// $ test/test_unet <ip_tx> <ip_rx> <port_tx> <port_rx>
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
#include <string.h>
#include <math.h>
#include "../pthreadwindows.h"
#include "../unet.h"
#ifndef _WIN32
#include <netdb.h>
#include <sys/time.h>
#endif

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

int main(int argc, char* argv[]) {
  printf("\n");
  int rv;
  int port_tx = 1100;
  int port_rx = 1100;
  uint8_t tdata[7];
  uint8_t data[7] = {1,2,3,4,5,6,7};
  fjage_msg_t ntf;
  struct hostent *server = NULL;
  unetsocket_t sock_tx;
  unetsocket_t sock_rx;
  if (argc < 5) {
    error("Usage : test_unet <ip_tx> <ip_rx> <port_tx> <port_rx> \n"
          "ip_tx: IP address of the transmitter modem. \n"
          "ip_rx: IP address of the receiver modem. \n"
          "port_tx: port number of the Unet service on the tx modem (default value used is 1100). \n"
          "port_rx: port number of the Unet service on the rx modem (default value used is 1100). \n"
          "A usage example: \n"
          "test_unet localhost localhost 1101 1102 \n"
          "(or) \n"
          "test_unet 192.168.1.10 192.168.1.20 1100 1100 \n");
    return -1;
  }
  if (argc > 4) {
    port_tx = (int)strtol(argv[3], NULL, 10);
    port_rx = (int)strtol(argv[4], NULL, 10);
  }
#ifndef _WIN32
  server = gethostbyname(argv[1]);
  if (server == NULL) {
    error("Enter a valid ip address of transmitter modem\n");
    return -1;
  }
  server = gethostbyname(argv[2]);
  if (server == NULL) {
    error("Enter a valid ip address of receiver modem\n");
    return -1;
  }
#endif
  // create a unet socket connection to modems
  sock_tx = unetsocket_open(argv[1], port_tx);
  test_assert("unetsocket_open_tx", sock_tx != NULL);
  if (sock_tx == NULL) return error("Couldn't open unet socket on transmitter");
  sock_rx = unetsocket_open(argv[2], port_rx);
  test_assert("unetsocket_open_rx", sock_rx != NULL);
  if (sock_rx == NULL) return error("Couldn't open unet socket on receiver");
  int rx_node_address = unetsocket_get_local_address(sock_rx);
  if (rx_node_address < 0) {
    error("Couldn't fetch the rx node address");
    return -1;
  }
  // send data
  rv = unetsocket_send(sock_tx, data, 7, rx_node_address, DATA);
  test_assert("unetsocket_send", rv == 0);
  // bind to protocol
  if (unetsocket_bind(sock_rx, 0) == 0 && unetsocket_bind(sock_rx, USER+1) == 0 && unetsocket_bind(sock_rx, 10) == -1) {
    rv = unetsocket_bind(sock_rx, -1);
    test_assert("unetsocket_bind", rv == -1);
  }
  else test_assert("unetsocket_bind", false);
  // unbind and check if unbound protocol
  unetsocket_unbind(sock_rx);
  rv = unetsocket_is_bound(sock_rx);
  test_assert("unetsocket_unbind", rv == -1);
  test_assert("unetsocket_is_bound", rv == -1);
  rv = unetsocket_get_local_protocol(sock_rx);
  test_assert("unetsocket_get_local_protocol", rv == -1);
  // get local address
  rv = unetsocket_get_local_address(sock_tx);
  test_assert("unetsocket_get_local_address", rv >= 0);
  // connect and protocol
  rv = unetsocket_connect(sock_tx, rx_node_address, USER+1);
  test_assert("unetsocket_connect", rv == 0);
  rv = unetsocket_is_connected(sock_tx);
  test_assert("unetsocket_is_connected", rv == 0);
  rv = unetsocket_get_remote_address(sock_tx);
  test_assert("unetsocket_get_remote_address", rv == rx_node_address);
  rv = unetsocket_get_remote_protocol(sock_tx);
  test_assert("unetsocket_get_local_protocol", rv == USER+1);
  // disconnect
  unetsocket_disconnect(sock_tx);
  rv = unetsocket_get_remote_address(sock_tx);
  test_assert("unetsocket_disconnect 1", rv == -1);
  rv = unetsocket_get_remote_protocol(sock_tx);
  test_assert("unetsocket_disconnect 2", rv == 0);
  // set and get timeout
  unetsocket_set_timeout(sock_tx, 1000);
  rv = unetsocket_get_timeout(sock_tx);
  test_assert("unetsocket_set_timeout", rv == 1000);
  unetsocket_set_timeout(sock_tx, -10);
  rv = unetsocket_get_timeout(sock_tx);
  test_assert("unetsocket_get_timeout", rv == -1);
  // receive
  unetsocket_send(sock_tx, data, 7, rx_node_address, DATA);
  unetsocket_set_timeout(sock_rx, 10000);
  ntf = unetsocket_receive(sock_rx);
  test_assert("unetsocket_receive", strcmp("org.arl.unet.DatagramNtf", fjage_msg_get_clazz(ntf))==0);
  fjage_msg_destroy(ntf);
  // // close the unet socket connection
  rv = unetsocket_close(sock_tx);
  test_assert("unetsocket_close_tx", rv == 0);
  rv = unetsocket_close(sock_rx);
  test_assert("unetsocket_close_rx", rv == 0);
  //check if closed
  rv = unetsocket_is_closed(sock_tx);
  test_assert("unetsocket_is_closed", rv == 0);
  test_summary();
  return 0;
}
