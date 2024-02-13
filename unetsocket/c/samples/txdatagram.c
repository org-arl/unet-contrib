/*
 * txdatagram : send a datagram to a remote host
 *
 * Usage: txdatagram [-i ipaddress] [-p port]
 *
 * Example : txdatagram
 *           txdatagram -p 1101
 *           txdatagram -p 1101 -i localhost
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include "../unet.h"

#ifndef _WIN32
#include <unistd.h>
#include <netdb.h>
#include <sys/time.h>
#endif

static int error(const char *msg) {
  fprintf(stderr, "\n*** ERROR: %s\n", msg);
  exit(EXIT_FAILURE);
}

void parse_args(int argc, char *argv[], char **ip, int *port) {
    int opt;
    while ((opt = getopt(argc, argv, "p:i:")) != -1) {
        switch (opt) {
        case 'p':
            *port = atoi(optarg);
            break;
        case 'i':
            *ip = optarg;
            break;
        default: /* '?' */
            fprintf(stderr, "Usage: %s [-i ipaddress] [-p port] [command]\n", argv[0]);
            exit(EXIT_FAILURE);
        }
    }
}

int main(int argc, char *argv[]) {

    // Parse the command line arguments (IP/Port)
    char *ip = "localhost";
    int port = 1101;
    parse_args(argc, argv, &ip, &port);

    /******** Parameters ********/
    // Set the values request parameters here!
    const char *agent = NULL;               // The name (AgentID) of the agent to send the request to. (Use either agent or service, not both)
    const char *service = NULL;             // The name of the service to send the request to. (Use either agent or service, not both)
    int protocol = 0;                       // The protocol to use [0, 32-64]
    bool reliability = true;                // If true, an acknowledgment is requested
    uint node = 31;                         // The destination node [0-256] (0=broadcast)
    uint8_t data[4] = {1, 2, 3, 4};         // The data to send
    uint data_len = 4;                      // The length of the data to send
    int timeout = 30000;                    // The timeout in milliseconds, for waiting for an acknowledgment (if reliability is requested)

    // Check if the parameters are valid
    if (node == 0 && reliability) return error("Reliability is not supported for broadcast (node=0) datagrams");

    // Open unet socket
    unetsocket_t sock = unetsocket_open(ip, port);
    if (sock == NULL) return error("Couldn't open unet socket");

    // If the agent string has dots, then it's a service name, otherwise it's an agent name
    fjage_aid_t aid = NULL;
    if (service != NULL) {
        aid = unetsocket_agent_for_service(sock, agent);
        if (aid == NULL) {
            unetsocket_close(sock);
            char msg[100];
            sprintf(msg, "Could not find an agent which advertises the service %s.\n", agent);
            return error(msg);
        }
    } else if (agent != NULL) {
        aid = fjage_aid_create(agent);
    } else {
        // Get any agent that advertises the DATAGRAM service
        aid = unetsocket_agent_for_service(sock, "org.arl.unet.Services.DATAGRAM");
    }

    // Send the datagram
    fjage_msg_t msg = fjage_msg_create("org.arl.unet.DatagramReq", FJAGE_REQUEST);
    fjage_msg_set_recipient(msg, aid);
    fjage_msg_add_byte_array(msg, "data",data, (int)data_len);
    fjage_msg_add_int(msg, "to", (int)node);
    fjage_msg_add_int(msg, "protocol", protocol);
    fjage_msg_add_bool(msg, "reliability", reliability);
    int rv = unetsocket_send_request(sock, msg);
    if (reliability){
        if (rv < 0) return error("Failed to send the datagram");
        unetsocket_set_timeout(sock, timeout);
        fjage_msg_t rsp = unetsocket_receive(sock);
        if (rsp == NULL) return error("Failed to receive an acknowledgment");
        const char *rsp_type = fjage_msg_get_clazz(rsp);
        rv = strcmp(rsp_type, "org.arl.unet.DatagramDeliveryNtf") == 0 ? 0 : -1;
    }
    fjage_aid_destroy(aid);
    unetsocket_close(sock);
    return rv;
}
