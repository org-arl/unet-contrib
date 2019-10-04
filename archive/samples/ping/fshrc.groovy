import org.arl.unet.*
import org.arl.unet.phy.*

subscribe phy

// add a closure to define the 'sendping' command
sendping = { addr, count = 3 ->
  println "Pinging $addr"
  count.times {
    phy << new DatagramReq(to: addr, protocol: Protocol.USER)
    def txNtf = receive(TxFrameNtf, 1000)
    def rxNtf = receive({ it instanceof RxFrameNtf && it.from == addr}, 5000)
    if (txNtf && rxNtf && rxNtf.from == addr)
      println "Response from ${rxNtf.from}: time=${(rxNtf.rxTime-txNtf.txTime)/1000} ms"
    else
      println 'Request timeout'
  }
}
