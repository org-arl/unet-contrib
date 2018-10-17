##############################################################################
# In order to run this example script, you need `unetpy` module installed. If
# not already installed, install it by running the following command in termi-
# nal window `pip3 install unetpy`
#
# In terminal window (an example):
#
# > python3 examples.py
#
# Set the actual IP address address of the modem in the code below.
##############################################################################

import numpy as np
import arlpy as ap
from unetpy import *

# Set the IP address of the modem
ip_address = 'localhost'
# Open a connection to modem
modem = UnetGateway(ip_address, 1100)

################### Transmission of CONTROL and DATA Packet ###################

# Subscribe to receive physical agent notification
phy = modem.agentForService(Services.PHYSICAL)

# Transmit a CONTROL packet
phy << TxFrameReq(type=Physical.CONTROL, data=np.arange(4))
# Receive the transmit notification
txntf1 = modem.receive(TxFrameNtf, 5000)
if txntf1 is not None:
    # Extract the transmission start time
    txtime = txntf1.txTime
    print('The transmission started at ' + str(txtime))
else:
    print('Transmission not successfull, try again!')

# Transmit a DATA packet
phy << TxFrameReq(type=Physical.DATA, data=np.arange(10))
# Receive the transmit notification
txntf2 = modem.receive(TxFrameNtf, 5000)
if txntf2 is not None:
    # Extract the transmission start time
    txtime = txntf2.txTime
    print('The transmission started at ' + str(txtime))
else:
    print('Transmission not successfull, try again!')

################### Transmission of Baseband Signal ##########################

# Subscribe to receive baseband agent notification
bb = modem.agentForService(Services.BASEBAND)

# Generate a random signal modulated with BPSK
signal = ap.comms.modulate(ap.comms.random_data(10000), ap.comms.psk())
# Transmit the baseband signal
bb << TxBasebandSignalReq(preamble=3, signal=signal)
# Receive the transmit notification
txntf3 = modem.receive(TxFrameNtf, 5000)
if txntf3 is not None:
    # Extract the transmission start time
    txtime = txntf3.txTime
    print('The transmission started at ' + str(txtime))
else:
    print('Transmission not successfull, try again!')

modem.close()
