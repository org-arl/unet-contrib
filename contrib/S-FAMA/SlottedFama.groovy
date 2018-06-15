/******************************************************************************
Copyright (c) 2016, Pritish Nahar
This file is released under Simplified BSD License.
Go to http://www.opensource.org/licenses/BSD-3-Clause for full license details.
******************************************************************************/
import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.mac.*
import org.arl.unet.nodeinfo.*
/**
 * The class implements the Slotted FAMA protocol.
 * Reference:
 * Molins, Marcal, and Milica Stojanovic. "Slotted FAMA: a MAC protocol for underwater acoustic networks."
 * OCEANS 2006-Asia Pacific. IEEE, 2007.
 */
class SlottedFama extends UnetAgent {

    ////// protocol constants

    private final static int PROTOCOL           = Protocol.MAC

    private final static int MAX_RETRY          = 3
    private final static int MAX_QUEUE_LEN      = 16
    private final static int MAX_BACKOFF_SLOTS  = 12
    private final static int MIN_BACKOFF_SLOTS  = 3

    private final static int AFFIRMATIVE        = 1
    private final static int NEGATIVE           = 0

    private final static int DATA_CHANNEL       = 1
    private final static int CONTROL_CHANNEL    = 0

    private final static int CONTINUE           = 1
    private final static int RESTART            = 2
    private final static int OFF                = 0
    private final static int ON                 = 1

    ////// PDU encoder/decoder
    private final static int RTS_PDU            = 0x01
    private final static int CTS_PDU            = 0x02
    private final static int DATA_PDU           = 0x03
    private final static int ACK_PDU            = 0x04
    private final static int NACK_PDU           = 0x05

    int startTime                               = 0
    int slotLength                              = 0
    int modemBusy                               = 0
    long backoffStartTime                       = 0
    long backoffEndTime                         = 0
    int senderOfXCTS                            = 0
    int senderOfXACK                            = 0
    int flagForCtsTimeout                       = OFF

    ArrayList<Integer> senderRTS = new ArrayList<Integer>()

    ////// reservation request queue
    private Queue<ReservationReq> queue = new ArrayDeque<ReservationReq>()

    def controlMsg = PDU.withFormat
    {
        uint8('type')
        uint16('duration')    // ms
    }

    ////// protocol FSM

    private enum State {
        IDLE, TX_RTS, TX_DATA, TX_CTS, TX_ACK, WAIT_FOR_CTS, WAIT_FOR_DATA, WAIT_FOR_ACK,
        BACKOFF_X_RTS, BACKOFF_X_CTS, BACKOFF_X_DATA, BACKOFF_CTS_TIMEOUT, BACKOFF_INTERFERENCE,
        RECEIVING,RECEIVING_FROM_BACKOFF_CTS_TIMEOUT
    }

    private enum Event {
        RX_RTS, RX_CTS, RX_ACK, RX_NACK, RX_DATA, SNOOP_RTS, SNOOP_CTS, SNOOP_DATA,SNOOP_ACK, SNOOP_NACK,
        RESERVATION_REQ, BADFRAME_NTF, CARRIER_SENSED, DATA_FRAME_CORRUPTED
    }

