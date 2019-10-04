import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.mac.*

class MySimpleThrottledMac extends UnetAgent {

  private final static double TARGET_LOAD     = 0.5
  private final static int    MAX_QUEUE_LEN   = 16

  private AgentID phy
  boolean busy = false         // is a reservation currently ongoing?
  Long t0 = null               // stores the time that the last reservation starts, or null if no reservations
  Long t1 = null               // stores the time that the last reservation ends, or null if no reservations
  int waiting = 0

  @Override
  void setup() {
    register Services.MAC                             // advertise that the agent provides a MAC service
  }

  @Override
  void startup() {
    phy = agentForService Services.PHYSICAL           // get AgentID of the PHY agent (to get frame duration from)
  }

  @Override
  Message processRequest(Message msg) {
    switch (msg) {
      case ReservationReq:
        if (msg.duration <= 0) return new Message(msg, Performative.REFUSE)
        if (waiting >= MAX_QUEUE_LEN) return new Message(msg, Performative.REFUSE)
        ReservationStatusNtf ntf1 = new ReservationStatusNtf(     // prepare START reservation notification
          recipient: msg.sender,
          requestID: msg.msgID,
          to: msg.to,
          status: ReservationStatus.START)
        ReservationStatusNtf ntf2 = new ReservationStatusNtf(     // prepare END reservation notification
          recipient: msg.sender,
          requestID: msg.msgID,
          to: msg.to,
          status: ReservationStatus.END)
        AgentLocalRandom rnd = AgentLocalRandom.current()
        double backoff = rnd.nextExp(TARGET_LOAD/msg.duration/nodes)
        long t = currentTimeMillis()
        if (t0 == null || t0 < t) t0 = t
        t0 += Math.round(1000*backoff)                            // schedule packet with a random backoff
        if (t0 < t1) t0 = t1                                      //   after the last scheduled packet
        long duration = Math.round(1000*msg.duration)
        t1 = t0 + duration
        waiting++
        add new WakerBehavior(t0-t, {
          send ntf1                                               // send START reservation notification
          busy = true
          waiting--
          add new WakerBehavior(duration, {                       // wait for reservation duration
            send ntf2                                             // send END reservation notification
            busy = false
          })
        })
        return new ReservationRsp(msg)                            // defaults to an AGREE performative
      case ReservationCancelReq:
      case ReservationAcceptReq:                                  // respond to other requests defined
      case TxAckReq:                                              //  by the MAC service trivially with
        return new Message(msg, Performative.REFUSE)              //  a REFUSE performative
    }
    return null
  }

  // expose parameters defined by the MAC service, and one additional parameter

  @Override
  List<Parameter> getParameterList() {
    return allOf(MacParam, Param)                                // advertise the list of parameters
  }

  @com.google.gson.annotations.JsonAdapter(org.arl.unet.JsonTypeAdapter.class)
  enum Param implements Parameter {
    nodes                                                        // additional parameter exposed by agent
  }

  int nodes = 6                                                  // number of nodes in network, to be set by user

  final int reservationPayloadSize = 0                           // some parameters are marked as 'final'
  final int ackPayloadSize = 0                                   //  to ensure that they are read-only
  final float maxReservationDuration = Float.POSITIVE_INFINITY

  boolean getChannelBusy() {                      // channel is considered busy if fsm is not IDLE
    return busy
  }

  float getRecommendedReservationDuration() {     // recommended reservation duration is one DATA packet
    return get(phy, Physical.DATA, PhysicalChannelParam.frameDuration)
  }

}
