/******************************************************************************
Copyright (c) 2016, Pritish Nahar
This file is released under Simplified BSD License.
Go to http://www.opensource.org/licenses/BSD-3-Clause for full license details.
******************************************************************************/

import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import static org.arl.unet.Services.*
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*
import org.arl.unet.PDU
import static org.arl.unet.phy.Physical.*
import java.nio.ByteOrder
import org.arl.fjage.Message
import org.arl.fjage.Performative
import java.lang.*
import java.util.Random 
import org.arl.unet.nodeinfo.NodeInfo
import java.util.*
import org.arl.unet.mac.*


/**
 * The class implements the ALOHA AN protocol.
 *
 * Reference:
 * N. Chirdchoo, W.S. Soh, K.C. Chua (2007), Aloha-based MAC Protocols with
 * Collision Avoidance for Underwater Acoustic Networks, in Proceedings of
 * IEEE INFOCOM 2007.
 */
class AlohaAN extends UnetAgent {
            
    private List<Integer> nodeList
    private int myAddr
    private AgentID node,phy

    private final static int MAX_RETRY_ATTEMPTS    = 10
    private final static int MAX_BACKOFF_SLOTS     = 29
    private final static int MIN_BACKOFF_SLOTS     = 3

    private final static int NTF_PACKET            = 1
    private final static int DATA_PACKET           = 2

    private final static int SENDING               = 1
    private final static int RECEIVING             = 2
    private final static int OVERHEARING           = 3

    private final static int NTF_NTF_TX_CLASH      = 1
    private final static int NTF_DATA_TX_CLASH     = 2
    private final static int DATA_DATA_TX_CLASH    = 3

    private final static int STORE_SCHEDULE        = 1
    private final static int DISCARD_SCHEDULE      = 2
    private final static int DEFER_TRANSMISSIONS   = 3

    ArrayList<Integer> maxDelay = new ArrayList<Integer>()
    ArrayList<ArrayList<AlohaAN.ScheduleSlot>> schedule  = new ArrayList<ArrayList<AlohaAN.ScheduleSlot>>() 
    ArrayList<AlohaAN.TransmissionSlot> transmissionSlotsList = new ArrayList<AlohaAN.TransmissionSlot>()

    int lagTime                    = 0
    int scheduleSlotKey            = 1
    int transmissionSlotKey        = 1

    int nodePosition               = 0
    int nodeCount                  = 0

    int transmissionClashCheckFlag = 0
    int collisionCheckFlag         = 0  
    int receivingNtfCheckFlag      = 0
    int scheduleCheckFlag          = 0

    private class TransmissionSlot
    {
        private int destination,backoffCount,key
        private long startTimeNtf,endTimeNtf,startTimeData,endTimeData
        private AgentID sender
        private String msgID        
    }

    private class ScheduleSlot
    {
        private long busyTimeStart,busyTimeEnd
        private int key,status 
    }   

    def ntfMsg = PDU.withFormat
    {
        uint8('destinationNodeAddress')
    }

    public void startup() 
    {
        phy = agentForService(Services.PHYSICAL)
        subscribe phy                                                  

        node = agentForService(Services.NODE_INFO)  
        myAddr = node.Address       
        nodePosition = nodeList.indexOf(myAddr)

    }

    public void setup()
    {
        register Services.MAC
    }

    private void setNodeList(List nodeList)
    {
        this.nodeList   = nodeList

        nodeCount = nodeList.size()

        initilialisationPhase()
    }

