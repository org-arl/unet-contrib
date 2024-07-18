import groovy.transform.CompileStatic
import org.arl.fjage.AgentID
import org.arl.fjage.AgentState
import org.arl.fjage.Message
import org.arl.fjage.Performative
import org.arl.fjage.TickerBehavior
import org.arl.fjage.param.Parameter
import org.arl.unet.Services
import org.arl.unet.UnetAgent

@CompileStatic
class BasebandRecorder extends UnetAgent{

    public int recLength = 1024        // length of recording in baseband samples
    public int interval = 10000        // interval between recordings in milliseconds. 0 means no recording

    enum Params implements Parameter {
        recLength, interval
    }

    private TickerBehavior ticker
    private AgentID bb
    private AgentID bbmon
    private AgentID notify

    protected void setup() {
        super.setup()
    }

    protected void startup() {
        subscribeForService(Services.BASEBAND)
        bb = agentForService(Services.BASEBAND)
        bbmon = agent('bbmon')
        if (bb == null) {
            log.warning("Baseband service not found")
            stop()
        }
        bbmon.set(BasebandSignalMonitorParam.enable, true)
        if (interval > 0) setupPeriodicRecordings()
    }

    List<Parameter> getParameterList(){ return allOf(Params) }

    void setInterval(int interval) {
        if (interval == this.interval) return;
        if (ticker != null) ticker.stop()
        if (interval > 0) {
            this.interval = interval
            if (this.state != AgentState.INIT) setupPeriodicRecordings()    // only if we are not in INIT state
        }else {
            log.warning "Invalid interval $interval. Stopping ticker."
        }
    }

    @Override
    protected void processMessage(Message msg) {
        if (msg instanceof RxBasebandSignalNtf && msg.recipient == this.getAgentID()) {
            log.info "Recieved recording starting at ${msg.properties.containsKey("rxStartTime") ? msg.properties.get("rxStartTime") : msg.properties.get("rxTime")} [${msg.signal.length/2}]"
            bbmon.send(msg)
        }
    }

    //////// private helpers

    private void setupPeriodicRecordings(){
        // create a TickerBehavior to request recordings periodically
        ticker = new TickerBehavior(interval) {
            @Override
            void onTick() {
                requestRecording(recLength)
            }
        }
        add ticker
        requestRecording(recLength)    // run the first recording immediately
    }

    private requestRecording(int recLength){
        log.info "Requesting recording of $recLength samples"
        def rsp = bb.request(new RecordBasebandSignalReq(recLength: recLength))
        if (!rsp || rsp.performative != Performative.AGREE) {
            log.warning "Recording request failed"
        }
    }
}
