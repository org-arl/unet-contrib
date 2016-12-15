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
enum AlohaANParam implements Parameter {

/**
 * The nodeList contains the addresses of nodes in the network within one hop of the current node as well as the address of the current node.
 *<p>
 *Let us say there are 4 nodes with addresses 12,11,13,14.
 *So nodeList = [12,11,13,14] 
 *<p>
 */
    nodeList,

/**
 * The dataMsgDuration is the transmit time (in milliseconds) for a DATA Packet. 
 */         
    dataMsgDuration,

/**
 * The controlMsgDuration is the transmit time (in milliseconds) for a CONTROL Packet. 
 */         
    controlMsgDuration,

/**
 * The propagationDelay is propagation delay in the network for all pair of nodes within one hop of the node.
 *<p>
 *Let us say there are 4 nodes with addresses 12,11,13,14.
 *So propagationDelay = [PropagationDelay(12->12),PropagationDelay(12->11),PropagationDelay(12->13),PropagationDelay(12->14),
 *                       PropagationDelay(11->12),PropagationDelay(11->11),PropagationDelay(11->13),PropagationDelay(11->14), 
 *                       PropagationDelay(13->12),PropagationDelay(13->11),PropagationDelay(13->13),PropagationDelay(13->14),
 *                       PropagationDelay(14->12),PropagationDelay(14->11),PropagationDelay(14->13),PropagationDelay(14->14)]
 *
 *It is an ArrayList each element containing propagation delays between two nodes.
 *<p>
 */
    propagationDelay
}


