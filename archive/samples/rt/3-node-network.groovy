//! Simulation: Simple 3-node network
///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/rt/3-node-network
/// OR
///   click on the Run button (▶) in UnetSim
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.fjage.*

///////////////////////////////////////////////////////////////////////////////
// display documentation

println '''
3-node network
--------------

Nodes: 1, 2, 3

You can interact with node 1 through :

- http://localhost:8101 (web interface)
- console shell (command line)

To connect to node 2 or node 3 via web shell:
  http://localhost:8102
  http://localhost:8103

To connect to nodes 1, 2 or 3 via unetsh:
  bin/unet csh localhost 1101
  bin/unet csh localhost 1102
  bin/unet csh localhost 1103

Press stop button (web interface), ^D (command line) to exit.
'''

///////////////////////////////////////////////////////////////////////////////
// simulator configuration

platform = RealTimePlatform   // use real-time mode

// run the simulation forever
simulate {
  node '1', remote: 1101, address: 1, location: [ 0.km, 0.km, -15.m], shell: true, web:8101, stack: "$home/etc/setup"
  node '2', remote: 1102, address: 2, location: [ 1.km, 0.km, -15.m], shell: 5102, web:8102, stack: "$home/etc/setup"
  node '3', remote: 1103, address: 3, location: [-1.km, 0.km, -15.m], shell: 5103, web:8103, stack: "$home/etc/setup"
}
