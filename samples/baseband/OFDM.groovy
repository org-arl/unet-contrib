import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.bb.*
import edu.emory.mathcs.jtransforms.fft.*;

class OFDM extends UnetAgent {

  final static int HDRSIZE = 4
  final static int MAXPKTLEN = 127

  AgentID bb, node
  AgentID ntf, snoop

  int nc = 256
  int np = 32

  void setup() {
    ntf = topic()
    snoop = topic(getAgentID(), Physical.SNOOP)
    register Services.PHYSICAL
    register Services.DATAGRAM
  }

  void startup() {
    node = agentForService Services.NODE_INFO
    bb = agentForService Services.BASEBAND
    def phy = agentsForService Services.PHYSICAL
    if (node == null || bb == null || !phy.contains(bb)) {
      log.severe 'Node info, baseband or physical service not found'
      stop()
      return
    }
    subscribe(bb)
  }

  Message processRequest(Message req) {
    if (req instanceof DatagramReq) {
      if (req.to < 0 || req.to > Address.MAX ||
          req.protocol < 0 || req.protocol > Protocol.MAX ||
          (req.data && req.data.length > 255))
        return new Message(req, Performative.REFUSE)
      def tx = processTX(req)
      if (tx) {
        tx.recipient = bb
        def rsp = request(tx, 1000)
        if (rsp && rsp.performative == Performative.AGREE)
          return new Message(req, Performative.AGREE)
      }
      return new Message(req, Performative.FAILURE)
    } else if (req instanceof ClearReq) {
      // do nothing
      return new Message(req, Performative.AGREE)
    }
  }

  void processMessage(Message msg) {
    if (msg instanceof RxBasebandSignalNtf) {
      def data = processRX(msg.signal)
      if (data) {
        def dntf = new RxFrameNtf(from: data[0], to: data[1], type: Physical.DATA, protocol: data[2], data: data[3], rxTime: msg.rxTime)
        dntf.recipient = (data[1] == Address.BROADCAST || data[1] == node.address) ? ntf : snoop
        send dntf
      } else {
        send new BadFrameNtf(recipient: ntf, type: Physical.DATA, rxTime: msg.rxTime)
      }
    }
  }

  // OFDM functions

  // convert data into signal
  TxBasebandSignalReq processTX(DatagramReq req) {
    byte[] data = req.data?:new byte[0]
    BitBuffer buf = new BitBuffer(data.length + HDRSIZE)
    buf.write([node.address, req.to, req.protocol, data.length] as byte[])
    buf.write(data)
    buf.reset()
    int L = (buf.sizeInBits+nc-1)/nc
    def sig = new float[2*L*(nc+np)]
    FloatFFT_1D fft = new FloatFFT_1D(nc)
    def bpsk = [1: -1, 0: +1]
    for (int j = 0; j < L; j++) {
      int blkStart = 2*j*(nc+np) + 2*np
      for (int k = 0; k < nc; k++)
        sig[blkStart+2*k] = bpsk[buf.read()]?:0
      fft.complexForward(sig, blkStart)
      System.arraycopy(sig, blkStart+2*(nc-np), sig, blkStart-2*np, 2*np)
    }
    return new TxBasebandSignalReq(signal: sig)
  }

  // convert signal into data
  // return [from, to, protocol, data]
  def processRX(float[] sig) {
    BitBuffer buf = new BitBuffer(MAXPKTLEN + HDRSIZE)
    int L = sig.length/(nc+np)
    FloatFFT_1D fft = new FloatFFT_1D(nc)
    for (int j = 0; j < L && !buf.eos(); j++) {
      int blkStart = 2*j*(nc+np) + 2*np
      fft.complexInverse(sig, blkStart, false)
      for (int k = 0; k < nc && !buf.eos(); k++)
        buf.write(sig[blkStart+2*k] < 0 ? 1 : 0)
    }
    byte[] hdrdata = buf.getBytes()
    if (hdrdata[0] < 0 || hdrdata[0] > Address.MAX) return null
    if (hdrdata[1] < 0 || hdrdata[1] > Address.MAX) return null
    if (hdrdata[2] < 0 || hdrdata[2] > Protocol.MAX) return null
    if (hdrdata[3] < 0 || hdrdata[3] > MAXPKTLEN) return null
    byte[] data = new byte[hdrdata[3]]
    System.arraycopy(hdrdata, 4, data, 0, hdrdata[3])
    return [hdrdata[0], hdrdata[1], hdrdata[2], data]
  }
  
  // parameters required by PHYSICAL

  List<Parameter> getParameterList() {
    return allOf(DatagramParam, PhysicalParam)
  }

  int getMTU() {
    int siglen = bb.maxSignalLength
    int L = siglen/(nc+np)
    int n = L*nc/8 - HDRSIZE
    if (n > MAXPKTLEN) n = MAXPKTLEN
    return n > 0 ? n : 0
  }

  boolean getRxEnable() {
    return bb.rxEnable
  }

  void setRxEnable(boolean b) {
    bb.rxEnable = b
  }

  long getTime() {
    return bb.time
  }

  float getPropagationSpeed() {
    return bb.propagationSpeed
  }

  boolean getBusy() {
    return bb.busy
  }

  float getTimestampedTxDelay() {
    return bb.timestampedTxDelay
  }

  float getRefPowerLevel() {
    return bb.refPowerLevel
  }

  // indexed parameters required by PHYSICAL

  List<Parameter> getParameterList(int index) {
    if (index < 0 || index > 1) return null;
    return allOf(DatagramParam, PhysicalChannelParam);
  }

  int getMTU(int type) {
    return getMTU()
  }

  float getFrameDuration(int type) {
    return (float)bb.maxSignalLength/bb.basebandRate + bb.preambleDuration
  }

  int getPowerLevel(int type) {
    return bb[type].powerLevel
  }

  void setPowerLevel(int type, int v) {
    bb[type].powerLevel = v
  }

  int getMaxPowerLevel(int type) {
    return bb[type].maxPowerLevel
  }

  int getMinPowerLevel(int type) {
    return bb[type].minPowerLevel
  }

  int getErrorDetection(int type) {
    return 0
  }

  int getFrameLength(int type) {
    return getMTU(type)
  }

  int getMaxFrameLength(int type) {
    return getMTU(type)
  }

  int getFec(int type) {
    return 0
  }

  List<String> getFecList(int type) {
    return []
  }

  float getDataRate(int type) {
    return getFrameLength(type)*8/getFrameDuration(type)
  }

}
