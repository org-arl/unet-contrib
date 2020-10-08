#!/usr/bin/env python
#
# UDP Sensor Simulator.
# Generates random data.
#
# sensor.py [port]
#

import socket
import time
import random
import sys

UDP_IP = "127.0.0.1"
UDP_PORT = 5000
SLEEP_INTERVAL = 30

if (len(sys.argv) > 1):
	UDP_PORT = int(sys.argv[1])

print("UDP target IP: %s" % UDP_IP)
print("UDP target port: %s" % UDP_PORT)

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

while True:
	d1 = random.randrange(0, 1000)
	d2 = random.randrange(0, 1000)
	msg = '{{"d1": {}, "d2": {}}}\r\n'.format(d1, d2)
	print("Sending.. " + msg)
	sock.sendto(msg.encode(), (UDP_IP, UDP_PORT))
	time.sleep(SLEEP_INTERVAL)
	pass
