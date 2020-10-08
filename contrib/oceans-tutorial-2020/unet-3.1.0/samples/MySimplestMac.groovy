import org.arl.fjage.*
import org.arl.fjage.param.Parameter
import org.arl.unet.*
import org.arl.unet.mac.*

class MySimplestMac extends UnetAgent {

  @Override
  void setup() {
    register Services.MAC
  }

  @Override
  Message processRequest(Message msg) {
    switch (msg) {
      case ReservationReq:
        if (msg.duration <= 0) return new RefuseRsp(msg, 'Bad reservation duration')
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
        add new OneShotBehavior({
          send ntf1
        })
        add new WakerBehavior(Math.round(1000*msg.duration), {
          send ntf2
        })
        return new ReservationRsp(msg)
      case ReservationCancelReq:
      case ReservationAcceptReq:                      // respond to other requests defined
      case TxAckReq:                                  //  by the MAC service with a RefuseRsp
        return new RefuseRsp(msg, 'Not supported')
    }
    return null
  }

  // expose parameters defined by the MAC service, with just default values

  @Override
  List<Parameter> getParameterList() {
    return allOf(MacParam)                            // advertise the list of parameters
  }

  final boolean channelBusy = false                   // parameters are marked as 'final'
  final int reservationPayloadSize = 0                //  to ensure that they are read-only
  final int ackPayloadSize = 0
  final float maxReservationDuration = Float.POSITIVE_INFINITY
  final Float recommendedReservationDuration = null

}
