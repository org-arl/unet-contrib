package org.arl.unet.mac

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import org.arl.fjage.AgentID
import org.arl.fjage.Message
import org.arl.fjage.Performative
import org.arl.fjage.WakerBehavior
import org.arl.fjage.param.Parameter
import org.arl.unet.Services
import org.arl.unet.UnetAgent
import org.arl.unet.Utils

class MacReserver extends UnetAgent{

    public static int RESERVATION_DURATION_S = 60

    private AgentID mac = null
    private WakerBehavior waker = null
    private String reservationID = null

    enum Params implements Parameter {
        reserved
    }

    @Override
    protected void setup() {
        super.setup()
    }

    @Override
    protected void startup() {
        mac = agentForService(Services.MAC)
        subscribeForService(Services.MAC)
        reserve()
    }

    @Override
    protected void shutdown() {
        cancelReservation()
    }

    @Override
    protected Message processRequest(Message msg) {
        log.info "Received request: ${msg}"
        if (msg instanceof CancelReq) {
            if (reservationID != null) {
                cancelReservation()
                if (msg.cancelInterval > 0) {
                   reserveAfter(msg.cancelInterval)
                }
            }
            return new Message(msg, Performative.AGREE)
        } else if (msg instanceof ReserveReq) {
            reserve()
            return new Message(msg, Performative.AGREE)
        }
        return new Message(msg, Performative.NOT_UNDERSTOOD)
    }

    @Override
    protected void processMessage(Message msg) {
        if (msg instanceof ReservationStatusNtf) {
            if (msg.status == ReservationStatus.START) {
                log.info "Medium reserved for user"
            }else if (msg.status == ReservationStatus.END) {
                log.info "Medium reservation ended"
                if (this.reservationID != null){
                    // the slot ended. re-reserve
                    this.reservationID = null
                    reserve()
                }
            }
        }
    }

    boolean getReserved(){
        return reservationID != null
    }

    @Override
    List<Parameter> getParameterList(){ return allOf(Params) }

    //////// private helpers

    private void cancelReservation(){
        if (!this.reservationID){
            log.fine "No ongoing reservation to cancel"
            return;
        }
        log.info "Canceling users's reservation of the medium..."
        def rsp = mac.request(new ReservationCancelReq(id: reservationID))
        if (!rsp || rsp.performative != Performative.AGREE) {
            log.warning "Failed to cancel reservation: ${rsp}"
        }
        this.reservationID = null
    }

    private void reserve(){
        if (reservationID != null) {
            log.warning "Already reserved for user"
            return
        }
        log.info "Reserving medium for user..."
        def req = new ReservationReq(duration: RESERVATION_DURATION_S)
        def rsp = mac.request(req)
        if (!rsp || rsp.performative != Performative.AGREE) {
            log.warning "Failed to make reservation: ${rsp}"
        }else {
            reservationID = req.messageID;
        }
    }

    private void reserveAfter(int interval){
        if (waker) waker.stop()
        waker = new WakerBehavior(interval) {
            @Override
            void onWake() {
                reserve()
            }
        }
        add waker
    }
}

@CompileStatic
class CancelReq extends Message {
    int cancelInterval = 10000;     // when to re-reserve the medium after canceling it

    public CancelReq() {
        super(Performative.REQUEST)
    }

    String toString() {
        return Utils.formatMessage(this, "cancelInterval:", cancelInterval);
    }
}

@CompileStatic
class ReserveReq extends Message {
    public ReserveReq() {
        super(Performative.REQUEST)
    }
}
