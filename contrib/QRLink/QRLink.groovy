/**
 * QRLink - QR code based Unet link agent.
 *
 * Copyright 2022 Mandar Chitre.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import java.awt.FlowLayout
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.util.zip.CRC32
import javax.swing.*
import org.arl.fjage.*
import org.arl.fjage.param.Parameter
import org.arl.unet.*
import org.bytedeco.javacv.*
import com.google.zxing.*
import com.google.zxing.common.*
import com.google.zxing.qrcode.decoder.*
import com.google.zxing.client.j2se.MatrixToImageWriter

/**
 * QR-code based optical link.
 */
@groovy.transform.CompileStatic
class QRLink extends UnetAgent {

  final static String title = 'QRlink'
  final static String description = 'QR-code based optical link.'

  final static long LINK_TIMEOUT = 2000
  final static long ACK_TIMEOUT = 1000
  final static int MAX_QUEUE_LEN = 32

  private JFrame jframe = null
  private JLabel jlabel = null
  private int res = 800
  private int mtu = 0
  private boolean enable = true
  private FrameGrabber grabber = new OpenCVFrameGrabber(0)
  private MultiFormatReader reader = new MultiFormatReader()
  private NodeAddressCache addr = null
  private Map hints = new HashMap()
  private int peer = 0
  private long lastQRtime = 0
  private long lastTXtime = 0
  private Queue<DatagramReq> txQ = new ArrayDeque<DatagramReq>()
  private int txid = 0
  private int rxid = 0
  private Random rnd = new Random()

  ////// agent implementation

  QRLink() {
    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
    hints.put(EncodeHintType.QR_VERSION, 11)
    mtu = 80
  }

  @Override
  void setup() {
    register Services.LINK
    register Services.DATAGRAM
    addCapability LinkCapability.LINK_STATUS
  }

  @Override
  void startup() {
    addr = new NodeAddressCache(this, false)
    if (enable) {
      grabber.start()
      enableDisplay()
    }
    add new TickerBehavior(100) {
      @Override
      void onTick() {
        if (!enable) return
        String s = capture()
        if (s != null) incoming(s)
        if (peer != 0 && currentTimeMillis() > lastQRtime + LINK_TIMEOUT) linkDown()
        if (txid != 0 && currentTimeMillis() > lastTXtime + ACK_TIMEOUT) {
          log.info "FAIL ${txid}"
          txid = 0
          lastTXtime = 0
          display(advert())
        }
        if (txid == 0 && !txQ.isEmpty()) {
          DatagramReq req = txQ.poll()
          if (req.to == Address.BROADCAST || req.to == peer) {
            txid = 1 + rnd.nextInt(Integer.MAX_VALUE)
            display(pkt(txid, req.to, req.protocol, req.data))
            lastTXtime = currentTimeMillis()
            log.info "TX ${txid} ${req}"
          } else {
            log.info "DROP ${req}"
          }
        }
      }
    }
  }

  @Override
  void shutdown() {
    grabber.stop()
    disableDisplay()
    if (jframe != null) {
      jlabel = null
      jframe.dispose()
      jframe = null
    }
  }

  @Override
  void processMessage(Message msg) {
    if (addr) addr.update(msg)
  }

  @Override
  Message processRequest(Message req) {
    if (req instanceof DatagramReq) {
      if (req.to != Address.BROADCAST && req.to != this.@peer) return new RefuseRsp(req, 'No link available for peer')
      if (req.data == null) req.data = new byte[0]
      if (req.data.length > mtu) return new RefuseRsp(req, 'Data exceeds MTU')
      txQ.add((DatagramReq)req)
      while (txQ.size() > MAX_QUEUE_LEN) {
        def oreq = txQ.poll()
        log.info "DROP ${oreq}"
      }
      return new Message(req, Performative.AGREE)
    }
    return null
  }

  ////// parameters

  public enum Param implements Parameter {
    enable,
    peer
  }

  @Override
  List<Parameter> getParameterList() {
    return allOf(DatagramParam, Param)
  }

  int getMTU() {
    return mtu
  }

  int getRTU() {
    return mtu
  }

  boolean getEnable() {
    return this.@enable
  }

  boolean setEnable(boolean b) {
    if (b && !this.@enable) {
      grabber.start()
      enableDisplay()
    } else if (!b && this.@enable) {
      grabber.stop()
      disableDisplay()
    }
    this.@enable = b
    return this.@enable
  }

  int getPeer() {
    return this.@peer
  }

  ////// helpers