    private void initilialisationPhase()
    {
        maxDelay.clear()
        transmissionSlotsList.clear()
        nodePosition = nodeList.indexOf(myAddr)

        int scheduleSize = schedule.size()

        if(scheduleSize == null)
        {
            scheduleSize = 0
        }
        
        if(scheduleSize > nodeCount)
        {
            for(int i = nodeCount; i<scheduleSize;i++)
            {
                schedule.remove(nodeCount)
            }
        }
        else
        {
            for(int i = 0;i<nodeCount-scheduleSize;i++)
            {
                schedule.add(new ArrayList<AlohaAN.ScheduleSlot>())
            }
        }   

        for(int i = 0;i<nodeCount;i++)
        {
            schedule[i].clear()

        }


        int index = -1
        for(int i = 0; i<nodeCount; i++)
        {
            for(int j = 0; j<nodeCount; j++)
            {
                index += 1
                if(j == 0)
                {
                    maxDelay.add(propagationDelay[index])
                }
                else
                {
                    if(propagationDelay[index] > maxDelay[i])
                    {
                        maxDelay[i] = propagationDelay[index]
                    }
                }   
            } 
        }

        for(int i = 0;i < nodeCount; i++)
        {
            if(lagTime < maxDelay[i])
            {
                lagTime = maxDelay[i]
            }
        }

        lagTime = lagTime + controlMsgDuration
    }
    
    private void sendNtf(ReservationReq msg)
    {
//for maintaining a list that stores node's ntf and data transmission times, along with other info
        int destination    = msg.to
        long startTimeNtf  = getCurrentTime()  
        long endTimeNtf    = startTimeNtf  + controlMsgDuration     
        long startTimeData = startTimeNtf  + lagTime
        long endTimeData   = startTimeData + dataMsgDuration
        int backoffCount   = 0

        TransmissionSlot tSlot = new TransmissionSlot()
        tSlot.destination   = destination
        tSlot.startTimeNtf  = startTimeNtf
        tSlot.endTimeNtf    = endTimeNtf
        tSlot.startTimeData = startTimeData
        tSlot.endTimeData   = endTimeData
        tSlot.backoffCount  = backoffCount
        tSlot.msgID         = msg.msgID
        tSlot.sender        = msg.sender
        tSlot.key           = transmissionSlotKey

//check if modem is busy sending
        transmissionClashCheckFlag = transmissionClashCheck(NTF_PACKET)

        if (transmissionClashCheckFlag == 0) 
        {
//check if transmission can result in collisions                
            collisionCheckFlag = collisionCheck(NTF_PACKET,0)
            if(collisionCheckFlag == 0)
            {
//check physically if modem is busy
                receivingNtfCheck()
                if((phy.busy) && (receivingNtfCheckFlag != OVERHEARING))//if these conditions are met , it assumes modem is busy receving a ntf packet
                {
                    //packet is backed off and sending slots updated as node is receving NTF
                    int bo = deferTransmissionsCollisionCheck(tSlot) 
                    tSlot.backoffCount = 1  
                    backoff(bo,tSlot)
                }
                else
                {
                    phy << new ClearReq()
                    rxDisable()
                    phy << new TxFrameReq(to: Address.BROADCAST, type: Physical.CONTROL , data : ntfMsg.encode([ destinationNodeAddress : destination ]))
                    sendData(tSlot)                     
                }               
            }
            else
            {
                //packet is backed off and sending slots updated as collision check returns negative
                int bo = deferTransmissionsCollisionCheck(tSlot) 
                tSlot.backoffCount = 1  
                backoff(bo,tSlot)                               
            }               
        }           
        else 
        {
            //packet is backed off and transmission slots updated as modem is busy transmitting
            int bo = deferTransmissionsCollisionCheck(tSlot) 
            tSlot.backoffCount = 1  
            backoff(bo,tSlot)           
        }

        transmissionSlotsList.add(tSlot)
        transmissionSlotKey += 1
    }

