//! Simulation

import org.arl.fjage.RealTimePlatform
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*

///////////////////////////////////////////////////////////////////////////////
// display documentation

println '''
2-node network (High-frequency)
-------------------------------

Node A: tcp://localhost:1101, http://localhost:8081/
Node B: tcp://localhost:1102, http://localhost:8082/
'''

///////////////////////////////////////////////////////////////////////////////
// simulation settings

platform = RealTimePlatform           // use real-time mode

///////////////////////////////////////////////////////////////////////////////
// channel and modem settings

channel = [
  model:                BasicAcousticChannel,
  carrierFrequency:     60.kHz,
  bandwidth:            20000.Hz,
  waterDepth:           30.m
]

modem.dataRate =        [10000.bps, 15000.bps]

// run the simulation forever
simulate {
  node 'A', location: [ 0.km, 0.km, -8.m], web: 8081, api: 1101, stack: "$home/etc/setup"
  node 'B', location: [ 0.2.km, 0.km, -5.m], web: 8082, api: 1102, stack: "$home/etc/setup"
}
