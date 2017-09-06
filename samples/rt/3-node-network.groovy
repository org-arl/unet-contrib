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

To connect to nodes 1, 2 or 3 via unet sh:
  bin/unet sh localhost 1101
  bin/unet sh localhost 1102
  bin/unet sh localhost 1103

To conect to nodes via web interface open browser
  http://localhost:8081/  for node 1
  http://localhost:8082/  for node 2
  http://localhost:8083/  for node 3

Connected to node 1...
Press ^D to exit
'''

///////////////////////////////////////////////////////////////////////////////
// simulator configuration

platform = RealTimePlatform   // use real-time mode

// run the simulation forever
simulate {
  node '1', remote: 1101, address: 1, location: [ 0.km, 0.km, -15.m], shell: [CONSOLE, WEB(8080,'/1')], web: 8081, stack: "$home/etc/initrc-stack"
  node '2', remote: 1102, address: 2, location: [ 1.km, 0.km, -15.m], shell: [TCP(5102), WEB(8080,'/2')], web: 8082, stack: "$home/etc/initrc-stack"
  node '3', remote: 1103, address: 3, location: [-1.km, 0.km, -15.m], shell: [TCP(5103), WEB(8080,'/3')], web: 8083, stack: "$home/etc/initrc-stack"
}
