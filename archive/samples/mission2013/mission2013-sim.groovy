//! Simulation: MISSION 2013 network
///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/mission2013/mission2013-sim
/// OR
///   click on the Run button (▶) in UnetSim
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.fjage.RealTimePlatform
import org.arl.unet.sim.channels.Mission2013a

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

Press stop button (web interface), ^D (command line) to exit.
'''

///////////////////////////////////////////////////////////////////////////////
// simulator configuration

platform = RealTimePlatform

// adopt MISSION 2013 channel model
channel = [ model: Mission2013a ]

// run the simulation forever
simulate {
  Mission2013a.nodes.each { addr ->
    node "$addr", location: Mission2013a.nodeLocation[addr], remote: (1100+addr),
    	shell: (5100+addr), web:(8100+addr), stack: "$home/etc/setup.groovy"
  }
}
