/*
 * exec : execute a command on a node and print the output
 *
 * Usage: exec [-i ipaddress] [-p port] [command]
 *
 * Example : exec "plvl 42"
 *           exec -p 1101 -i localhost "plvl 42"
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
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

    // Get the command to execute
    const char *cmd = optind < argc ? argv[optind] : "";

    unetsocket_t sock = unetsocket_open(ip, port);
    if (sock == NULL) return error("Couldn't open unet socket");

    fjage_gw_t gw = unetsocket_get_gateway(sock);

    fjage_aid_t shell_aid = unetsocket_agent_for_service(sock, "org.arl.fjage.shell.Services.SHELL");
    if (shell_aid == NULL) {
        unetsocket_close(sock);
        return error("Could not find an agent which implements the SHELL service");
    }
    fjage_msg_t msg = fjage_msg_create("org.arl.fjage.shell.ShellExecReq", FJAGE_REQUEST);
    fjage_msg_set_recipient(msg, shell_aid);
    fjage_msg_add_string(msg, "cmd", cmd);
    fjage_msg_add_bool(msg, "ans", true);
    fjage_msg_t rsp = fjage_request(gw, msg, 1000);
    bool success = false;
    if (rsp != NULL && fjage_msg_get_performative(rsp) == FJAGE_AGREE) {
        success = true;
        printf("%s", fjage_msg_get_string(rsp, "ans"));
    }else {
        error("Command execution failed");
    }
    if (rsp != NULL) fjage_msg_destroy(rsp);
    fjage_aid_destroy(shell_aid);

    // Close the unet socket
    unetsocket_close(sock);
    if (!success) return -1;
    return 0;
}
