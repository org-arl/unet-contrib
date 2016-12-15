//! Simulation: 3-node network with ping daemons
///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/ping/ping-sim
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.fjage.RealTimePlatform

///////////////////////////////////////////////////////////////////////////////
// display documentation

println '''
3-node network with ping daemons
--------------------------------

You can interact with node 1 in the console shell. For example, try:
> ping 2
> help ping

When you are done, exit the shell by pressing ^D or entering:
> shutdown
'''

///////////////////////////////////////////////////////////////////////////////
// simulator configuration

platform = RealTimePlatform

// run simulation forever
simulate {
  node '1', address: 1, location: [0, 0, 0], shell: true, stack: { container ->
    container.add 'ping', new PingDaemon()
    container.shell.addInitrc "${script.parent}/fshrc.groovy"
  }
  node '2', address: 2, location: [1.km, 0, 0], stack: { container ->
    container.add 'ping', new PingDaemon()
  }
  node '3', address: 3, location: [2.km, 0, 0], stack: { container ->
    container.add 'ping', new PingDaemon()
  }
}
