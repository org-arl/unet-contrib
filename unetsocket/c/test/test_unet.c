#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <math.h>
#include <netdb.h>
#include "../unet.h"

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
	int port_tx = 1101;
	int port_rx = 1102;
	uint8_t data[7] = {1,2,3,4,5,6,7};
	unetsocket_t sock_tx;
	unetsocket_t sock_rx;

	// create a unet socket connection to modems
	sock_tx = unetsocket_open(argv[1], port_tx);
	sock_rx = unetsocket_open(argv[1], port_rx);

	test_assert("unetsocket_open", sock_tx != NULL);
	if (sock_tx == NULL) return error("Couldn't open unet socket");

	// get local node address
	rv = unetsocket_get_local_address(sock_tx);
	test_assert("unetsocket_get_local_address", rv >= 0);

	// send data
	rv = unetsocket_send(sock_tx, data, 7, 0, DATA);
	test_assert("unetsocket_send", rv == 0);

	// bind to protocol
	unetsocket_bind(sock_rx, DATA);
	unetsocket_send(sock_tx, data, 7, 0, DATA);
	printf("%s\n", unetsocket_receive(sock_rx));

	// close the unet socket connection
	rv = unetsocket_close(sock_tx);
	test_assert("unetsocket_close", rv == 0);

	// check if closed
	rv = unetsocket_is_closed(sock_tx);
	test_assert("unetsocket_is_closed", rv == 0);

	test_summary();

	return 0;
}
