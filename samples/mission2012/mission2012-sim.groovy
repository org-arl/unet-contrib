//! Simulation: MISSION 2012 network
///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/mission2012/mission2012-sim
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.fjage.RealTimePlatform

///////////////////////////////////////////////////////////////////////////////
// display documentation

println '''
MISSION 2012 network
--------------------

Nodes: 21, 22, 27, 28, 29

To connect to nodes via telnet shell:
22: telnet localhost 5122
27: telnet localhost 5127
28: telnet localhost 5128
29: telnet localhost 5129

Or to connect to nodes via unetsh:
21: bin/unet sh localhost 1121
22: bin/unet sh localhost 1122
27: bin/unet sh localhost 1127
28: bin/unet sh localhost 1128
29: bin/unet sh localhost 1129

Connected to 21...
Press ^D to exit
'''

///////////////////////////////////////////////////////////////////////////////
// simulator configuration

platform = RealTimePlatform

// adopt MISSION 2012 channel model
channel = [ model: Mission2012Channel ]

// run the simulation forever
simulate {
  Mission2012Channel.nodes.each { addr ->
    node "$addr", location: Mission2012Channel.nodeLocation[addr], remote: (1100+addr),
         shell: (addr==21)?[true,5121]:(5100+addr), stack: "$home/etc/initrc-stack.groovy"
  }
}
