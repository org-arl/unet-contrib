//! Simulation: Equilateral triangle network
///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/super-tdma/e3-network
///
/// Output trace file: logs/trace.nam
///
/// Reference:
/// [1] M. Chitre, M. Motani, and S. Shahabudeen, "Throughput of networks
///     with large propagation delays", IEEE Journal of Oceanic Engineering,
///     37(4):645-658, 2012.
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import static org.arl.unet.Services.*

///////////////////////////////////////////////////////////////////////////////
// settings

def slot = 422.ms               // default packet length is about 345 ms
def range = 650.m               // about slot x 1540 m/s
def time = 15.minutes           // simulation time
def schedule = [[2, 3, 0, 0],   // schedule from [1]
                [0, 0, 1, 3],
                [0, 1, 0, 2]]

///////////////////////////////////////////////////////////////////////////////
// display documentation

println """
Equilateral triangle network
----------------------------

Internode distance:     ${range} m
Slot length:            ${(1000*slot).round()} ms
Simulation time:        ${time} s"""

///////////////////////////////////////////////////////////////////////////////
// simulate schedule

simulate time, {

  def n = []
  n << node('1', address: 1, location: [0, 0, 0])
  n << node('2', address: 2, location: [range, 0, 0])
  n << node('3', address: 3, location: [0.5*range, 0.866*range, 0])

  n.eachWithIndex { n1, i ->
    n1.startup = {
      def phy = agentForService PHYSICAL
      phy[1].frameLength = phy[0].frameLength
      phy[1].dataRate = phy[0].dataRate
      add new TickerBehavior(1000*slot, {
        def slen = schedule[i].size()
        def s = schedule[i][(tickCount-1)%slen]
        if (s) phy << new TxFrameReq(to: s, type: Physical.DATA)
      })
    }
  }

}

// display statistics
println """TX:                     ${trace.txCount}
RX:                     ${trace.rxCount}
Offered load:           ${trace.offeredLoad.round(3)}
Throughput:             ${trace.throughput.round(3)}"""