    private void sendData(TransmissionSlot tSlot)   
    {
        def phy  = agentForService(Services.PHYSICAL)
         
//Try to send data after lagTime if all checks permit the node to send
        add new WakerBehavior(lagTime, {           
            if(tSlot != null)
            {
                int currentTime = getCurrentTime()  
                if(currentTime < tSlot.startTimeData) //this implies that the transmission was deferred due to a clash with another node implied by a ntf packet
                {
                    int bo = (Integer)((tSlot.startTimeNtf - currentTime) / dataMsgDuration)
                    backoff(bo,tSlot) 
                }
                else
                {
                    transmissionClashCheckFlag = transmissionClashCheck(DATA_PACKET)
                    
                    if(transmissionClashCheckFlag == 0)
                    {
                        collisionCheckFlag = collisionCheck(DATA_PACKET,tSlot.destination)
                        if(collisionCheckFlag == 0)
                        {
                            //node is clear to send data, as checks have returned affirmative, so send a request to abort any ongoing reception and turn off receiver
                            phy << new ClearReq()
                            rxDisable()

                            ReservationStatusNtf ntf1 = new ReservationStatusNtf(     // prepare START reservation notification
                              recipient: tSlot.sender,
                              requestID: tSlot.msgID,
                              to: tSlot.destination,
                              status: ReservationStatus.START)
                            ReservationStatusNtf ntf2 = new ReservationStatusNtf(     // prepare END reservation notification
                              recipient: tSlot.sender,
                              requestID: tSlot.msgID,
                              to: tSlot.destination,
                              status: ReservationStatus.END)
                            send ntf1                                    // send START reservation notification
                            add new WakerBehavior(dataMsgDuration, 
                            {    // wait for reservation duration
                              send ntf2   
                              clearTransmissionSlot(tSlot.key)  //after transmission is over, clear the entry in the list for the transmission
                            })      
                        }
                        else
                        {
                            tSlot.backoffCount = tSlot.backoffCount + 1         
                            if(tSlot.backoffCount > MAX_RETRY_ATTEMPTS)
                            {
                                //discard packet as it's backoff count has exceeded the maximum allowed number allowed
                                clearTransmissionSlot(tSlot.key)
                            }
                            else
                            {
                                //packet is backed off and sending slots updated as collision check returns negative
                                int bo = deferTransmissionsCollisionCheck(tSlot)
                                backoff(bo,tSlot)           
                            }               
                        }           
                    }
                    else
                    {
                        tSlot.backoffCount = tSlot.backoffCount + 1             
                        if(tSlot.backoffCount > MAX_RETRY_ATTEMPTS)
                        {
                            //discard packet as it's backoff count has exceeded the maximum allowed number allowed
                            clearTransmissionSlot(tSlot.key)

                        }
                        else
                        {
                            //packet is backed off and sending slots updated as collision check returns negative
                            int bo = deferTransmissionsCollisionCheck(tSlot)
                            backoff(bo,tSlot)
        
                        }               
                    } 
                }
            }                           
        })  
    }

    private void backoff(int bo, TransmissionSlot tSlot)
    {
                       
        add new BackoffBehavior(bo*dataMsgDuration, {
            if(tSlot != null)
            {
                int currentTime = getCurrentTime()  
                if(currentTime < tSlot.startTimeNtf)//this implies that the transmission was deferred due to a clash with another node implied by a ntf packet
                {
                    int bo1 = (Integer)(tSlot.startTimeNtf) - currentTime
                    backoff(bo1)
                }
                else
                {
                    transmissionClashCheckFlag = transmissionClashCheck(NTF_PACKET) 
                    if (transmissionClashCheckFlag != 0) 
                    {
                        tSlot.backoffCount = tSlot.backoffCount + 1             
                        if(tSlot.backoffCount > MAX_RETRY_ATTEMPTS)
                        {
                            clearTransmissionSlot(tSlot.key)
                        }
                        else
                        {
                            int bo1 = deferTransmissionsCollisionCheck(tSlot)
                            backoff(bo1*dataMsgDuration)

                        }               
                    }                   
                    else 
                    {

                        collisionCheckFlag = collisionCheck(NTF_PACKET,0)
                        if(collisionCheckFlag == 0)
                        {

                            receivingNtfCheck()
                            if((phy.busy) && (receivingNtfCheckFlag != OVERHEARING))
                            {

                                int bo1 = deferTransmissionsCollisionCheck(tSlot)
                                backoff(bo1*dataMsgDuration)

                            }
                            else
                            {
                                phy << new ClearReq()
                                rxDisable()
                                def rsp1 = phy << new TxFrameReq(to: Address.BROADCAST, type: Physical.CONTROL , data : ntfMsg.encode([ destinationNodeAddress : tSlot.destination]))
                                sendData(tSlot)                     
                                
                            }
                        }

                        else
                        {
                            tSlot.backoffCount = tSlot.backoffCount + 1             
                            if(tSlot.backoffCount > MAX_RETRY_ATTEMPTS)
                            {
                                clearTransmissionSlot(tSlot.key)
                            }
                            else
                            {
                                int bo1 = deferTransmissionsCollisionCheck(tSlot)
                                backoff(bo1*dataMsgDuration)

                            }               
                        }                       
                    }
                }               
            }                                   
        })//backoff behavior
    }

