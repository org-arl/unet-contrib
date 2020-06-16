package myagent

import org.arl.fjage.Message
import org.arl.fjage.param.Parameter
import org.arl.unet.UnetAgent

@groovy.transform.CompileStatic
class MyAgent extends UnetAgent {

  @Override
  void setup() {
    // code to be executed to initialize agent
    // other agents may not be initialized yet
  }

  @Override
  void startup() {
    // code to be executed after all agents are initialized
  }

  @Override
  Message processRequest(Message req) {
    // process requests (messages with performative REQUEST)
    // return response message, if any
    return null
  }

  @Override
  void processMessage(Message msg) {
    // process non-request messages
  }

  @Override
  List<Parameter> getParameterList() {
    return allOf(MyAgentParam)
  }

}
