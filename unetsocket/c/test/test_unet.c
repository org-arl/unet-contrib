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
	int port = 1100;
	uint8_t data[7] = {1,2,3,4,5,6,7};
	unetsocket_t sock;

	// create a unet socket connection to modem
	sock = unetsocket_open(argv[1], port);
	test_assert("Socket open", sock != NULL);
	if (sock == NULL) return error("Couldn't open unet socket");

	// get local node address
	rv = unetsocket_get_local_address(sock);
	test_assert("Get local address", rv >= 0);

	// send data
	rv = unetsocket_send(sock, data, 7, 0, DATA);
	test_assert("Transmit data", rv == 0);

	// close the unet socket connection
	rv = unetsocket_close(sock);
	test_assert("Socket close", rv == 0);

	test_summary();

	return 0;
}
