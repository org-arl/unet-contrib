///////////////////////////////////////////////////////////////////////////////
//
// Script to wakeup a modem over ethernet (WOL).
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
#include "../unet.h"

#define MAC_ADDR_MAX       6
#define CONVERT_BASE       16

unsigned char mac_addr[MAC_ADDR_MAX];

int packMacAddr(char *, unsigned char *);

static int error(const char *msg)
{
    printf("\n*** ERROR: %s\n\n", msg);
    return -1;
}

int main(int argc, char *argv[])
{
    int ret = 0;
    if (argc <= 1)
    {
        error("Usage : ./wakeup <device_MAC_address> \n"
              "device_MAC_address: Hardware MAC address of the sleeping modem. \n"
              "A usage example: \n"
              "wakeup 00:14:2D:2F:C0:9A \n");
        return -1;
    }

    char *addr = argv[1];
    ret = packMacAddr(addr, mac_addr);
    if (ret < 0) return ret;
    ret = modem_ethernet_wakeup(mac_addr);
    if (ret < 0) return ret;
}


int packMacAddr(char *mac, unsigned char *packedMac )
{

    char *delimiter = (char *) ":";
    char *tok;

    tok = strtok( mac, delimiter );
    for( int i = 0; i < MAC_ADDR_MAX; i++ )
    {
        if( tok == NULL ) return -1;
        mac_addr[i] = (unsigned char) strtol( tok, NULL, CONVERT_BASE );
        tok = strtok( NULL, delimiter );
    }
    return 0;
}
