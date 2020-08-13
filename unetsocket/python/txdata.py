#///////////////////////////////////////////////////////////////////////////////
#//
#// Script to transmit data.
#//
#// In terminal window (an example):
#//
#// $ txdata.py <ip_address> <rx_node_address> [port]
#//
#////////////////////////////////////////////////////////////////////////////////

from unetpy import *
import sys

port = 1100
ip_address = 'localhost'
node_address = 0
data = [1,2,3,4,5,6,7]

if (len(sys.argv) < 3):
	print("Usage : txdata <ip_address> <rx_node_address> <port> \n"
	      "ip_address: IP address of the transmitter modem. \n"
	      "rx_node_address: Node address of the receiver modem. \n"
	      "port: port number of transmitter modem. \n"
	      "A usage example: \n"
	      "txdata.py 192.168.1.20 5 1100\n");
	sys.exit();

ip_address = sys.argv[1]
node_address = sys.argv[2]

if (len(sys.argv) > 3):
	port = int(sys.argv[3])

print("Connecting to " + ip_address + ":" + str(port));
sock = UnetSocket(ip_address, port)
if ( sock == None ):
	print("Couldn't open unet socket");
	sys.exit();

# Transmit data
print("Transmitting " + str(len(data)) + " bytes of data to " + str(node_address));
sock.send(data, node_address, Protocol.DATA);

# Close the unet socket
sock.close()

print("Transmission Complete");