    private FSMBehavior fsm = FSMBuilder.build {

        int retryCount = 0
        int dataDetected = NEGATIVE
        int backoff = 0
        def rxInfo
        int ackTimeout = 0
        int waitTimeForCTS = 0
        int correspondent = 0
        int endTimeBackoffCtsTimeout = 0

        state(State.IDLE)
        {
            onEnter
            {
                long currentTime  = GetCurrentTime()
                int timeForNextSlot = slotLength - ( (currentTime - startTime) % slotLength )

                after(timeForNextSlot.milliseconds)
                {
                    modemBusy = ModemCheck()
                    if (!modemBusy)
                    {
                        if(!queue.isEmpty())
                        {
                            setNextState(State.TX_RTS)
                        }
                    }
                    else
                    {
                        setNextState(State.RECEIVING)
                    }
                }
            }

            action
            {
                block()
            }

            onEvent(Event.RESERVATION_REQ)
            {
                reenterState()
            }

            onEvent(Event.CARRIER_SENSED)
            {
                setNextState(State.RECEIVING)
            }

            onEvent(Event.BADFRAME_NTF){ waitTime ->
                backoff = waitTime
                setNextState(State.BACKOFF_INTERFERENCE)
            }
        }

        state(State.TX_RTS) {
            //Transmit an RTS packet
            onEnter
            {
                Message msg = queue.peek()
                def bytes = controlMsg.encode(type: RTS_PDU, duration: Math.ceil(msg.duration*1000))
                phy << new ClearReq()
                phy << new TxFrameReq(to: msg.to, type: Physical.CONTROL, protocol: PROTOCOL, data: bytes)
                correspondent = msg.to
                after(controlMsgDuration.milliseconds)
                {
                    setNextState(State.WAIT_FOR_CTS)
                }
            }
        }

        state(State.WAIT_FOR_CTS)
        {
            //Wait for CTS till the end of the next slot.
            onEnter
            {
                long currentTime  = GetCurrentTime()
                waitTimeForCTS = slotLength - ( (currentTime - startTime) % slotLength ) + slotLength
                after(waitTimeForCTS.milliseconds)
                {
                    flagForCtsTimeout = ON
                    backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                    backoffStartTime = GetCurrentTime()
                    backoffEndTime = backoffStartTime + backoff
                    endTimeBackoffCtsTimeout = backoffEndTime
                    setNextState(State.BACKOFF_CTS_TIMEOUT)
                }
            }

            onEvent(Event.RX_CTS)
            {
                setNextState(State.TX_DATA)
            }
        }

        state(State.TX_DATA)
        {
            //Transmit DATA packet.
            onEnter
            {
                long currentTime  = GetCurrentTime()
                int timeForNextSlot = slotLength - ( (currentTime - startTime ) % slotLength )
                after(timeForNextSlot.milliseconds)
                {
                    ReservationReq msg = queue.peek()
                    sendReservationStatusNtf(msg, ReservationStatus.START)
                    after(msg.duration)
                    {
                        sendReservationStatusNtf(msg, ReservationStatus.END)
                        setNextState(State.WAIT_FOR_ACK)
                    }
                }
            }
        }

        state(State.WAIT_FOR_ACK)
        {
            //Wait for ACK for a time equal to ackTimeout as defined below.
            onEnter
            {
                long currentTime = GetCurrentTime()
                ackTimeout = 3*slotLength - ((currentTime-startTime)%slotLength)
                after(ackTimeout.milliseconds)
                {
                    if (++retryCount >= MAX_RETRY)
                    {
                        sendReservationStatusNtf(queue.poll(), ReservationStatus.FAILURE)
                        retryCount = 0
                        setNextState(State.IDLE)
                    }
                    else
                    {
                        setNextState(State.TX_DATA)
                    }
                }
            }

            onEvent(Event.RX_ACK)
            {
                queue.poll()
                setNextState(State.IDLE)
            }

            onEvent(Event.RX_NACK)
            {
                if (++retryCount >= MAX_RETRY)
                {
                    sendReservationStatusNtf(queue.poll(), ReservationStatus.FAILURE)
                    retryCount = 0
                    setNextState(State.IDLE)
                }
                else
                {
                    setNextState(State.TX_DATA)
                }
            }
        }

        state(State.TX_CTS)
        {
            //Transmit a CTS packet.
            onEnter
            {
                int destination = senderRTS.get(new Random().nextInt(senderRTS.size()))
                senderRTS.clear()
                def bytes = controlMsg.encode(type: CTS_PDU, duration: Math.round(rxInfo.duration*1000))
                phy << new ClearReq()
                phy << new TxFrameReq(to: destination, type: Physical.CONTROL, protocol: PROTOCOL, data: bytes)
                rxInfo = null
                after(controlMsgDuration.milliseconds)
                {
                    setNextState(State.WAIT_FOR_DATA)
                }
            }
        }

        state(State.WAIT_FOR_DATA)
        {
            //Wait for a DATA packet.
            onEnter
            {
                long currentTime = GetCurrentTime()
                int waitTimeForData = 2*slotLength-((currentTime-startTime)%slotLength)
                after(waitTimeForData.milliseconds)
                {
                    if(dataDetected == NEGATIVE)
                    {
                        backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                        backoffStartTime = GetCurrentTime()
                        backoffEndTime = backoffStartTime + backoff
                        setNextState(State.IDLE)
                    }
                    dataDetected = NEGATIVE
                }
            }

            onEvent(Event.RX_DATA){ info ->
                long currentTime  = GetCurrentTime()
                int timeForNextSlot = slotLength - ( (currentTime - startTime ) % slotLength )
                rxInfo = info
                after(timeForNextSlot.milliseconds)
                {
                    setNextState(State.TX_ACK)
                }
            }

            onEvent(Event.CARRIER_SENSED){channelType->
                if(channelType == DATA_CHANNEL)
                {
                    dataDetected = AFFIRMATIVE
                }
            }

            onEvent(Event.DATA_FRAME_CORRUPTED)
            {
                dataDetected = NEGATIVE
                long currentTime = GetCurrentTime()
                int timeForNextSlot = slotLength - ( (currentTime - startTime ) % slotLength )
                after(timeForNextSlot.milliseconds)
                {
                    def bytes = controlMsg.encode(type: NACK_PDU, duration: dataMsgDuration)//Math.round(info.duration))
                    phy << new ClearReq()
                    phy << new TxFrameReq(to: correspondent, type: Physical.CONTROL, protocol: PROTOCOL, data: bytes)
                    reenterState()
                }
            }
        }

        state(State.TX_ACK)
        {
            //Transmit an ACK packet
            onEnter
            {
                def bytes = controlMsg.encode(type: ACK_PDU, duration: Math.round(rxInfo.duration))
                phy << new ClearReq()
                phy << new TxFrameReq(to: rxInfo.from, type: Physical.CONTROL, protocol: PROTOCOL, data: bytes)
                rxInfo = null
                after(controlMsgDuration.milliseconds)
                {
                    if(timerCtsTimeoutOpMode == CONTINUE)
                    {
                        if(endTimeBackoffCtsTimeout > GetCurrentTime())
                        {
                            backoff = endTimeBackoffCtsTimeout - GetCurrentTime()
                            backoffStartTime = GetCurrentTime()
                            backoffEndTime = backoffStartTime + backoff
                            setNextState(State.BACKOFF_CTS_TIMEOUT)
                        }
                        else
                        {
                            setNextState(State.IDLE)
                        }
                    }
                    if(timerCtsTimeoutOpMode == RESTART)
                    {
                        if(flagForCtsTimeout == ON)
                        {
                            backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                            backoffStartTime = GetCurrentTime()
                            backoffEndTime = backoffStartTime + backoff
                            endTimeBackoffCtsTimeout = backoffEndTime
                            setNextState(State.BACKOFF_CTS_TIMEOUT)
                        }
                        else
                        {
                            setNextState(State.IDLE)
                        }
                    }
                }
            }
        }

        state(State.BACKOFF_X_RTS)
        {
            //Backoff due to having received an X_RTS packet.
            onEnter
            {
                after(backoff.milliseconds)
                {
                    modemBusy = ModemCheck()
                    if(modemBusy)
                    {
                        setNextState(State.RECEIVING)
                    }
                    else
                    {
                        if(timerCtsTimeoutOpMode == CONTINUE)
                        {
                            if(endTimeBackoffCtsTimeout > GetCurrentTime())
                            {
                                backoff = endTimeBackoffCtsTimeout - GetCurrentTime()
                                backoffStartTime = GetCurrentTime()
                                backoffEndTime = backoffStartTime + backoff
                                setNextState(State.BACKOFF_CTS_TIMEOUT)
                            }
                            else
                            {
                                setNextState(State.IDLE)
                            }
                        }
                        if(timerCtsTimeoutOpMode == RESTART)
                        {
                            if(flagForCtsTimeout == ON)
                            {
                                backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                                backoffStartTime = GetCurrentTime()
                                backoffEndTime = backoffStartTime + backoff
                                endTimeBackoffCtsTimeout = backoffEndTime
                                setNextState(State.BACKOFF_CTS_TIMEOUT)
                            }
                            else
                            {
                                setNextState(State.IDLE)
                            }
                        }
                    }
                }
            }
        }

        state(State.BACKOFF_X_CTS)
        {
            //Backoff due to having received an X_CTS packet.
            onEnter
            {
                after(backoff.milliseconds)
                {
                    if(timerCtsTimeoutOpMode == CONTINUE)
                    {
                        if(endTimeBackoffCtsTimeout > GetCurrentTime())
                        {
                            backoff = endTimeBackoffCtsTimeout - GetCurrentTime()
                            backoffStartTime = GetCurrentTime()
                            backoffEndTime = backoffStartTime + backoff
                            setNextState(State.BACKOFF_CTS_TIMEOUT)
                        }
                        else
                        {
                            setNextState(State.IDLE)
                        }
                    }
                    if(timerCtsTimeoutOpMode == RESTART)
                    {
                        if(flagForCtsTimeout == ON)
                        {
                            backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                            backoffStartTime = GetCurrentTime()
                            backoffEndTime = backoffStartTime + backoff
                            endTimeBackoffCtsTimeout = backoffEndTime
                            setNextState(State.BACKOFF_CTS_TIMEOUT)
                        }
                        else
                        {
                            setNextState(State.IDLE)
                        }
                    }
                }
            }

            onEvent(Event.SNOOP_ACK) { info ->
                senderOfXACK = info.from
                if(senderOfXCTS == senderOfXACK || senderOfXCTS == 0)
                {
                    senderOfXCTS = 0
                    senderOfXACK = 0
                    if(timerCtsTimeoutOpMode == CONTINUE)
                    {
                        if(endTimeBackoffCtsTimeout > GetCurrentTime())
                        {
                            backoff = endTimeBackoffCtsTimeout - GetCurrentTime()
                            backoffStartTime = GetCurrentTime()
                            backoffEndTime = backoffStartTime + backoff
                            setNextState(State.BACKOFF_CTS_TIMEOUT)
                        }
                        else
                        {
                            setNextState(State.IDLE)
                        }
                    }
                    if(timerCtsTimeoutOpMode == RESTART)
                    {
                        if(flagForCtsTimeout == ON)
                        {
                            backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                            backoffStartTime = GetCurrentTime()
                            backoffEndTime = backoffStartTime + backoff
                            endTimeBackoffCtsTimeout = backoffEndTime
                            setNextState(State.BACKOFF_CTS_TIMEOUT)
                        }
                        else
                        {
                            setNextState(State.IDLE)
                        }
                    }
                }
                else
                {
                    backoffStartTime = GetCurrentTime()
                    backoff = backoffEndTime - backoffStartTime
                    setNextState(State.BACKOFF_X_CTS)
                }
            }

            onEvent(Event.SNOOP_NACK) { info ->
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_CTS)
            }
        }

