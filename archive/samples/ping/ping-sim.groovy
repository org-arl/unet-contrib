//! Simulation: 3-node network with ping daemons
///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/ping/ping-sim
/// OR
///   click on the Run button (▶) in UnetSim
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.fjage.RealTimePlatform

///////////////////////////////////////////////////////////////////////////////
// display documentation

println '''
3-node network with ping daemons
--------------------------------

You can interact with node 1 through :

- http://localhost:8101 (web interface)
- console shell (command line)

For example, try:
> sendping 2

Press stop button (web interface), ^D (command line) to exit.
'''

///////////////////////////////////////////////////////////////////////////////
// simulator configuration

platform = RealTimePlatform

// run simulation forever
simulate {
  node '1', address: 1, location: [0, 0, 0], shell: CONSOLE, web:8101, stack: { container ->
    container.add 'ping', new PingDaemon()
    container.shell.addInitrc "${script.parent}/fshrc.groovy"
    container.websh.addInitrc "${script.parent}/fshrc.groovy"
  }
  node '2', address: 2, location: [1.km, 0, 0], stack: { container ->
    container.add 'ping', new PingDaemon()
  }
  node '3', address: 3, location: [2.km, 0, 0], stack: { container ->
    container.add 'ping', new PingDaemon()
  }
}
