import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.mac.*

class LoadGenerator extends UnetAgent {

  private List<Integer> destNodes                     // list of possible destination nodes
  private float load                                  // normalized load to generate
  private AgentID mac, phy

  def data_msg = PDU.withFormat
  {
    uint32('data')
  }

  LoadGenerator(List<Integer> destNodes, float load) {
    this.destNodes = destNodes                        
    this.load = load                                  
  }

  @Override
  void startup() {
    phy = agentForService Services.PHYSICAL
    mac = agentForService Services.MAC
 
    float dataPktDuration = get(phy, Physical.DATA, PhysicalChannelParam.frameDuration)
    println "Load Generator : dataPktDuration = ${dataPktDuration}"
    float rate = load/dataPktDuration                 // compute average packet arrival rate
    add new PoissonBehavior(1000/rate, {              // create Poisson arrivals at given rate
      mac << new ReservationReq(to: rnditem(destNodes), duration: dataPktDuration)
    })

  }

  @Override
  void processMessage(Message msg) {
    if (msg instanceof ReservationStatusNtf && msg.status == ReservationStatus.START) {
      phy << new ClearReq()                                   // stop any ongoing transmission or reception
      phy << new TxFrameReq(to: msg.to, type: Physical.DATA , data : data_msg.encode([ data : 25]), protocol : Protocol.USER )             

    }
  }

}
