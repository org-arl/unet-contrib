import org.arl.fjage.remote.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.bb.*

/******************** Open connection to modem *********************************/
// Get the IP address of the modem from argument passed
if (args.size() < 1) {
  println "Please provide the IP address of the modem as argument!"
  return
}
ip_address = args[0]
// Open a connection to modem
Gateway modem = new Gateway(ip_address, 1100);

/******************** Transmission of CONTROL and DATA packet ******************/

// Look for agents providing physical service
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

/******************** Transmission of baseband signal ****************************/

// Look for agents providing baseband service
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

/******************** Recording a baseband signal ****************************/

// Record a baseband signal
def msg4 = new RecordBasebandSignalReq(recLength: 24000)
msg4.recipient = bb
modem.send(msg4)
// Receive the notification when the signal is recorded
def rxntf = modem.receive(RxBasebandSignalNtf, 5000)
if (rxntf != null) {
    // Extract the recorded signal
    def rec_signal = rxntf.signal
    println 'Recorded signal successfully!'
    // The recorded signal saved in `rec_signal` variable
    // can be processed as required by the user.
} else {
    println 'Recording not successfull, try again!'
}

/******************** Close connection to modem ****************************/

modem.shutdown()
