//! Simulation: MISSION 2013 network
///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/mission2013/mission2013-sim
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

To connect to nodes via telnet shell:
22: telnet localhost 5122
27: telnet localhost 5127
28: telnet localhost 5128
29: telnet localhost 5129
31: telnet localhost 5131
34: telnet localhost 5134

Or to connect to nodes via unetsh:
21: bin/unet sh localhost 1121
22: bin/unet sh localhost 1122
27: bin/unet sh localhost 1127
28: bin/unet sh localhost 1128
29: bin/unet sh localhost 1129
31: bin/unet sh localhost 1131
34: bin/unet sh localhost 1134

Connected to 21...
Press ^D to exit
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
         shell: (addr==21)?[true,5121]:(5100+addr), stack: "$home/etc/initrc-stack.groovy"
  }
}
