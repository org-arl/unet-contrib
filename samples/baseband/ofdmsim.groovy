//! Simulation: OFDM SDR
///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/baseband/ofdmsim
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.fjage.*

///////////////////////////////////////////////////////////////////////////////
// display documentation

println '''
OFDM Simulation as SDR
----------------------

Nodes: 1, 2

To connect to node 2 via telnet:
  telnet localhost 5102

Or to connect to nodes via unetsh:
  1: bin/unet sh localhost 1101
  2: bin/unet sh localhost 1102

Connected to node 1...
Press ^D to exit
'''

///////////////////////////////////////////////////////////////////////////////
// simulator configuration

platform = RealTimePlatform           // use real-time mode

// stack
ofdmStack = { container ->
  container.add 'ofdm', new OFDM()
  container.shell.addInitrc "${script.parent}/fshrc.groovy"
}

// run the simulation forever
simulate {
  node '1', remote: 1101, address: 1, location: [ 0.km, 0.km, -15.m], shell: true, stack: ofdmStack
  node '2', remote: 1102, address: 2, location: [ 1.km, 0.km, -15.m], shell: 5102, stack: ofdmStack
}
