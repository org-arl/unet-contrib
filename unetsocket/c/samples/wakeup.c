///////////////////////////////////////////////////////////////////////////////
//
// Wakeup a modem over ethernet (WOL).
//
// In terminal window (an example):
//
// $ make samples
// $ ./wakeup <device_MAC_address>
//
// Pass the actual MAC address of a sleeping modem to wake it up.
////////////////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <ctype.h>
#include "../unet.h"
#include "../unet_ext.h"

#define MAC_ADDR_MAX       6
#define CONVERT_BASE       16

unsigned char mac_addr[MAC_ADDR_MAX];

int packMacAddr(char *, unsigned char *);

static int error(const char *msg)
{
  printf("\n*** ERROR: %s\n\n", msg);
  return -1;
}

static int isValidMacAddress(const char* mac) {
  int i = 0;
  int s = 0;
  while (*mac) {
     if (isxdigit(*mac)) {
        i++;
     }
     else if (*mac == ':' || *mac == '-') {
        if (i == 0 || i / 2 - 1 != s)
          break;
        ++s;
     }
     else {
        s = -1;
     }
     ++mac;
  }
  return (i == 12 && (s == 5 || s == 0));
}

int main(int argc, char *argv[])
{
  int ret = 0;
  if (argc < 2)
  {
    error("Usage : ./wakeup <device_MAC_address> \n"
          "device_MAC_address: Hardware MAC address of the sleeping modem. \n"
          "A usage example: \n"
          "wakeup 00:14:2D:2F:C0:9A \n");
    return -1;
  }

  if (isValidMacAddress(argv[1]) == 0) {
    error("Enter valid MAC address. \n");
    return -1;
  }

  char *addr = argv[1];
  ret = packMacAddr(addr, mac_addr);
  if (ret < 0) return ret;
  ret = unetsocket_ext_ethernet_wakeup(mac_addr);
  if (ret < 0) return ret;
  printf("Wakeup packet sent.\n");
  return 0;
}


int packMacAddr(char *mac, unsigned char *packedMac )
{
  char *delimiter = (char *) ":";
  char *tok;

  tok = strtok( mac, delimiter );
  for( int i = 0; i < MAC_ADDR_MAX; i++ )
  {
    if( tok == NULL ) return -1;
    packedMac[i] = (unsigned char) strtol( tok, NULL, CONVERT_BASE );
    tok = strtok( NULL, delimiter );
  }
  return 0;
}