    private void rxDisable()
    {
        //DisableReceiver
        ParameterReq req = new ParameterReq(agentForService(Services.PHYSICAL))
        req.get(PhysicalParam.rxEnable)
        ParameterRsp rsp = (ParameterRsp) request(req, 1000)            
        rsp.set(PhysicalParam.rxEnable,false) 
    }

    private void rxEnable()
    {
        //EnableReceiver
        ParameterReq req = new ParameterReq(agentForService(Services.PHYSICAL))
        req.get(PhysicalParam.rxEnable)
        ParameterRsp rsp = (ParameterRsp)request(req, 1000)         
        rsp.set(PhysicalParam.rxEnable,true) 
    }


    private int collisionCheck(int typeOfPacket,int destination)
    {
        //This function will check if a transmission will result in a collision.This is done by checking with the schedule entries set by virtue of the NTF packet.
        collisionCheckFlag = 0
        
        long currentTime    = getCurrentTime()
        long sendTimeStart  = currentTime 
        long sendTimeEnd    = sendTimeStart + controlMsgDuration
        
        if(typeOfPacket == DATA_PACKET)
        {
            sendTimeEnd = sendTimeEnd + dataMsgDuration - controlMsgDuration
        }

        for(int i = 0; i<nodeCount; i++)    
        {   
            for(int j = 0; j<schedule[i].size(); j++)
            {
                if(sendTimeStart + propagationDelay[nodePosition*nodeCount+i] > schedule[i][j].busyTimeEnd) 
                {
                    continue
                }   
                else if(sendTimeEnd + propagationDelay[nodePosition*nodeCount+i] < schedule[i][j].busyTimeStart)    
                {
                    continue
                }
                else if(schedule[i][j].status == OVERHEARING || schedule[i][j].status == SENDING)
                {
            
                    if((typeOfPacket == DATA_PACKET) && (nodeList[i] != destination))
                    {
                        continue    
                    }
                    else
                    {
                        collisionCheckFlag = collisionCheckFlag + 1
                        break

                    }   
                }  
                else
                {
                    collisionCheckFlag = collisionCheckFlag + 1
                    break
                }
            }
        }

        return collisionCheckFlag
    }


    private void receivingNtfCheck()
    {
        //This function checks if a node is currently receiving a ntf pakcet. 

        receivingNtfCheckFlag = 0
        int sendTimeStart     = getCurrentTime()
        int sendTimeEnd       = sendTimeStart + controlMsgDuration

        for(int j = 0; j<schedule[nodePosition].size(); j++)
        {
            if( sendTimeStart > schedule[nodePosition][j].busyTimeEnd )   
            {
                continue
            }   
            else if( sendTimeEnd  < schedule[nodePosition][j].busyTimeStart )   
            {
                continue
            }
            else if( schedule[nodePosition][j].status == OVERHEARING )
            {
                receivingNtfCheckFlag = OVERHEARING
                break       
            } 
            else
            {
                continue
            } 
        }   
    }

