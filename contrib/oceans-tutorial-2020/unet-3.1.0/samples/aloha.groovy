///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/aloha.groovy
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*
import static org.arl.unet.Services.*
import static org.arl.unet.phy.Physical.*

println '''
Pure ALOHA simulation
=====================

TX Count\tRX Count\tOffered Load\tThroughput
--------\t--------\t------------\t----------'''

///////////////////////////////////////////////////////////////////////////////
// modem settings for a simple modem with 1 second frame duration

channel.model = ProtocolChannelModel

modem.dataRate = [2400, 2400].bps
modem.frameLength = [2400/8, 2400/8].bytes
modem.headerLength = 0
modem.preambleDuration = 0
modem.txDelay = 0

///////////////////////////////////////////////////////////////////////////////
// simulation settings

def nodes = 1..4                      // list with 4 nodes
def T = 2.hours                       // simulation duration
def minLoad = 0.1                     // mimimum load
def maxLoad = 1.5                     // maximum load
def loadStep = 0.1                    // step size for load
trace.warmup = 15.minutes             // collect statistics after a while

///////////////////////////////////////////////////////////////////////////////
// simulation details

for (def load = minLoad; load <= maxLoad; load += loadStep) {

  simulate T, {                   // simulate 2 hours of elapsed time
    nodes.each { myAddr ->
      def myNode = node "${myAddr}", address: myAddr, location: [0, 0, 0]
      myNode.startup = {                      // startup script to run on each node
        def phy = agentForService PHYSICAL
        def arrivalRate = load/nodes.size()   // arrival rate per node
        add new PoissonBehavior(1000/arrivalRate, {     // avg time between events in ms
          // drop any ongoing TX/RX and then send frame to random node, except myself
          def dst = rnditem(nodes-myAddr)
          phy << new ClearReq()
          phy << new TxFrameReq(to: dst, type: DATA)
        })
      }
    }
  } // simulate

  // display collected statistics
  println sprintf('%6d\t\t%6d\t\t%7.3f\t\t%7.3f',
    [trace.txCount, trace.rxCount, trace.offeredLoad, trace.throughput])

} // for