  private void enableDisplay() {
    if (jframe == null) {
      jframe = new JFrame()
      jframe.getContentPane().setLayout(new FlowLayout())
      jlabel = new JLabel(new ImageIcon(advert()))
      jframe.getContentPane().add(jlabel)
      jframe.pack()
      jframe.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
    }
    jframe.setVisible(true)
  }

  private void disableDisplay() {
    if (jframe != null) jframe.setVisible(false)
  }

  private void display(BufferedImage bimg) {
    jlabel.icon = new ImageIcon(bimg)
  }

  private BufferedImage advert() {
    return qr("ACK,${Integer.toString(addr.address,16)},0")
  }

  private BufferedImage ack(int pktid) {
    return qr("ACK,${Integer.toString(addr.address,16)},${Integer.toString(pktid,16)}")
  }

  private BufferedImage pkt(int pktid, int to, int protocol, byte[] data) {
    String cargo = Base64.getEncoder().encodeToString(data)
    String s = "${Integer.toString(pktid,16)},${Integer.toString(addr.address,16)},${Integer.toString(to,16)},${Integer.toString(protocol,16)},${cargo}"
    CRC32 crc32 = new CRC32()
    crc32.update(s.bytes)
    return qr("PKT,${Long.toString(crc32.value,16)},${s}")
  }

  private BufferedImage qr(String s) {
    def matrix = new MultiFormatWriter().encode(s, BarcodeFormat.QR_CODE, res, res, hints)
    return MatrixToImageWriter.toBufferedImage(matrix)
  }

  private void linkDown() {
    if (this.@peer == 0) return
    log.info "LINK DOWN ${this.@peer}"
    send new LinkStatusNtf(recipient: topic(), to: this.@peer, up: false)
    this.@peer = 0
  }

  private void linkUp(int a) {
    if (a == 0) return
    log.info "LINK UP $a"
    this.@peer = a
    send new LinkStatusNtf(recipient: topic(), to: this.@peer, up: true)
  }

  private void incoming(String s) {
    if (s.startsWith('ACK,')) {
      String[] p = s.substring(4).split(',')
      if (p.length == 2) {
        try {
          int a = Integer.parseInt(p[0], 16)
          int n = Integer.parseInt(p[1], 16)
          lastQRtime = currentTimeMillis()
          if (a != this.@peer) {
            linkDown()
            linkUp(a)
          }
          if (n != 0 && n == txid) {
            log.info "DELIVERED ${txid}"
            txid = 0
            lastTXtime = 0
            display(advert())
          }
        } catch (Exception ex) {
          // ignore
        }
      }
    } else if (s.startsWith('PKT,')) {
      String[] p = s.substring(4).split(',')
      if (p.length == 6) {
        try {
          long crc = Long.parseLong(p[0], 16)
          // TODO: add crc check
          int to = Integer.parseInt(p[3], 16)
          if (to == Address.BROADCAST || to == addr.address) {
            int n = Integer.parseInt(p[1], 16)
            if (n != rxid) {
              int from = Integer.parseInt(p[2], 16)
              int protocol = Integer.parseInt(p[4], 16)
              byte[] data = Base64.getDecoder().decode(p[5])
              lastQRtime = currentTimeMillis()
              if (from != this.@peer) {
                linkDown()
                linkUp(from)
              }
              log.info "RX ${n} from: ${from}, to: ${to}, protocol: ${protocol}, data: ${Utils.bytesToHexString(data)}"
              send new DatagramNtf(recipient: topic(), from: from, to: to, protocol: protocol, data: data)
              display(ack(n))
              rxid = n
            }
          }
        } catch (Exception ex) {
          // ignore
        }
      }
    }
  }

  private String capture() {
    Frame frame = grabber.grab()
    int[] argb = frame2argb(frame)
    LuminanceSource source = new RGBLuminanceSource(frame.imageWidth, frame.imageHeight, argb)
    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source))
    try {
      Result result = reader.decodeWithState(bitmap)
      return result.getText()
    } catch (NotFoundException) {
      return null
    }
  }

  private int[] frame2argb(Frame frame) {
    ByteBuffer buf = (ByteBuffer)frame.image[0]
    buf.rewind()
    int[] img = new int[frame.imageHeight * frame.imageWidth]
    for (int y = 0; y < frame.imageHeight; y++)
      for (int x = 0; x < frame.imageWidth; x++)
        img[y * frame.imageWidth + x] = (((int)buf.get()) << 16) | (((int)buf.get()) << 8) | buf.get()
    return img
  }

}
