//! Simulation

import org.arl.fjage.RealTimePlatform
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*

///////////////////////////////////////////////////////////////////////////////
// simulation settings

platform = RealTimePlatform           // use real-time mode

///////////////////////////////////////////////////////////////////////////////
// channel settings

channel = [model: ProtocolChannelModel]

///////////////////////////////////////////////////////////////////////////////
// simulation details

simulate {
  node 'beacon1', address: 1, location: [0.km, 0.km,  -5.m], web: 8081, api: 1101, stack: "$home/scripts/setup.groovy", shell: true

  node 'beacon2', address: 2, location: [1.km, 0.km,  -30.m], api: 1102, stack: "$home/scripts/custom.groovy", shell: true

  node 'beacon3', address: 3, location: [1.5.km, 0.5.km,  -20.m], api: 1103, stack: "$home/scripts/custom.groovy", shell: true

  node 'target', address: 4, location: [1.km, 1.km,  -30.m], stack: "$home/scripts/custom.groovy", shell: true, mobility: true
}