    private int transmissionClashCheck(int typeOfPacket)
    {
        //This function checks whether a node's transmission clashes with an already scheduled tranmission.
        
        transmissionClashCheckFlag = 0

        def sendTimeStart = getCurrentTime()
        def sendTimeEnd = sendTimeStart + controlMsgDuration
        
        if(typeOfPacket == DATA_PACKET)
        {
            sendTimeEnd = sendTimeEnd + dataMsgDuration - controlMsgDuration
            for(int i = 0; i<transmissionSlotsList.size(); i++ )
            {
                if(sendTimeStart > transmissionSlotsList[i].startTimeData && sendTimeStart < transmissionSlotsList[i].endTimeData)
                {
                    transmissionClashCheckFlag = DATA_DATA_TX_CLASH
                    break
                }

            }
        }
        else
        {
            for(int i = 0;i<transmissionSlotsList.size();i++ )
            {
                if(sendTimeStart > transmissionSlotsList[i].startTimeNtf && sendTimeStart < transmissionSlotsList[i].endTimeNtf)
                {
                    transmissionClashCheckFlag = NTF_NTF_TX_CLASH
                    break   
                }
                else if((sendTimeStart > transmissionSlotsList[i].startTimeData && sendTimeStart < transmissionSlotsList[i].endTimeData) || (sendTimeEnd > transmissionSlotsList[i].startTimeData && sendTimeEnd < transmissionSlotsList[i].endTimeData))
                {
                    transmissionClashCheckFlag = NTF_DATA_TX_CLASH
                    break
                }
                else
                {
                    continue    
                }
            }
        }       
        return transmissionClashCheckFlag
    }  

    private void clearTransmissionSlot(int key)
    {
        //Clear entries pertaining to a particular packet request 
        for(int i = 0;i<transmissionSlotsList.size();i++)
        {
            if(transmissionSlotsList[i].key == key)
            {
                transmissionSlotsList.remove(i)
                break
            }
        }
    }

    private int deferTransmissionsCollisionCheck(TransmissionSlot tSlot)
    {

        //Defer transmission times of a particular transmisson by making the node backoff the packet.
        //This is done when either transmissionClashCheck,collisionCheck or receiveNtfCheck instruct the node to refrain from sending the packet. 

        def bo = AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS

        tSlot.startTimeNtf  = tSlot.startTimeNtf  + bo*dataMsgDuration 
        tSlot.endTimeNtf    = tSlot.endTimeNtf    + bo*dataMsgDuration 
        tSlot.startTimeData = tSlot.startTimeData + bo*dataMsgDuration 
        tSlot.endTimeData   = tSlot.endTimeData   + bo*dataMsgDuration 

        return bo
    }

    private void deferTransmissionsScheduleCheck(TransmissionSlot tSlot)
    {

        //Defer transmission times of a particular transmissons by making the node backoff the packet.
        //This is done when a received NTF packet implies a conflict whose resolution tantamounts to the node backing off the packet.

        def bo = AgentLocalRandom.current().nextInt(MAX_BACKOFF_SLOTS)+MIN_BACKOFF_SLOTS    

        tSlot.startTimeNtf  = tSlot.startTimeNtf  + lagTime + bo*dataMsgDuration// + dataMsgDuration
        tSlot.endTimeNtf    = tSlot.startTimeNtf  + controlMsgDuration 
        tSlot.startTimeData = tSlot.startTimeNtf  + lagTime 
        tSlot.endTimeData   = tSlot.startTimeData + dataMsgDuration         
    }

