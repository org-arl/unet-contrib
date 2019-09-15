//! Simulation: MISSION 2013 network
///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/mission2013/mission2013b-sim
/// OR
///   click on the Run button (▶) in UnetSim
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.fjage.RealTimePlatform
import org.arl.unet.sim.channels.Mission2013b

///////////////////////////////////////////////////////////////////////////////
// display documentation

println '''
MISSION 2013 network
--------------------

Nodes: 21, 22, 27, 28, 29, 31, 34

To connect to nodes via web shell:
22: http://localhost:8122
27: http://localhost:8127
28: http://localhost:8128
29: http://localhost:8129
31: http://localhost:8131
34: http://localhost:8134

Or to connect to nodes via unetsh:
21: bin/unet csh localhost 1121
22: bin/unet csh localhost 1122
27: bin/unet csh localhost 1127
28: bin/unet csh localhost 1128
29: bin/unet csh localhost 1129
31: bin/unet csh localhost 1131
34: bin/unet csh localhost 1134

'''

///////////////////////////////////////////////////////////////////////////////
// simulator configuration

platform = RealTimePlatform

// adopt MISSION 2013 channel model
channel = [ model: Mission2013b ]

// run the simulation forever
simulate {
  Mission2013b.nodes.each { addr ->
    node "$addr", location: Mission2013b.nodeLocation[addr],
    	shell: (5100+addr), web:(8100+addr), stack: "$home/etc/setup.groovy"
  }
}
