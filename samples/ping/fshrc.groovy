import org.arl.unet.*
import org.arl.unet.phy.*

// documentation for the 'ping' command
doc['ping'] = '''ping - ping a remote node

Examples:
  ping 2                // ping node address 2 thrice
  ping 3,1              // ping node address 3 once
'''

subscribe phy

// add a closure to define the 'ping' command
ping = { addr, count = 3 ->
  println "PING $addr"
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
