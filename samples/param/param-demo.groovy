//! Simulation: Agent parameter demo
///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/param/param-demo
/// OR
///   click on the Run button (▶) in UnetSim
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.fjage.RealTimePlatform
import org.arl.fjage.shell.*

///////////////////////////////////////////////////////////////////////////////
// simulator configuration

platform = RealTimePlatform

///////////////////////////////////////////////////////////////////////////////
// display documentation

println '''
Agent parameter demo
--------------------

Agent "myAgent" loaded. You can interact with it in through :

- web shell at http://localhost:8101
- console shell on the command line

When you are done, exit the shell by pressing ^D or entering "shutdown".
'''

// run the simulation forever
simulate {
  node '1', address: 1, shell: CONSOLE, web:'/:8101', stack: { container ->
    container.add 'myAgent', new MyAgent()
  }
}
