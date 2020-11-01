/**
 * Sample custom Julia PHYSICAL layer implementation for UnetStack based modems,
 * as described in blog article: https://blog.unetstack.net/custom-phy-in-julia.
 *
 * Do note that this implementation is intentionally minimalistic, to aid
 * understanding. While this may be used as a starting point for your own
 * development, the code here isn't directly suitable for production use.
 * You'd want to have much better signal processing, error detection,
 * error correction, clock synchronization, exception handling, etc.
 *
 * Licensed under the MIT license:
 *
 * Copyright (c) 2020 Mandar Chitre.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import org.arl.fjage.*
import org.arl.fjage.param.Parameter
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.bb.*
import org.arl.yoda.ModemChannelParam
import org.arl.jajub.*

@groovy.transform.CompileStatic
class MyJuliaPhy extends UnetAgent {

  private final int HDRSIZE = 5
  private final int SAMPLES_PER_SYMBOL = 150
  private final float NFREQ = 1/15

  // default Yoda PHY agent name hardcoded for simplicity,
  // and assumed to provide PHYSICAL and BASEBAND services
  private final AgentID bbsp = agent('phy')

  private NodeAddressCache addrCache = new NodeAddressCache(this, true)
  private Map<String,Message> pending = [:]
  private JuliaBridge julia = new JuliaBridge()

  @Override
  void setup() {
    julia.open()
    julia.exec(julia_bytes2signal)
    julia.exec(julia_signal2bytes)
    register Services.DATAGRAM
    register Services.PHYSICAL
  }

  @Override
  void startup() {
    subscribe bbsp
    int nsamples = (MTU + HDRSIZE) * 8 * SAMPLES_PER_SYMBOL
    set(bbsp, Physical.CONTROL, ModemChannelParam.modulation, 'none')
    set(bbsp, Physical.CONTROL, ModemChannelParam.basebandExtra, nsamples)
    set(bbsp, Physical.CONTROL, ModemChannelParam.basebandRx, true)
    set(bbsp, Physical.DATA, ModemChannelParam.modulation, 'none')
    set(bbsp, Physical.DATA, ModemChannelParam.basebandExtra, nsamples)
    set(bbsp, Physical.DATA, ModemChannelParam.basebandRx, true)
  }

  @Override
  void shutdown() {
    julia.close()
  }

  ////// parameters

  @Override
  protected List<Parameter> getParameterList() {
    return allOf(DatagramParam, PhysicalParam)
  }

  @Override
  protected List<Parameter> getParameterList(int ndx) {
    if (ndx == Physical.CONTROL || ndx == Physical.DATA)
      return allOf(DatagramParam, PhysicalChannelParam)
    return null
  }

  // Datagram service parameters (read-only)
  final int MTU = 8
  final int RTU = MTU

  // Physical service parameters (read-only) delegated to Yoda PHY
  Float getRefPowerLevel()    { return (Float)get(bbsp, PhysicalParam.refPowerLevel) }
  Float getMaxPowerLevel()    { return (Float)get(bbsp, PhysicalParam.maxPowerLevel) }
  Float getMinPowerLevel()    { return (Float)get(bbsp, PhysicalParam.minPowerLevel) }
  Float getRxSensitivity()    { return (Float)get(bbsp, PhysicalParam.rxSensitivity) }
  Float getPropagationSpeed() { return (Float)get(bbsp, PhysicalParam.propagationSpeed) }
  Long getTime()              { return (Long)get(bbsp, PhysicalParam.time) }
  Boolean getBusy()           { return (Boolean)get(bbsp, PhysicalParam.busy) }
  Boolean getRxEnable()       { return (Boolean)get(bbsp, PhysicalParam.rxEnable) }

  // Physical service indexed parameter (read-only)
  int getMTU(int ndx)               { return MTU }
  int getRTU(int ndx)               { return RTU }
  int getFrameLength(int ndx)       { return MTU + HDRSIZE }
  int getMaxFrameLength(int ndx)    { return MTU + HDRSIZE }
  int getFec(int ndx)               { return 0 }        // no FEC
  List<String> getFecList(int ndx)  { return [] }       // FEC not supported
  int getErrorDetection(int ndx)    { return 8 }        // 8 bits
  boolean getLlr(int ndx)           { return false }    // LLR not supported

  // Physical service indexed dynamic parameters

  void setPowerLevel(int ndx, float lvl) {
    if (ndx != Physical.CONTROL && ndx != Physical.DATA) return
    set(bbsp, BasebandParam.signalPowerLevel, lvl)
  }

  Float getPowerLevel(int ndx) {
    if (ndx != Physical.CONTROL && ndx != Physical.DATA) return null
    return (Float)get(bbsp, BasebandParam.signalPowerLevel)
  }

  Float getFrameDuration(int ndx) {
    if (ndx != Physical.CONTROL && ndx != Physical.DATA) return null
    def bbrate = (Float)get(bbsp, BasebandParam.basebandRate)
    if (bbrate == null) return 0f
    int prelen = getPreambleLength(ndx)
    return (float)((prelen + (MTU + HDRSIZE) * 8 * SAMPLES_PER_SYMBOL) / bbrate)
  }

  Float getDataRate(int ndx) {
    if (ndx != Physical.CONTROL && ndx != Physical.DATA) return null
    return (float)(8 * getFrameLength(ndx) / getFrameDuration(ndx))
  }

  private int getPreambleLength(int ndx) {
    int prelen = 0
    def pre = request(new GetPreambleSignalReq(recipient: bbsp, preamble: ndx), 1000)
    if (pre instanceof BasebandSignal) prelen = ((BasebandSignal)pre).signalLength
    return prelen
  }

  ////// handle transmit requests

  @Override
  Message processRequest(Message req) {
    if (req instanceof DatagramReq) return processDatagramReq(req)
    if (req instanceof TxRawFrameReq) return processTxRawFrameReq(req)
    if (req instanceof ClearReq) {
      send new ClearReq(recipient: bbsp)
      pending.clear()
      return new Message(req, Performative.AGREE)
    }
  }

  private Message processTxRawFrameReq(TxRawFrameReq req) {
    if (req.data == null || req.data.length != MTU + HDRSIZE)
      return new Message(req, Performative.REFUSE)
    if (req.type != Physical.CONTROL && req.type != Physical.DATA)
      return new Message(req, Performative.REFUSE)
    if (transmit(req.type, req.data, req))
      return new Message(req, Performative.AGREE)
    return new Message(req, Performative.FAILURE)
  }

  private Message processDatagramReq(DatagramReq req) {
    if (req.data != null && req.data.length > MTU)
      return new Message(req, Performative.REFUSE)
    def from = addrCache.address
    if (from != null) {
      byte[] buf = composePDU(from, req.to, req.protocol, req.data)
      int ch = req instanceof TxFrameReq ? req.type : Physical.DATA
      if (ch != Physical.CONTROL && ch != Physical.DATA)
        return new Message(req, Performative.REFUSE)
      if (transmit(ch, buf, req))
        return new Message(req, Performative.AGREE)
    }
    return new Message(req, Performative.FAILURE)
  }

  private final PDU header = new PDU() {
    @Override
    void format() {
      length(HDRSIZE)
      uint8('parity')
      uint8('protocol')
      uint8('from')
      uint8('to')
      uint8('len')
    }
  }

  private byte[] composePDU(int from, int to, int protocol, byte[] data) {
    if (data == null) data = new byte[0]
    def hdr = header.encode([
      parity: 0,
      from: from,
      to: to,
      protocol: protocol,
      len: data.length
    ] as Map<String,Object>)
    def buf = new byte[HDRSIZE + MTU]
    System.arraycopy(hdr, 0, buf, 0, HDRSIZE)
    System.arraycopy(data, 0, buf, HDRSIZE, data.length)
    int parity = 0
    for (int i = 1; i < buf.length; i++)
      parity ^= buf[i]    // compute parity bits
    buf[0] = (byte)parity
    return buf
  }

  private boolean transmit(int ch, byte[] buf, Message req) {
    def signal = bytes2signal(buf)
    def bbreq = new TxBasebandSignalReq(recipient: bbsp, preamble: ch, signal: signal)
    def rsp = request(bbreq, 1000)
    if (rsp?.performative != Performative.AGREE) return false
    pending.put(bbreq.messageID, req)
    return true
  }

  private final String julia_bytes2signal = """
    function bytes2signal(buf)
      signal = Array{ComplexF32}(undef, length(buf) * 8 * ${SAMPLES_PER_SYMBOL})
      p = 1
      for b in buf
        for j in 0:7
          bit = (b >> j) & 0x01
          f = bit == 1 ? -${NFREQ} : ${NFREQ}
          signal[p:p+${SAMPLES_PER_SYMBOL-1}] .= cis.(2pi * f * (0:${SAMPLES_PER_SYMBOL-1}))
          p += ${SAMPLES_PER_SYMBOL}
        end
      end
      return signal
    end
  """

  private float[] bytes2signal(byte[] buf) {
    def arg = new ByteArray(data: buf, dims: [buf.length] as int[])
    def rv = (FloatArray)julia.call("bytes2signal", arg)
    return rv?.data
  }

  ////// handle transmit notifications & receptions

  @Override
  void processMessage(Message msg) {
    addrCache.update(msg)
    if (msg instanceof TxFrameNtf) handleTxFrameNtf(msg)
    else if (msg instanceof RxBasebandSignalNtf) handleRxBasebandSignalNtf(msg)
  }

  private void handleTxFrameNtf(TxFrameNtf msg) {
    def req = pending.remove(msg.inReplyTo)
    if (req == null) return
    def ntf = new TxFrameNtf(req)
    ntf.type = msg.type
    ntf.txTime = msg.txTime
    ntf.location = msg.location
    send ntf
  }

  private void handleRxBasebandSignalNtf(RxBasebandSignalNtf msg) {
    def buf = signal2bytes(msg.signal, 2 * getPreambleLength(msg.preamble))
    if (buf == null) return
    int parity = 0
    for (int i = 1; i < buf.length; i++)
      parity ^= buf[i]        // compute parity bits
    if (buf.length >= HDRSIZE && buf[0] == parity) {
      def hdr = header.decode(buf[0..HDRSIZE-1] as byte[])
      int len = (int)hdr.len
      if (len <= MTU) {
        def rcpt = topic()
        if (hdr.to != Address.BROADCAST && hdr.to != addrCache.address)
          rcpt = topic(agentID, Physical.SNOOP)
        byte[] data = null
        if (len > 0) {
          data = new byte[len]
          System.arraycopy(buf, HDRSIZE, data, 0, len)
        }
        send new RxFrameNtf(
          recipient: rcpt,
          type: msg.preamble,
          rxTime: msg.rxTime,
          location: msg.location,
          rssi: msg.rssi,
          from: (int)hdr.from,
          to: (int)hdr.to,
          protocol: (int)hdr.protocol,
          data: data
        )
        return
      }
    }
    send new BadFrameNtf(
      recipient: topic(),
      type: msg.preamble,
      rxTime: msg.rxTime,
      location: msg.location,
      rssi: msg.rssi,
      data: buf
    )
  }

  private final String julia_signal2bytes = """
    function signal2bytes(signal)
      n = length(signal) ÷ (${SAMPLES_PER_SYMBOL} * 8)
      buf = zeros(Int8, n)
      p = 1
      for i in 1:length(buf)
        for j in 0:7
          s = @view signal[p:p+${SAMPLES_PER_SYMBOL-1}]
          p += ${SAMPLES_PER_SYMBOL}
          x = cis.(2pi * ${NFREQ} .* (0:${SAMPLES_PER_SYMBOL-1}))
          s0 = sum(s .* conj.(x))
          s1 = sum(s .* x)
          if abs(s1) > abs(s0)
            buf[i] = buf[i] | (0x01 << j)
          end
        end
      end
      return buf
    end
  """

  private byte[] signal2bytes(float[] signal, int start) {
    def arg = new FloatArray(
      data: signal[start..-1] as float[],
      dims: [(signal.length-start).intdiv(2)] as int[],
      isComplex: true
    )
    def rv = (ByteArray)julia.call('signal2bytes', arg)
    return rv?.data
  }

}
