import org.arl.fjage.*
import org.arl.fjage.param.Parameter
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.mac.*

class MySimpleThrottledMac extends UnetAgent {

  private final static double TARGET_LOAD     = 0.5
  private final static int    MAX_QUEUE_LEN   = 16

  private AgentID phy
  boolean busy = false   // is a reservation currently ongoing?
  Long t0 = null         // time of last reservation start, or null
  Long t1 = null         // time of last reservation end, or null
  int waiting = 0

  @Override
  void setup() {
    register Services.MAC
  }

  @Override
  void startup() {
    phy = agentForService Services.PHYSICAL
  }

  @Override
  Message processRequest(Message msg) {
    switch (msg) {
      case ReservationReq:
        if (msg.duration <= 0) return new RefuseRsp(msg, 'Bad reservation duration')
        if (waiting >= MAX_QUEUE_LEN) return new Message(msg, Performative.FAILURE)
        ReservationStatusNtf ntf1 = new ReservationStatusNtf(
          recipient: msg.sender,
          inReplyTo: msg.msgID,
          to: msg.to,
          status: ReservationStatus.START)
        ReservationStatusNtf ntf2 = new ReservationStatusNtf(
          recipient: msg.sender,
          inReplyTo: msg.msgID,
          to: msg.to,
          status: ReservationStatus.END)

        // grant the request after a random backoff
        AgentLocalRandom rnd = AgentLocalRandom.current()
        double backoff = rnd.nextExp(TARGET_LOAD/msg.duration/nodes)
        long t = currentTimeMillis()
        if (t0 == null || t0 < t) t0 = t
        t0 += Math.round(1000*backoff)  // schedule packet with a random backoff
        if (t0 < t1) t0 = t1            //   after the last scheduled packet
        long duration = Math.round(1000*msg.duration)
        t1 = t0 + duration
        waiting++
        add new WakerBehavior(t0-t, {
          send ntf1
          busy = true
          waiting--
          add new WakerBehavior(duration, {
            send ntf2
            busy = false
          })
        })

        return new ReservationRsp(msg)
      case ReservationCancelReq:
      case ReservationAcceptReq:
      case TxAckReq:
        return new RefuseRsp(msg, 'Not supported')
    }
    return null
  }

  // expose parameters defined by the MAC service, and one additional parameter

  @Override
  List<Parameter> getParameterList() {
    return allOf(MacParam, Param)
  }

  enum Param implements Parameter {
    nodes
  }

  int nodes = 6                          // number of nodes in network, to be set by user

  final int reservationPayloadSize = 0
  final int ackPayloadSize = 0
  final float maxReservationDuration = Float.POSITIVE_INFINITY

  boolean getChannelBusy() {
    return busy
  }

  float getRecommendedReservationDuration() {
    return get(phy, Physical.DATA, PhysicalChannelParam.frameDuration)
  }

}
