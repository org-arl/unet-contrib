///////////////////////////////////////////////////////////////////////////////
//
// Script to transmit data.
//
// In terminal window (an example):
//
// $ make samples
// $ ./txdata <ip_address> <peer_node_address> <port>
//
////////////////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>

#ifdef _WIN32
#pragma comment(lib, "ws2_32.lib")
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#include <io.h>
#include <winsock2.h>
#include <Ws2tcpip.h>
#else
#include <unistd.h>
#include <netdb.h>
#include <sys/time.h>
#endif

#include "../unet.h"

static int error(const char *msg)
{
    printf("\n*** ERROR: %s\n\n", msg);
    return -1;
}

int main(int argc, char *argv[])
{
  unetsocket_t sock;
	int address = 0;
  int port = 1100;
	uint8_t data[7] = {1, 2, 3, 4, 5, 6, 7};
	if (argc < 2)
    {
        error("Usage : txdata <ip_address> <peer_node_address> <port> \n"
              "ip_address: IP address of the transmitter modem. \n"
              "peer_node_address: Node address of the receiver modem. Set this to 0 for broadcast. \n"
              "port: port number \n"
              "A usage example: \n"
              "txdata 192.168.1.20 5\n");
        return -1;
    }
    if (argc > 2)
    {
        address = (int)strtol(argv[2], NULL, 10);
        if (argc > 3) port = (int)strtol(argv[3], NULL, 10);
    }

    // Check valid ip address
    struct hostent *server = gethostbyname(argv[1]);
    if (server == NULL) {
      error("Enter a valid ip addreess\n");
      return -1;
    }

    // Open a unet socket connection to modem
    sock = unetsocket_open(argv[1], port);
    if (sock == NULL) return error("Couldn't open unet socket");

    // Transmit data
    unetsocket_send(sock, data, 7, address, DATA);

    sleep(2);
    // Close the unet socket
    unetsocket_close(sock);

    return 0;

}