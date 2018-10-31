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
