///////////////////////////////////////////////////////////////////////////////
///
/// Sample agent class to demonstrate agent parameters
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.unet.*

class MyAgent extends UnetAgent {

  // parameters
  int retryCount = 3
  float retryTimeout = 1.0

  List<Parameter> getParameterList() {
    allOf(MyAgentParameters)
  }

}
