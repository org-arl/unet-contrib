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
#
# Find relevant documentation on UnetStack and APIs at the following link:
# https://www.unetstack.net/docs.html
##############################################################################

# TODO:
# change ip_address to hostname
# Add readme


import numpy as np
import arlpy as ap
from unetpy import *

################### Open connection to modem #################################

# Set the IP address of the modem
ip_address = 'localhost'
# Open a connection to modem
modem = UnetGateway(ip_address, 1100)

################### Transmission of CONTROL and DATA packet ###################

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

################### Transmission of baseband signal ##########################

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

################### Record a baseband signal #################################

# Record a baseband signal
bb << RecordBasebandSignalReq(recLen=24000)
# Receive the notification when the signal is recorded
rxntf = modem.receive(RxBasebandSignalNtf, 5000)
if rxntf is not None:
    # Extract the recorded signal
    rec_signal = rxntf.signal
    print('Recorded signal successfully!')
    # The recorded signal saved in `rec_signal` variable
    # can be processed as required by the user.
else:
    print('Recording not successfull, try again!')

################### Close connection to modem ################################

modem.close()
