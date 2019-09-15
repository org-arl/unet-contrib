//! Simulation: MISSION 2012 network
///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/mission2012/mission2012-sim
/// OR
///   click on the Run button (▶) in UnetSim
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.fjage.RealTimePlatform

///////////////////////////////////////////////////////////////////////////////
// display documentation

println '''
MISSION 2012 network
--------------------

Nodes: 21, 22, 27, 28, 29

To connect to nodes via web shell:
21: http://localhost:8122
22: http://localhost:8122
27: http://localhost:8127
28: http://localhost:8128
29: http://localhost:8129

Or to connect to nodes via unetsh:
21: bin/unet csh localhost 1121
22: bin/unet csh localhost 1122
27: bin/unet csh localhost 1127
28: bin/unet csh localhost 1128
29: bin/unet csh localhost 1129


Press stop button (web interface), ^D (command line) to exit.
'''

///////////////////////////////////////////////////////////////////////////////
// simulator configuration

platform = RealTimePlatform

// adopt MISSION 2012 channel model
channel = [ model: Mission2012Channel ]

// run the simulation forever
simulate {
  Mission2012Channel.nodes.each { addr ->
    node "$addr", location: Mission2012Channel.nodeLocation[addr], shell: (5100+addr), web:(8100+addr), stack: "$home/etc/setup.groovy"
  }
}
