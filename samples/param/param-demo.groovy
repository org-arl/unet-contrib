//! Simulation: Agent parameter demo
///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/param/param-demo
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

Agent "myAgent" loaded. You can interact with it in the console shell.
When you are done, exit the shell by pressing ^D or entering "shutdown".
'''

// run the simulation forever
simulate {
  node '1', address: 1, shell: true, stack: { container ->
    container.add 'myAgent', new MyAgent()
  }
}
