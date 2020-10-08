import org.arl.fjage.TickerBehavior
import org.arl.unet.UnetAgent
import org.arl.unet.api.UnetSocket
import org.arl.unet.Protocol

/*
* Simulated Sensor which generates 4 bytes of
* data every 10 seconds and sends them to a peer
* over UnetSocket
*/
class Sensor extends UnetAgent{
  int peer = 1
  boolean enable = false
  float period = 10000

  void startup() {
    // On startup, creat a socket.
    def s = new UnetSocket(container);
    s.connect(peer, Protocol.DATA) 
    def r = new Random()
    
    // Every period milliseconds...
    add new TickerBehavior(period) {
      @Override
      void onTick() {
        if(enable){
          def data = (0..3).collect{r.nextInt(127)}
          log.info("Sending.. ${data} to ${peer}")
          // send some random data over the socket
          s.send(data as byte[])
        }
      }
    }
  }
}