    private int scheduleCheck(long receiveTime,int sourcePosition,int destinationPosition)
    {
        //This function checks whether the data transmission corresponding to a received NTF packet will result in a conflict with the node's own scheduled tranmission.
        long startTimeAtReceiver = receiveTime - propagationDelay[nodePosition*nodeCount+sourcePosition] + propagationDelay[sourcePosition*nodeCount+destinationPosition] + lagTime
        long endTimeAtReceiver  = startTimeAtReceiver + dataMsgDuration

        scheduleCheckFlag = STORE_SCHEDULE

        if(nodeList[destinationPosition] == myAddr) 
        {
            for (int i = 0; i < transmissionSlotsList.size(); i++)
            {
                if(( startTimeAtReceiver > transmissionSlotsList[i].startTimeData) && ( startTimeAtReceiver < transmissionSlotsList[i].endTimeData))  
                {
                    if(transmissionSlotsList[i].startTimeNtf < receiveTime - propagationDelay[nodePosition*nodeCount+sourcePosition])
                    {
                        scheduleCheckFlag = DISCARD_SCHEDULE
                        break
                    }
                    else
                    {
                        scheduleCheckFlag = DEFER_TRANSMISSIONS
                        deferTransmissionsScheduleCheck(transmissionSlotsList[i])
                    }
                    
                }
                else if(( endTimeAtReceiver > transmissionSlotsList[i].startTimeData ) && ( endTimeAtReceiver < transmissionSlotsList[i].endTimeData))
                {
                    if(transmissionSlotsList[i].startTimeNtf < receiveTime - propagationDelay[nodePosition*nodeCount+sourcePosition])
                    {
                        scheduleCheckFlag = DISCARD_SCHEDULE
                        break
                    }
                    else
                    {
                        scheduleCheckFlag = DEFER_TRANSMISSIONS
                        deferTransmissionsScheduleCheck(transmissionSlotsList[i])

                    }
                }
                else if(( startTimeAtReceiver > transmissionSlotsList[i].startTimeNtf) && ( startTimeAtReceiver < transmissionSlotsList[i].endTimeNtf))  
                {
                    scheduleCheckFlag = DEFER_TRANSMISSIONS
                    deferTransmissionsScheduleCheck(transmissionSlotsList[i])
                }
                
                else if(( endTimeAtReceiver > transmissionSlotsList[i].startTimeNtf ) && ( endTimeAtReceiver < transmissionSlotsList[i].endTimeNtf))
                {
                    scheduleCheckFlag = DEFER_TRANSMISSIONS
                    deferTransmissionsScheduleCheck(transmissionSlotsList[i])   
                }
                else
                {
                    scheduleCheckFlag = STORE_SCHEDULE
                }   
            }
        }
        else
        {
            for (int i = 0; i < transmissionSlotsList.size(); i++)
            {
                if(transmissionSlotsList[i].destination == nodeList[destinationPosition])
                {
                    if(( startTimeAtReceiver > transmissionSlotsList[i].startTimeData + propagationDelay[nodePosition*nodeCount+destinationPosition] ) && ( startTimeAtReceiver < transmissionSlotsList[i].endTimeData + propagationDelay[nodePosition*nodeCount+destinationPosition] ))  
                    {
                        if(transmissionSlotsList[i].startTimeNtf < receiveTime - propagationDelay[nodePosition*nodeCount+sourcePosition])
                        {
                            scheduleCheckFlag = DISCARD_SCHEDULE
                            break
                        }
                        else
                        {
                            scheduleCheckFlag = DEFER_TRANSMISSIONS     
                            deferTransmissionsScheduleCheck(transmissionSlotsList[i])
                        }
                    }
                    else if(( endTimeAtReceiver > transmissionSlotsList[i].startTimeData + propagationDelay[nodePosition*nodeCount+destinationPosition] ) && ( endTimeAtReceiver < transmissionSlotsList[i].endTimeData + propagationDelay[nodePosition*nodeCount+destinationPosition] ))
                    {
                        if(transmissionSlotsList[i].startTimeNtf < receiveTime - propagationDelay[nodePosition*nodeCount+sourcePosition])
                        {
                            scheduleCheckFlag = DISCARD_SCHEDULE
                            break
                        }
                        else
                        {
                            scheduleCheckFlag = DEFER_TRANSMISSIONS
                            deferTransmissionsScheduleCheck(transmissionSlotsList[i])
                        }
                    }
                    else
                    {
                        scheduleCheckFlag = STORE_SCHEDULE
                    }   
                }
            }                       
        }
        return scheduleCheckFlag
    }

    private void clearSchedules(int clearTime, int key)
    {
    
    //Clear the schedule entries corresponding to a received NTF packet.
        add new WakerBehavior(clearTime, {
            for(int i = 0;i<schedule[0].size();i++)
            {
                if(schedule[0][i].key == key)
                {
                    for(int j = 0; j < nodeCount; j++)
                    {
                        schedule[j].remove(i)
                    }
                    break
                }
            }            
        })   

    }

