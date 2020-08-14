#///////////////////////////////////////////////////////////////////////////////
#//
#// Script to transmit data.
#//
#// In terminal window (an example):
#//
#// $ rxdata.py <ip_address> [port]
#//
#////////////////////////////////////////////////////////////////////////////////

from unetpy import *
import sys

port = 1100
ip_address = 'localhost'

if (len(sys.argv) < 2):
	print("Usage : rxdata <ip_address> <rx_node_address> <port> \n"
	      "ip_address: IP address of the transmitter modem. \n"
	      "port: port number of transmitter modem. \n"
	      "A usage example: \n"
	      "rxdata.py 192.168.1.20 1100\n");
	sys.exit();

ip_address = sys.argv[1]

if (len(sys.argv) > 2):
	port = int(sys.argv[2])

print("Connecting to " + ip_address + ":" + str(port));
sock = UnetSocket(ip_address, port)
if ( sock == None ):
	print("Couldn't open unet socket");
	sys.exit();

# Bind to protocol DATA
if (not sock.bind(Protocol.DATA)):
	print("Couldn't open bind to socket");
	sys.exit();

# Set a timeout of 10 seconds
sock.setTimeout(10000);

# Receive and display data
print("Waiting for a Datagram");

ntf = sock.receive()

if (ntf != None):
	print(ntf)
else:
	print("Error receiving data")

# Close the unet socket
sock.close()

print("Reception Complete");
