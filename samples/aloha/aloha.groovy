//! Simulation: Aloha wireless network
///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/aloha/aloha
///
/// Output trace file: logs/trace.nam
/// Plot results: bin/unet samples/aloha/plot-results
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
Pure Aloha simulation
=====================

TX Count\tRX Count\tLoss %\t\tOffered Load\tThroughput
--------\t--------\t------\t\t------------\t----------'''

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

def nodes = 1..4                      // list of nodes
def loadRange = [0.1, 1.5, 0.1]       // min, max, step
def T = 2.hours                       // simulation horizon
trace.warmup = 15.minutes             // collect statistics after a while

///////////////////////////////////////////////////////////////////////////////
// simulation details

for (def load = loadRange[0]; load <= loadRange[1]; load += loadRange[2]) {
  simulate T, {

    // setup each node at origin to ensure no propagation delay between nodes
    nodes.each { myAddr ->
      def myNode = node("${myAddr}", address: myAddr, location: [0, 0, 0])
      myNode.startup = {
        def phy = agentForService PHYSICAL
        add new PoissonBehavior(1000*nodes.size()/load, {
          // drop any ongoing TX/RX and then send frame to random node, except myself
          phy << new ClearReq()
          phy << new TxFrameReq(to: rnditem(nodes-myAddr), type: DATA)
        })
      }

    }

  }  // simulate

  // display statistics
  float loss = trace.txCount ? 100*trace.dropCount/trace.txCount : 0
  println sprintf('%6d\t\t%6d\t\t%5.1f\t\t%7.3f\t\t%7.3f',
    [trace.txCount, trace.rxCount, loss, trace.offeredLoad, trace.throughput])

} // for
