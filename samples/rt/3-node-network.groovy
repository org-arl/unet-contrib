//! Simulation: Simple 3-node network
///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/rt/3-node-network
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.fjage.*

///////////////////////////////////////////////////////////////////////////////
// display documentation

println '''
3-node network
--------------

Nodes: 1, 2, 3

To connect to node 2 or node 3 via telnet:
  telnet localhost 5102
  telnet localhost 5103

To connect to nodes 1, 2 or 3 via unet sh:
  bin/unet sh localhost 1101
  bin/unet sh localhost 1102
  bin/unet sh localhost 1103

Connected to node 1...
Press ^D to exit
'''

///////////////////////////////////////////////////////////////////////////////
// simulator configuration

platform = RealTimePlatform   // use real-time mode

// run the simulation forever
simulate {
  node '1', remote: 1101, address: 1, location: [ 0.km, 0.km, -15.m], shell: true, stack: "$home/etc/initrc-stack"
  node '2', remote: 1102, address: 2, location: [ 1.km, 0.km, -15.m], shell: 5102, stack: "$home/etc/initrc-stack"
  node '3', remote: 1103, address: 3, location: [-1.km, 0.km, -15.m], shell: 5103, stack: "$home/etc/initrc-stack"
}
