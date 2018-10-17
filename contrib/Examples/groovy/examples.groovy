///////////////////////////////////////////////////////////////////////////////
// In terminal window (an example):
//
// > ./run.sh
//
// Set the actual IP address address of the modem in the code below.
////////////////////////////////////////////////////////////////////////////////

import org.arl.fjage.remote.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.bb.*

// Set the IP address of the modem
ip_address = 'localhost'
// Open a connection to modem
Gateway modem = new Gateway(ip_address, 1100);

/******************** Transmission of CONTROL and DATA Packet ******************/

// Subscribe to receive physical agent notification
def phy = modem.agentForService Services.PHYSICAL

// Transmit a CONTROL packet
def msg1 = new TxFrameReq(type: Physical.CONTROL, data: 'hello' as byte[])
msg1.recipient = phy
modem.send(msg1)
// Receive the transmit notification
def txntf1 = modem.receive(TxFrameNtf, 5000)
if (txntf1 != null) {
    // Extract the transmission start time
    def txtime = txntf1.txTime
    println 'The transmission started at ' + txtime.toString()
} else {
    println 'Transmission not successfull, try again!'
}

// Transmit a DATA packet
def msg2 = new TxFrameReq(type: Physical.DATA, data: 'hello' as byte[])
msg2.recipient = phy
modem.send(msg2)
// Receive the transmit notification
def txntf2 = modem.receive(TxFrameNtf, 5000)
if (txntf2 != null) {
    // Extract the transmission start time
    def txtime = txntf2.txTime
    println 'The transmission started at ' + txtime.toString()
} else {
    println 'Transmission not successfull, try again!'
}

/******************** Transmission of Baseband Signal ****************************/

// Subscribe to receive baseband agent notification
def bb = modem.agentForService Services.BASEBAND

// Generate a baseband signal
float freq = 5000
float duration = 1000e-3
int fd = 24000
int fc = 24000
int n = duration*fd
def signal = []
(0..n-1).each { t ->
    double a = 2*Math.PI*(freq-fc)*t/fd
    signal << (int)(Math.cos(a))
    signal << (int)(Math.sin(a))
}

// Transmit a baseband signal
def msg3 = new TxBasebandSignalReq(preamble: 3, signal: signal)
msg3.recipient = bb
modem.send(msg3)
// Receive the transmit notification
def txntf3 = modem.receive(TxFrameNtf, 5000)
if (txntf3 != null) {
    // Extract the transmission start time
    def txtime = txntf3.txTime
    println 'The transmission started at ' + txtime.toString()
} else {
    println 'Transmission not successfull, try again!'
}

modem.shutdown()