        state(State.BACKOFF_X_DATA)
        {
            //Backoff due to having received an X_DATA packet.
            onEnter
            {
                after(backoff.milliseconds)
                {
                    backoffEndTime = GetCurrentTime()+slotLength
                    after(slotLength.milliseconds)
                    {
                        if(timerCtsTimeoutOpMode == CONTINUE)
                        {
                            if(endTimeBackoffCtsTimeout > GetCurrentTime())
                            {
                                backoff = endTimeBackoffCtsTimeout - GetCurrentTime()
                                backoffStartTime = GetCurrentTime()
                                backoffEndTime = backoffStartTime + backoff
                                setNextState(State.BACKOFF_CTS_TIMEOUT)
                            }
                            else
                            {
                                setNextState(State.IDLE)
                            }
                        }
                        if(timerCtsTimeoutOpMode == RESTART)
                        {
                            if(flagForCtsTimeout == ON)
                            {
                                backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                                backoffStartTime = GetCurrentTime()
                                backoffEndTime = backoffStartTime + backoff
                                endTimeBackoffCtsTimeout = backoffEndTime
                                setNextState(State.BACKOFF_CTS_TIMEOUT)
                            }
                            else
                            {
                                setNextState(State.IDLE)
                            }
                        }
                    }
                }
            }

            onEvent(Event.CARRIER_SENSED){channelType->
                if(channelType == DATA_CHANNEL)
                {
                    long currentTime = GetCurrentTime()
                    backoff = dataMsgDuration+maxPropagationDelay-((currentTime-startTime)%slotLength)+2*slotLength - ((currentTime-((currentTime-startTime)%slotLength)+dataMsgDuration+maxPropagationDelay)%slotLength)
                    backoffStartTime = GetCurrentTime()
                    backoffEndTime = backoffStartTime + backoff
                    reenterState()
                }
            }

            onEvent(Event.SNOOP_ACK) { info ->
                senderOfXACK = info.from
                if(senderOfXCTS == senderOfXACK || senderOfXCTS == 0)
                {
                    senderOfXCTS = 0
                    senderOfXACK = 0
                    if(timerCtsTimeoutOpMode == CONTINUE)
                    {
                        if(endTimeBackoffCtsTimeout > GetCurrentTime())
                        {
                            backoff = endTimeBackoffCtsTimeout - GetCurrentTime()
                            backoffStartTime = GetCurrentTime()
                            backoffEndTime = backoffStartTime + backoff
                            setNextState(State.BACKOFF_CTS_TIMEOUT)
                        }
                        else
                        {
                            setNextState(State.IDLE)
                        }
                    }
                    if(timerCtsTimeoutOpMode == RESTART)
                    {
                            if(flagForCtsTimeout == ON)
                            {
                                backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                                backoffStartTime = GetCurrentTime()
                                backoffEndTime = backoffStartTime + backoff
                                endTimeBackoffCtsTimeout = backoffEndTime
                                setNextState(State.BACKOFF_CTS_TIMEOUT)
                            }
                            else
                            {
                                setNextState(State.IDLE)
                            }
                    }
                }
                else
                {
                    backoffStartTime = GetCurrentTime()
                    backoff = backoffEndTime - backoffStartTime
                    setNextState(State.BACKOFF_X_DATA)
                }
            }

            onEvent(Event.SNOOP_NACK) { info ->
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_CTS)
            }

        }

        state(State.BACKOFF_CTS_TIMEOUT) {
            //Backoff due to a CTS Timeout.
            onEnter
            {
                after(backoff.milliseconds)
                {
                    flagForCtsTimeout = OFF
                    setNextState(State.IDLE)
                }
            }

            onEvent(Event.CARRIER_SENSED)
            {
                setNextState(State.RECEIVING_FROM_BACKOFF_CTS_TIMEOUT)
            }
        }

        state(State.BACKOFF_INTERFERENCE)
        {
            //Backoff due to having received sensed interference(having received a BadFrameNtf).
            onEnter
            {
                after(backoff.milliseconds)
                {
                    if(timerCtsTimeoutOpMode == CONTINUE)
                    {
                        if(endTimeBackoffCtsTimeout > GetCurrentTime())
                        {
                            backoff = endTimeBackoffCtsTimeout - GetCurrentTime()
                            backoffStartTime = GetCurrentTime()
                            backoffEndTime = backoffStartTime + backoff
                            setNextState(State.BACKOFF_CTS_TIMEOUT)
                        }
                        else
                        {
                            setNextState(State.IDLE)
                        }
                    }
                    if(timerCtsTimeoutOpMode == RESTART)
                    {
                        if(flagForCtsTimeout == ON)
                        {
                            backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                            backoffStartTime = GetCurrentTime()
                            backoffEndTime = backoffStartTime + backoff
                            setNextState(State.BACKOFF_CTS_TIMEOUT)
                        }
                        else
                        {
                            setNextState(State.IDLE)
                        }
                    }
                }
            }
        }

        state(State.RECEIVING)
        {
            //Enter this state when you sense carrier in the IDLE state or after backoff timer expires in BACKOFF_X_RTS state.
            onEvent(Event.RX_RTS){info ->
                rxInfo = info
                senderRTS.add(rxInfo.from)
                long currentTime = GetCurrentTime()
                long delayForTX_CTS = slotLength - ( (currentTime - startTime) % slotLength )
                correspondent = info.from
                after(delayForTX_CTS.milliseconds)
                {
                    setNextState(State.TX_CTS)
                }
            }
            onEvent(Event.RX_CTS)
            {
                setNextState(State.TX_DATA)
            }

            onEvent(Event.SNOOP_RTS) { info ->
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_RTS)
            }

            onEvent(Event.SNOOP_CTS) { info ->
                senderOfXCTS = info.from
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_CTS)
            }

            onEvent(Event.SNOOP_DATA) { info ->
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_DATA)
            }

            onEvent(Event.SNOOP_ACK) { info ->
                setNextState(State.IDLE)
            }

            onEvent(Event.SNOOP_NACK) { info ->
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_CTS)
            }

            onEvent(Event.BADFRAME_NTF){ waitTime ->
                backoff = waitTime
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_INTERFERENCE)
            }
        }

        state(State.RECEIVING_FROM_BACKOFF_CTS_TIMEOUT)
        {
            //Enter this state when you sense carrier in the BACKOFF_CTS_TIMEOUT state.
            onEvent(Event.RX_RTS){info ->
                rxInfo = info
                senderRTS.add(rxInfo.from)
                long currentTime = GetCurrentTime()
                long delayForTX_CTS = slotLength - ( (currentTime - startTime) % slotLength )
                correspondent = info.from
                after(delayForTX_CTS.milliseconds)
                {
                    setNextState(State.TX_CTS)
                }
            }

            onEvent(Event.RX_CTS)
            {
                setNextState(State.TX_DATA)
            }

            onEvent(Event.SNOOP_RTS) { info ->
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_RTS)
            }

            onEvent(Event.SNOOP_CTS) { info ->
                senderOfXCTS = info.from
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_CTS)
            }

            onEvent(Event.SNOOP_DATA) { info ->
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_DATA)
            }

            onEvent(Event.SNOOP_ACK) { info ->
                if(timerCtsTimeoutOpMode == CONTINUE)
                {
                    if(endTimeBackoffCtsTimeout > GetCurrentTime())
                    {
                        backoff = endTimeBackoffCtsTimeout - GetCurrentTime()
                        backoffStartTime = GetCurrentTime()
                        backoffEndTime = backoffStartTime + backoff
                        setNextState(State.BACKOFF_CTS_TIMEOUT)
                    }
                    else
                    {
                        setNextState(State.IDLE)
                    }
                }
                if(timerCtsTimeoutOpMode == RESTART)
                {
                    if(flagForCtsTimeout == ON)
                    {
                        backoff = (AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS)*slotLength
                        backoffStartTime = GetCurrentTime()
                        backoffEndTime = backoffStartTime + backoff
                        setNextState(State.BACKOFF_CTS_TIMEOUT)
                    }
                    else
                    {
                        setNextState(State.IDLE)
                    }
                }
            }

            onEvent(Event.SNOOP_NACK) { info ->
                backoff = info.duration
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_X_CTS)
            }

            onEvent(Event.BADFRAME_NTF){ waitTime ->
                backoff = waitTime
                backoffStartTime = GetCurrentTime()
                backoffEndTime = backoffStartTime + backoff
                setNextState(State.BACKOFF_INTERFERENCE)
            }
        }

    } // of FSMBuilder

  ////// agent startup sequence

    private AgentID phy
    private AgentID node
    private int addr

    @Override
    void setup()
    {
        register Services.MAC
    }

    @Override
    void startup()
    {
        phy = agentForService(Services.PHYSICAL)
        node = agentForService(Services.NODE_INFO)

        subscribe phy
        subscribe(topic(phy, Physical.SNOOP))

        startTime = 0

        addr = node.Address

        add(fsm)

    }

  ////// process MAC service requests

    private void setControlMsgDuration(int controlMsgDuration)
    {
        this.controlMsgDuration = controlMsgDuration
        initilialisationPhase()
    }

    private void initilialisationPhase()
    {
        slotLength = controlMsgDuration + maxPropagationDelay + 1
    }

    private long GetCurrentTime()
    {
        ParameterReq req = new ParameterReq(agentForService(Services.PHYSICAL))
        req.get(PhysicalParam.time)
        ParameterRsp rsp = (ParameterRsp)request(req, 1000)
        long time = rsp.get(PhysicalParam.time)
        time = time / 1000
        return time
    }

    private int ModemCheck()
    {
        if(phy.busy)
        {
            return 1
        }
        else
        {
            return 0
        }
    }

    @Override
    Message processRequest(Message msg)
    {
        switch (msg)
        {
            case ReservationReq:
            if (msg.to == Address.BROADCAST || msg.to == addr) return new Message(msg, Performative.REFUSE)
            if (msg.duration <= 0 || msg.duration > maxReservationDuration) return new Message(msg, Performative.REFUSE)
            if (queue.size() >= MAX_QUEUE_LEN) return new Message(msg, Performative.REFUSE)
            queue.add(msg)
            fsm.trigger(Event.RESERVATION_REQ)
            return new ReservationRsp(msg)
            case ReservationCancelReq:
            case ReservationAcceptReq:
            case TxAckReq:
            return new Message(msg, Performative.REFUSE)
        }
        return null
    }

    @Override
    void processMessage(Message msg)
    {
        if (msg instanceof RxFrameNtf)
        {
            def rx = controlMsg.decode(msg.data)
            def info = [from: msg.from, to: msg.to]
            long currentTime = GetCurrentTime()
            int timeForNextSlot = slotLength - ( (currentTime - startTime ) % slotLength )

            if(msg.type == Physical.CONTROL)
            {
                /*Check for type of Control packet received and it's intended receiver. If current node is not the
                intended receiver, set appropriate backoff-times and trigger events corresponding to the packet received. */

                if (rx.type == RTS_PDU)
                {
                    if(info.to == addr)
                    {
                        info.duration = rx.duration
                        fsm.trigger(Event.RX_RTS, info)
                    }
                    else
                    {
                        info.duration = 2*slotLength
                        fsm.trigger(Event.SNOOP_RTS, info)
                    }
                }
                else if (rx.type == CTS_PDU)
                {
                    if(info.to == addr)
                    {
                        fsm.trigger(Event.RX_CTS)
                    }
                    else
                    {
                        info.duration = timeForNextSlot+dataMsgDuration+maxPropagationDelay+2*slotLength - ((currentTime-startTime+timeForNextSlot+dataMsgDuration+maxPropagationDelay)%slotLength)
                        fsm.trigger(Event.SNOOP_CTS, info)
                    }
                }
                else if (rx.type == ACK_PDU)
                {
                    if(info.to == addr)
                    {
                        fsm.trigger(Event.RX_ACK, info)
                    }
                    else
                    {
                        fsm.trigger(Event.SNOOP_ACK, info)
                    }
                }
                else if (rx.type == NACK_PDU)
                {
                    if(info.to == addr)
                    {
                        fsm.trigger(Event.RX_NACK, info)
                    }
                    else
                    {
                        info.duration = timeForNextSlot+dataMsgDuration+maxPropagationDelay+2*slotLength - ((currentTime-startTime+timeForNextSlot+dataMsgDuration+maxPropagationDelay-startTime)%slotLength)
                        fsm.trigger(Event.SNOOP_NACK, info)
                    }
                }
                else
                {
                  //pass
                }
            }

            if(msg.type == Physical.DATA)
            {
                /*If DATA packet is intended for current node, trigger event RX_DATA.
                Else set appropriate backoff time and trigger event SNOOP_DATA */

                if(info.to == addr)
                {
                    info.duration = phy[0].frameDuration
                    info.from     = msg.getFrom()
                    fsm.trigger(Event.RX_DATA, info)
                }
                else
                {
                    info.duration = timeForNextSlot + slotLength
                    if(backoffEndTime - info.duration - GetCurrentTime() == slotLength)
                    {
                        info.duration += slotLength
                    }
                    fsm.trigger(Event.SNOOP_DATA, info)
                }
            }
        }

        if(msg instanceof BadFrameNtf)
        {
            /*If node receives BadFrameNtf in the state WAIT_FOR_DATA, trigger Event DATA_FRAME_CORRUPTED.
            Else trigger event BADFRAME_NTF. */
            def currentTime = GetCurrentTime()
            def timeForNextSlot = slotLength - ((GetCurrentTime()-startTime)%slotLength)
            def backoff = timeForNextSlot+dataMsgDuration+maxPropagationDelay+2*slotLength - ((currentTime-startTime+timeForNextSlot+dataMsgDuration+maxPropagationDelay)%slotLength)

            if(fsm.getCurrentState().toString() == "WAIT_FOR_DATA")
            {
                fsm.trigger(Event.DATA_FRAME_CORRUPTED)
            }
            else
            {
                fsm.trigger(Event.BADFRAME_NTF, backoff)
            }
        }

        if(msg instanceof RxFrameStartNtf)
        {
            //This notification denotes carrier sense and hence the corresponding event is triggered.
            fsm.trigger(Event.CARRIER_SENSED,msg.type)
        }
    }

    ////// expose parameters that are expected of a MAC service

    final int reservationPayloadSize = 0            // read-only parameters
    final int ackPayloadSize = 0
    final float maxReservationDuration = 65.535


    //Parameters to be passed to Agent File
    int controlMsgDuration, dataMsgDuration, maxPropagationDelay, timerCtsTimeoutOpMode

    @Override
    List<Parameter> getParameterList() {
        allOf(SlottedFamaParam,MacParam)
    }

    boolean getChannelBusy() {                      // channel is considered busy if fsm is not IDLE
        return fsm.currentState.name != State.IDLE
    }

    float getRecommendedReservationDuration() {     // recommended reservation duration is one DATA packet
        return get(phy, Physical.DATA, PhysicalChannelParam.frameDuration)
    }

  ////// utility methods

    private void sendReservationStatusNtf(ReservationReq msg, ReservationStatus status) {
        send new ReservationStatusNtf(recipient: msg.sender, requestID: msg.msgID, to: msg.to, from: addr, status: status)
    }

}
