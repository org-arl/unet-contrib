//! Simulation: Aloha-CS MAC protocol
///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/aloha-cs/aloha-cs
///
/// Output trace file: logs/trace.nam
///
/// Reference:
///   N. Chirdchoo, W.S. Soh, K.C. Chua (2007), Aloha-based MAC Protocols with
///   Collision Avoidance for Underwater Acoustic Networks, in Proceedings of
///   IEEE INFOCOM 2007.
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*
import static org.arl.unet.Services.*

println '''
Aloha-CS simulation
===================
'''

///////////////////////////////////////////////////////////////////////////////
// modem and channel model to use

modem.dataRate = [2400, 2400].bps
modem.frameLength = [2400/8, 2400/8].bytes
modem.preambleDuration = 0
modem.txDelay = 0

channel.model = ProtocolChannelModel
channel.soundSpeed = 1500.mps
channel.communicationRange = 5.km
channel.interferenceRange = 5.km

///////////////////////////////////////////////////////////////////////////////
// simulation settings

def nodes = 1..4                      // list of nodes
def loadRange = [0.1, 1.5, 0.1]       // min, max, step
def T = 2.hours                       // simulation horizon
trace.warmup = 15.minutes             // collect statistics after a while

///////////////////////////////////////////////////////////////////////////////
// simulation details

// generate random network geometry
def nodeLocation = [:]
nodes.each { myAddr ->
  nodeLocation[myAddr] = [rnd(0.km, 3.km), rnd(0.km, 3.km), -15.m]
}

// compute average distance between nodes for display
def sum = 0
def n = 0
nodes.each { n1 ->
  nodes.each { n2 ->
    if (n1 < n2) {
      n++
      sum += distance(nodeLocation[n1], nodeLocation[n2])
    }
  }
}
def avgRange = sum/n
println """Average internode distance: ${Math.round(avgRange)} m, delay: ${Math.round(1000*avgRange/channel.soundSpeed)} ms

TX Count\tRX Count\tLoss %\t\tOffered Load\tThroughput
--------\t--------\t------\t\t------------\t----------"""

File out = new File("logs/results.txt")
out.text = ''

// simulate at various arrival rates
for (def load = loadRange[0]; load <= loadRange[1]; load += loadRange[2]) {
  simulate T, {

    // setup 4 nodes identically
    nodes.each { myAddr ->
      def myNode = node("${myAddr}", address: myAddr, location: nodeLocation[myAddr])

      myNode.startup = {
        def phy = agentForService PHYSICAL
        def duration = Math.round(1000*phy[0].frameDuration)   // duration in ms
        add new PoissonBehavior(duration*nodes.size()/load, {
          // generate frame for a random node, except myself
          def txReq = new TxFrameReq(to: rnditem(nodes-myAddr), type: Physical.DATA)
          def busy = phy.busy
          if (busy == null) log.warning 'BUSY FAILED!'
          if (!busy) {
            // send it if modem is not TX/RX
            def rsp = phy << txReq
            if (rsp == null) log.warning 'TX FAILED!'
          } else {
            // back-off and keep trying until modem is idle
            def bo = AgentLocalRandom.current().nextInt(32)+1
            agent.add new BackoffBehavior(bo*duration, {
              def busy1 = phy.busy
              if (busy1 == null) log.warning 'BUSY FAILED!'
              if (busy1) {
                def bo1 = AgentLocalRandom.current().nextInt(32)+1
                backoff(bo1*duration)
              } else {
                def rsp1 = phy << txReq
                if (rsp1 == null) log.warning 'TX FAILED!'
              }
            })
          }
        })

      } // startup
    } // each

  }  // simulate

  // display statistics
  float loss = trace.txCount ? 100*trace.dropCount/trace.txCount : 0
  println sprintf('%6d\t\t%6d\t\t%5.1f\t\t%7.3f\t\t%7.3f',
    [trace.txCount, trace.rxCount, loss, trace.offeredLoad, trace.throughput])

  // save to file
  out << "${trace.offeredLoad},${trace.throughput}\n"

} // for
