///////////////////////////////////////////////////////////////////////////////
//
// Execute any valid command on shell.
//
// In terminal window (an example):
//
// $ make samples
// $ ./execcmd <ip_address> <cmd> [port]
//
////////////////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>
#include "../unet.h"

#ifndef _WIN32
#include <unistd.h>
#include <netdb.h>
#include <sys/time.h>
#endif

static int error(const char *msg) {
  printf("\n*** ERROR: %s\n\n", msg);
  return -1;
}

int main(int argc, char *argv[]) {
   unetsocket_t sock;
   char* cmd = NULL;
   int port = 1100;
   if (argc <= 2) {
      error("Usage : execcmd <ip_address> <cmd> [port] \n"
        "ip_address: IP address of the transmitter modem. \n"
        "cmd: Command to execute on modem's shell. \n"
        "port: port number of the modem. \n"
        "A usage example: \n"
        "execcmd 192.168.1.20 ps 1100\n");
      return -1;
   } else {
      cmd = argv[2];
      if (argc > 3) port = (int)strtol(argv[3], NULL, 10);
   }   
#ifndef _WIN32
// Check valid ip address
   struct hostent *server = gethostbyname(argv[1]);
   if (server == NULL) {
      error("Enter a valid ip addreess\n");
      return -1;
   }
#endif
   // Open a unet socket connection to modem
  printf("Connecting to %s:%d\n",argv[1],port);

  sock = unetsocket_open(argv[1], port);
  if (sock == NULL) return error("Couldn't open unet socket");

  fjage_gw_t gw = unetsocket_get_gateway(sock);

  fjage_aid_t aid = unetsocket_agent_for_service(sock, "org.arl.fjage.shell.Services.SHELL");
  if (aid == NULL) {
     printf("Could not find SHELL agent\n");
     unetsocket_close(sock);
     return -1;
  }
  fjage_msg_t msg = fjage_msg_create("org.arl.fjage.shell.ShellExecReq", FJAGE_REQUEST);
  fjage_msg_set_recipient(msg, aid);
  fjage_msg_add_string(msg, "cmd", cmd);
  fjage_msg_t rsp = fjage_request(gw, msg, 1000);
  if (rsp != NULL && fjage_msg_get_performative(rsp) == FJAGE_AGREE) printf("SUCCESS\n");
  else printf("FAILURE\n");
  if (rsp != NULL) fjage_msg_destroy(rsp);
  fjage_aid_destroy(aid);

  // Close the unet socket
  unetsocket_close(sock);

  return 0;

}
