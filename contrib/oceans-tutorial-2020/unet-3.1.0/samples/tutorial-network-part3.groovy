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
  node '1', address: 1, location: [0.km, 0.km,  -5.m], web: 8081, stack: "$home/scripts/setup.groovy", shell: true
  
  node '2', address: 2, location: [1.km, 0.km,  -30.m], stack: "$home/scripts/custom.groovy", shell: true
  
  node '3', address: 3, location: [1.5.km, 0.5.km,  -20.m], stack: "$home/scripts/custom.groovy", shell: true
  
  node '4', address: 4, location: [3.km, 0.km,  -5.m], web: 8084, stack: "$home/scripts/setup.groovy", shell: true
  
  node '5', address: 5, location: [4.5.km, 0.km,  -30.m], stack: "$home/scripts/custom.groovy", shell: true
}