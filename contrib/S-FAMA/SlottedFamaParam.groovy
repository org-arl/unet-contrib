/******************************************************************************
Copyright (c) 2016, Pritish Nahar
This file is released under Simplified BSD License.
Go to http://www.opensource.org/licenses/BSD-3-Clause for full license details.
******************************************************************************/
import org.arl.unet.Parameter

/**
 * The class represents the parameters to be passed to the MAC agent.
 */
@com.google.gson.annotations.JsonAdapter(org.arl.unet.JsonTypeAdapter.class)
enum SlottedFamaParam implements Parameter {

/**
 * The controlMsgDuration is the transmit time (in milliseconds) for a CONTROL Packet. 
 */     
    controlMsgDuration, 

/**
 * The dataMsgDuration is the transmit time (in milliseconds) for a DATA Packet.
 */
    dataMsgDuration,

/**
 * The maxPropagationDelay is the maximum propagation delay in the network for a pair of nodes.
 */
    maxPropagationDelay,

/**
 * timerCtsTimeoutOpMode defines the mode in which the timer set due to CTS_Timeout will operate.
 *<p> 
 *The parameter is can have either of the two values, 1 and 2.
 * 1 means that the CTS_TIMEOUT timer will not restart if the node leaves the state BACKOFF_CTS_TIMEOUT due to a carrier sense.
 * 2 means that the CTS_TIMEOUT timer will restart whenever a node returns to the state, BACKOFF_CTS_TIMEOUT, after having left it due to a carrier sense.
 *<p>
 */
    timerCtsTimeoutOpMode
}

