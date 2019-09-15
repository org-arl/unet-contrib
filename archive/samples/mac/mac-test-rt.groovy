//! Simulation: 5-node random network with MySimplestMac agent loaded
///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/mac/mac-test-rt.groovy
/// OR
///   click on the Run button (▶) in UnetSim
///
///////////////////////////////////////////////////////////////////////////////
println '''
5-node random network with MySimplestMac
--------------------

You can interact with node 1 through :

- http://localhost:8081 (web interface)
- console shell (command line)

Press stop button (web interface), ^D (command line) to exit.
'''

import org.arl.fjage.RealTimePlatform
import org.arl.unet.sim.channels.BasicAcousticChannel

platform = RealTimePlatform                 // use real-time platform for user interaction
channel = [ model: BasicAcousticChannel ]   // use an acoustic channel model with default parameters

// simulate until terminated by user, with random node locations (with a user console shell for node 1)
simulate {

  // define network stack
  def myStack = { container ->
    container.add 'mac', new MySimplestMac()
  }

  // define simulation nodes
  node "1", location: [rnd(-500.m, 500.m), rnd(-500.m, 500.m), rnd(-20.m, 0)], stack: myStack, shell: CONSOLE, web:8081
  node "2", location: [rnd(-500.m, 500.m), rnd(-500.m, 500.m), rnd(-20.m, 0)], stack: myStack
  node "3", location: [rnd(-500.m, 500.m), rnd(-500.m, 500.m), rnd(-20.m, 0)], stack: myStack
  node "4", location: [rnd(-500.m, 500.m), rnd(-500.m, 500.m), rnd(-20.m, 0)], stack: myStack
  node "5", location: [rnd(-500.m, 500.m), rnd(-500.m, 500.m), rnd(-20.m, 0)], stack: myStack

}
