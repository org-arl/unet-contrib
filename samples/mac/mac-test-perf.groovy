//! Simulation: 10-node random network for MySimplestMac performance test
///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/mac/mac-test-perf.groovy
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.unet.sim.channels.BasicAcousticChannel

channel = [ model: BasicAcousticChannel ]   // use an acoustic channel model with default parameters
trace.warmup = 10.minutes                   // collect statistics after a while

println '''
MySimplestMac simulation
========================

TX Count\tRX Count\tOffered Load\tThroughput
--------\t--------\t------------\t----------'''

def nodes = 1..10                           // nodes with addresses 1 to 10
for (int i: 1..10) {
  float load = i/10.0                       // network load from 0.1 to 1.0 in steps of 0.1
  float loadPerNode = load/nodes.size()     // divide network load across nodes evenly
  simulate 1.hour, {                        // run each simulation for 1 hour of simulated time
    for (int me: nodes) {                   //   with randomly placed nodes
      node "$me", location: [rnd(-500.m, 500.m), rnd(-500.m, 500.m), rnd(-20.m, 0)], stack: { container ->
        container.add 'mac', new MySimplestMac()
        container.add 'load', new LoadGenerator(nodes-me, loadPerNode)   // generate load to all nodes except me
      }
    }
  }
  println sprintf('%6d\t\t%6d\t\t%7.3f\t\t%7.3f', trace.txCount, trace.rxCount, trace.offeredLoad, trace.throughput)
}
