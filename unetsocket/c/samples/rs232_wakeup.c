///////////////////////////////////////////////////////////////////////////////
//
// Wakeup a modem over RS232.
//
// In terminal window (an example):
//
// $ make samples
// $ ./rs232_wakeup <dev_name>
//
// NOTE: Validity of the <dev_name> is not checked in this example code.
//
////////////////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <math.h>
#include <inttypes.h>
#include "../unet.h"
#include "../unet_ext.h"

#define ACTIVITYONSERIAL  3

static int error(const char *msg)
{
  printf("\n*** ERROR: %s\n\n", msg);
  return -1;
}

int main(int argc, char *argv[])
{
  int ret = 0;
  if (argc < 2)
  {
    error("Usage : rs232_wakeup <dev_name> \n"
          "dev_name: Device name on which serial port exists. \n"
          "A usage example: \n"
          "rs232_wakeup /dev/tty.usbserial \n");
    return -1;
  }
  for (int i = 0; i < ACTIVITYONSERIAL; i++) {
    ret = unetsocket_ext_rs232_wakeup(argv[1], 115200, "N81");
    if (ret == 0) printf("Started activity on RS232 port..\n");
  }
  printf("Wakeup packet sent.\n");
  return 0;
}
