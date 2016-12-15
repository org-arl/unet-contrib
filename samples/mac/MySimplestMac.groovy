import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.mac.*

class MySimplestMac extends UnetAgent {

  @Override
  void setup() {
    register Services.MAC                             // advertise that the agent provides a MAC service
  }

  @Override
  Message processRequest(Message msg) {
    switch (msg) {
      case ReservationReq:
        if (msg.duration <= 0) return new Message(msg, Performative.REFUSE)   // check requested duration
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
        send ntf1                                                 // send START reservation notification
        add new WakerBehavior(Math.round(1000*msg.duration), {    // wait for reservation duration
          send ntf2                                               // send END reservation notification
        })
        return new ReservationRsp(msg)                            // defaults to an AGREE performative
      case ReservationCancelReq:
      case ReservationAcceptReq:                                  // respond to other requests defined
      case TxAckReq:                                              //  by the MAC service trivially with
        return new Message(msg, Performative.REFUSE)              //  a REFUSE performative
    }
    return null
  }

  // expose parameters defined by the MAC service, with just default values

  @Override
  List<Parameter> getParameterList() {
    return allOf(MacParam)                                        // advertise the list of parameters
  }

  final boolean channelBusy = false                               // parameters are marked as 'final'
  final int reservationPayloadSize = 0                            //  to ensure that they are read-only
  final int ackPayloadSize = 0
  final float maxReservationDuration = Float.POSITIVE_INFINITY
  final Float recommendedReservationDuration = null

}