    private int setSchedules(int sourcePosition, int destinationPosition, long receiveTime)
    {
//This function will set schedules(busy periods at all nodestination) due to the Data transmission corresponding to a received NTF packet.       
        for(int i = 0; i < nodeCount; i++)
        {
            long startTime = receiveTime - propagationDelay[nodePosition*nodeCount+sourcePosition] + lagTime + propagationDelay[sourcePosition*nodeCount+i]
            long endTime   = startTime + dataMsgDuration
            
            ScheduleSlot sSlot = new ScheduleSlot()
            sSlot.busyTimeStart = startTime
            sSlot.busyTimeEnd = endTime
            sSlot.key = scheduleSlotKey
            
            if(i == sourcePosition)
            {
                sSlot.status = SENDING
            }
            else if(i == destinationPosition)
            {
                sSlot.status = RECEIVING
            }
            else
            {
                sSlot.status = OVERHEARING              
            }
            schedule[i].add(sSlot)
        }

        return scheduleSlotKey  
    }


    private long getCurrentTime()
    {       
        ParameterReq req = new ParameterReq(agentForService(Services.PHYSICAL))
        req.get(PhysicalParam.time)
        ParameterRsp rsp = (ParameterRsp)request(req, 1000)         
        long time = rsp.get(PhysicalParam.time) 
        time = time / 1000
        return time 
    }

    public Message processRequest(Message msg) 
    {
        switch (msg) {
          case ReservationReq:
            if (msg.duration <= 0) 
            {
                return new Message(msg, Performative.REFUSE)   // check requested duration
            }
            else
            {
                sendNtf(msg)                
            }
          case ReservationCancelReq:
          case ReservationAcceptReq:                                  // respond to other requests defined
          case TxAckReq:                                              //  by the MAC service trivially with
            return new Message(msg, Performative.REFUSE)              //  a REFUSE performative
        }
        return null     
    }

    public void processMessage(Message msg) 
    {
        def phy = agentForService Services.PHYSICAL
        def node = agentForService NODE_INFO

        if(msg instanceof TxFrameNtf)
        { 
            if(msg.getRecipient() == Address.BROADCAST)
            {
                add new WakerBehavior(controlMsgDuration, {
                    //Turn on Receiver as NTF_Transmission is over. 
                    rxEnable()  
                })                  
            }
            else
            {
                add new WakerBehavior(dataMsgDuration, {
                    //Turn on Receiver as DATA_Transmission is over.                        
                    rxEnable()      
                })                  

            }
        }

        if(msg instanceof RxFrameNtf)
        {       

            if( msg.getTo() == Address.BROADCAST )
            {
                //Received a NTF packet.
                int sourcePosition     = nodeList.indexOf(msg.getFrom()) 

                def pkt = ntfMsg.decode(msg.data)   
                int destinationPosition = nodeList.indexOf(pkt.destinationNodeAddress) 
                
                long receiveTime = msg.rxTime
                receiveTime = receiveTime / 1000                        
                int clearTime = lagTime - propagationDelay[nodePosition*nodeCount+sourcePosition] + maxDelay[sourcePosition] + dataMsgDuration - controlMsgDuration 
                //Clearing time for a particular entry in schedule is the time when all nodes will have finished receiving the DATA packet corresponding to the received NTF packet.            
                scheduleCheck(receiveTime,sourcePosition,destinationPosition)
                if(scheduleCheckFlag == STORE_SCHEDULE || scheduleCheckFlag == DEFER_TRANSMISSIONS)
                {
                    //Since scheduleCheck implies no conflict, store busy periods.Clear them after clearTime defined above.
                    int key = setSchedules(sourcePosition, destinationPosition,receiveTime)
                    clearSchedules(clearTime,key)
                    scheduleSlotKey = scheduleSlotKey + 1   
                }
                
            }

            if(msg.getTo() == node.Address && msg.protocol == Protocol.USER)
            {
                //Received a DATA packet
                def source          = msg.getFrom()  
                def destination     = msg.getTo()
                def receiveTime     = msg.rxTime                            
            }
        }
    }


//Parameters to be passed to Agent File 

    List nodeList = new ArrayList<Integer>()
    List propagationDelay = new ArrayList<Integer>()
    int dataMsgDuration, controlMsgDuration

    List<Parameter> getParameterList() {
        allOf(AlohaANParam)
    }

}


