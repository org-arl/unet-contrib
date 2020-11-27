import sys
import numpy as np
import arlpy as ap
from unetpy import *

################### Open connection to modem #################################
# Get the IP address of the modem from argument passed
if len(sys.argv) < 2:
    print("Please provide the IP address of the modem as argument!")
    sys.exit(0)

ip_address = sys.argv[1]
# Open a connection to modem
modem = UnetSocket(ip_address, 1100)

################### Transmission of CONTROL and DATA packet ###################

# Look for agents providing physical service def phy = modem.agentForService Services.PHYSICAL
phy = modem.agentForService(Services.PHYSICAL)

# Transmit a CONTROL packet (phy.CONTROL)
phy << TxFrameReq(type=1, data=np.arange(4))

# Receive the transmit notification
txntf1 = modem.receive(TxFrameNtf, 5000)
if txntf1 is not None:
    # Extract the transmission start time
    txtime = txntf1.txTime
    print('The transmission started at ' + str(txtime))
else:
    print('Transmission not successfull, try again!')

# Transmit a DATA packet (phy.DATA)
phy << TxFrameReq(type=2, data=np.arange(10))
# Receive the transmit notification
txntf2 = modem.receive(TxFrameNtf, 5000)
if txntf2 is not None:
    # Extract the transmission start time
    txtime = txntf2.txTime
    print('The transmission started at ' + str(txtime))
else:
    print('Transmission not successfull, try again!')

################### Transmission of baseband signal ##########################

# Look for agents poviding baseband service
bb = modem.agentForService(Services.BASEBAND)

# Generate a random signal modulated with BPSK
csig = ap.comms.modulate(ap.comms.random_data(10000), ap.comms.psk())
# Convert complex signal (csig) with real and imag to an array
# with alternate real and imag values
real_csig = csig.real
imag_csig = csig.imag
signal = np.empty((real_csig.size + imag_csig.size,), dtype=real_csig.dtype)
signal[0::2] = real_csig
signal[1::2] = imag_csig
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

################### Recording a baseband signal #################################

# Record a baseband signal
bb << RecordBasebandSignalReq(recLength=24000)
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

################### Transmit and record a signal ##########################

# Load the baseband signal to be transmitted.
# signal.txt contains the same signal as csig in previous section
# Format: array with alternate real and imag values
tx_signal = np.genfromtxt('signals/signal.txt', delimiter=',')

# Transmit the baseband signal
bb << TxBasebandSignalReq(preamble=3, signal=tx_signal)

txntf4 = modem.receive(TxFrameNtf, 5000)
if txntf4 is not None:
    # Request a recording from txTime onwards for a duration of 2x the original transmitted signal.
    bb << RecordBasebandSignalReq(recTime=txntf4.txTime, recLength=(len(tx_signal)*2))
else:
    print('Transmission not successfull, try again!')

# Read the receive notification
rxntf4 = modem.receive(RxBasebandSignalNtf, 5000)

if rxntf is not None:
    # Extract the recorded signal
    rec_signal = rxntf4.signal
    print('Successfully recorded signal after transmission!')
    # The recorded signal is saved in `rec_signal` variable
    # It can be processed as required by the user.
else:
    print('Recording not successfull, try again!')

################### Close connection to modem ################################

modem.close()
