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

	// send data
	rv = unetsocket_send(sock_tx, data, 7, 0, DATA);
	test_assert("unetsocket_send", rv == 0);

	// bind to protocol
	if (unetsocket_bind(sock_rx, 0) == 0 && unetsocket_bind(sock_rx, 33) == 0 && unetsocket_bind(sock_rx, 10) == -1) {
		rv = unetsocket_bind(sock_rx, -1);
		test_assert("unetsocket_bind", rv == -1);
	}

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
	rv = unetsocket_connect(sock_tx, 31, 33);
	test_assert("unetsocket_connect", rv == 0);
	rv = unetsocket_get_remote_address(sock_tx);
	test_assert("unetsocket_get_remote_address", rv == 31);
	rv = unetsocket_get_remote_protocol(sock_tx);
	test_assert("unetsocket_get_local_protocol", rv == 33);

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

	unetsocket_send(sock_tx, data, 7, 31, 0);

	// close the unet socket connection
	rv = unetsocket_close(sock_tx);
	rv = unetsocket_close(sock_rx);
	test_assert("unetsocket_close", rv == 0);

	// check if closed
	rv = unetsocket_is_closed(sock_tx);
	test_assert("unetsocket_is_closed", rv == 0);

	test_summary();

	return 0;
}
