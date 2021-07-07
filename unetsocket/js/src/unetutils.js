import {AgentID, MessageClass, Services} from 'fjage';

const DatagramReq = MessageClass('org.arl.unet.DatagramReq');
const DatagramNtf = MessageClass('org.arl.unet.DatagramNtf');
const BasebandSignal = MessageClass('org.arl.unet.bb.BasebandSignal');

let UnetServices = {
  'NODE_INFO': 'org.arl.unet.Services.NODE_INFO',
  'ADDRESS_RESOLUTION': 'org.arl.unet.Services.ADDRESS_RESOLUTION',
  'DATAGRAM': 'org.arl.unet.Services.DATAGRAM',
  'PHYSICAL': 'org.arl.unet.Services.PHYSICAL',
  'RANGING': 'org.arl.unet.Services.RANGING',
  'BASEBAND': 'org.arl.unet.Services.BASEBAND',
  'LINK': 'org.arl.unet.Services.LINK',
  'MAC': 'org.arl.unet.Services.MAC',
  'ROUTING': 'org.arl.unet.Services.ROUTING',
  'ROUTE_MAINTENANCE': 'org.arl.unet.Services.ROUTE_MAINTENANCE',
  'TRANSPORT': 'org.arl.unet.Services.TRANSPORT',
  'REMOTE': 'org.arl.unet.Services.REMOTE',
  'STATE_MANAGER': 'org.arl.unet.Services.STATE_MANAGER',
};

Object.assign(Services, UnetServices);

/**
 * Well-known protocol number assignments used in UnetStack
 * @typedef {Object.<string, number>} Protocol
 */
let Protocol = {
  'DATA' : 0,               // Protocol number for user application data.
  'RANGING' : 1,            // Protocol number for use by ranging agents.
  'LINK' : 2,               // Protocol number for use by link agents.
  'REMOTE' : 3,             // Protocol number for use by remote management agents.
  'MAC' : 4,                // Protocol number for use by MAC protocol agents.
  'ROUTING' : 5,            // Protocol number for use by routing agents.
  'TRANSPORT' : 6,          // Protocol number for use by transport agents.
  'ROUTE_MAINTENANCE' : 7,   // Protocol number for use by route maintenance agents.
  'LINK2' : 8,              // Protocol number for use by secondary link agents.
  'USER' : 32,              // Lowest protocol number allowable for user protocols.
  'MAX' : 63,               // Largest protocol number allowable.
};

/**
 * Well-known protocol Messages used in UnetStack
 * @typedef {Object.<string, MessageClass>} UnetMessages
 */
let UnetMessages = {
  // unet
  'TestReportNtf'          : MessageClass('org.arl.unet.TestReportNtf'), 
  'AbnormalTerminationNtf' : MessageClass('org.arl.unet.AbnormalTerminationNtf'), 
  'CapabilityListRsp'      : MessageClass('org.arl.unet.CapabilityListRsp'), 
  'CapabilityReq'          : MessageClass('org.arl.unet.CapabilityReq'), 
  'ClearReq'               : MessageClass('org.arl.unet.ClearReq'), 
  'DatagramCancelReq'      : MessageClass('org.arl.unet.DatagramCancelReq'), 
  'DatagramDeliveryNtf'    : MessageClass('org.arl.unet.DatagramDeliveryNtf'), 
  'DatagramFailureNtf'     : MessageClass('org.arl.unet.DatagramFailureNtf'), 
  'DatagramNtf'            : MessageClass('org.arl.unet.DatagramNtf'), 
  'DatagramProgressNtf'    : MessageClass('org.arl.unet.DatagramProgressNtf'), 
  'DatagramReq'            : MessageClass('org.arl.unet.DatagramReq'), 
  'ParamChangeNtf'         : MessageClass('org.arl.unet.ParamChangeNtf'), 
  'RefuseRsp'              : MessageClass('org.arl.unet.RefuseRsp'), 
  'FailureNtf'             : MessageClass('org.arl.unet.FailureNtf'), 

  // net
  'DatagramTraceReq'       : MessageClass('org.arl.unet.net.DatagramTraceReq'), 
  'RouteDiscoveryReq'      : MessageClass('org.arl.unet.net.RouteDiscoveryReq'), 
  'RouteTraceReq'          : MessageClass('org.arl.unet.net.RouteTraceReq'), 
  'RouteDiscoveryNtf'      : MessageClass('org.arl.unet.net.RouteDiscoveryNtf'), 
  'RouteTraceNtf'          : MessageClass('org.arl.unet.net.RouteTraceNtf'), 

  // phy
  'FecDecodeReq'           : MessageClass('org.arl.unet.phy.FecDecodeReq'), 
  'RxJanusFrameNtf'        : MessageClass('org.arl.unet.phy.RxJanusFrameNtf'), 
  'TxJanusFrameReq'        : MessageClass('org.arl.unet.phy.TxJanusFrameReq'), 
  'BadFrameNtf'            : MessageClass('org.arl.unet.phy.BadFrameNtf'), 
  'BadRangeNtf'            : MessageClass('org.arl.unet.phy.BadRangeNtf'),
  'ClearSyncReq'           : MessageClass('org.arl.unet.phy.ClearSyncReq'), 
  'CollisionNtf'           : MessageClass('org.arl.unet.phy.CollisionNtf'), 
  'RxFrameNtf'             : MessageClass('org.arl.unet.phy.RxFrameNtf', DatagramNtf), 
  'RxFrameStartNtf'        : MessageClass('org.arl.unet.phy.RxFrameStartNtf'), 
  'SyncInfoReq'            : MessageClass('org.arl.unet.phy.SyncInfoReq'), 
  'SyncInfoRsp'            : MessageClass('org.arl.unet.phy.SyncInfoRsp'), 
  'TxFrameNtf'             : MessageClass('org.arl.unet.phy.TxFrameNtf'), 
  'TxFrameReq'             : MessageClass('org.arl.unet.phy.TxFrameReq', DatagramReq), 
  'TxFrameStartNtf'        : MessageClass('org.arl.unet.phy.TxFrameStartNtf'), 
  'TxRawFrameReq'          : MessageClass('org.arl.unet.phy.TxRawFrameReq'), 

  // addr
  'AddressAllocReq'        : MessageClass('org.arl.unet.addr.AddressAllocReq'), 
  'AddressAllocRsp'        : MessageClass('org.arl.unet.addr.AddressAllocRsp'), 
  'AddressResolutionReq'   : MessageClass('org.arl.unet.addr.AddressResolutionReq'), 
  'AddressResolutionRsp'   : MessageClass('org.arl.unet.addr.AddressResolutionRsp'), 

  // bb
  'BasebandSignal'         : MessageClass('org.arl.unet.bb.BasebandSignal'), 
  'RecordBasebandSignalReq' : MessageClass('org.arl.unet.bb.RecordBasebandSignalReq'), 
  'RxBasebandSignalNtf'    : MessageClass('org.arl.unet.bb.RxBasebandSignalNtf', BasebandSignal), 
  'TxBasebandSignalReq'    : MessageClass('org.arl.unet.bb.TxBasebandSignalReq', BasebandSignal), 

  // link
  'LinkStatusNtf'          : MessageClass('org.arl.unet.link.LinkStatusNtf'), 

  // localization
  'RangeNtf'               : MessageClass('org.arl.unet.localization.RangeNtf'), 
  'RangeReq'               : MessageClass('org.arl.unet.localization.RangeReq'), 
  'BeaconReq'              : MessageClass('org.arl.unet.localization.BeaconReq'), 
  'RespondReq'             : MessageClass('org.arl.unet.localization.RespondReq'), 
  'InterrogationNtf'       : MessageClass('org.arl.unet.localization.InterrogationNtf'), 


  // mac
  'ReservationAcceptReq'   : MessageClass('org.arl.unet.mac.ReservationAcceptReq'), 
  'ReservationCancelReq'   : MessageClass('org.arl.unet.mac.ReservationCancelReq'), 
  'ReservationReq'         : MessageClass('org.arl.unet.mac.ReservationReq'), 
  'ReservationRsp'         : MessageClass('org.arl.unet.mac.ReservationRsp'), 
  'ReservationStatusNtf'   : MessageClass('org.arl.unet.mac.ReservationStatusNtf'), 
  'RxAckNtf'               : MessageClass('org.arl.unet.mac.RxAckNtf'), 
  'TxAckReq'               : MessageClass('org.arl.unet.mac.TxAckReq'), 


  // remote
  'RemoteExecReq'          : MessageClass('org.arl.unet.remote.RemoteExecReq'), 
  'RemoteFailureNtf'       : MessageClass('org.arl.unet.remote.RemoteFailureNtf'), 
  'RemoteFileGetReq'       : MessageClass('org.arl.unet.remote.RemoteFileGetReq'), 
  'RemoteFileNtf'          : MessageClass('org.arl.unet.remote.RemoteFileNtf'), 
  'RemoteFilePutReq'       : MessageClass('org.arl.unet.remote.RemoteFilePutReq'), 
  'RemoteSuccessNtf'       : MessageClass('org.arl.unet.remote.RemoteSuccessNtf'), 
  'RemoteTextNtf'          : MessageClass('org.arl.unet.remote.RemoteTextNtf'), 
  'RemoteTextReq'          : MessageClass('org.arl.unet.remote.RemoteTextReq'), 

  // scheduler
  'AddScheduledSleepReq'   : MessageClass('org.arl.unet.scheduler.AddScheduledSleepReq'), 
  'GetSleepScheduleReq'    : MessageClass('org.arl.unet.scheduler.GetSleepScheduleReq'), 
  'RemoveScheduledSleepReq' : MessageClass('org.arl.unet.scheduler.RemoveScheduledSleepReq'), 
  'SleepScheduleRsp'       : MessageClass('org.arl.unet.scheduler.SleepScheduleRsp'), 
  'WakeFromSleepNtf'       : MessageClass('org.arl.unet.scheduler.WakeFromSleepNtf'), 

  // state
  'ClearStateReq'          : MessageClass('org.arl.unet.state.ClearStateReq'), 
  'SaveStateReq'           : MessageClass('org.arl.unet.state.SaveStateReq')
};

/**
 * A message which requests the transmission of the datagram from the Unet
 * 
 * @typedef {Message} DatagramReq
 * @property {number[]} data - data as an Array of bytes
 * @property {number} from - from/source node address
 * @property {number} to - to/destination node address
 * @property {number} protocol - protocol number to be used to send this Datagram
 * @property {boolean} reliability - true if Datagram should be reliable, false if unreliable
 * @property {number} ttl - time-to-live for the datagram. Time-to-live is advisory, and an agent may choose it ignore it
 */

/**
 * Notification of received datagram message received by the Unet node.
 * 
 * @typedef {Message} DatagramNtf
 * @property {number[]} data - data as an Array of bytes
 * @property {number} from - from/source node address
 * @property {number} to - to/destination node address
 * @property {number} protocol - protocol number to be used to send this Datagram
 * @property {number} ttl - time-to-live for the datagram. Time-to-live is advisory, and an agent may choose it ignore it
 */

/**
 * An identifier for an agent or a topic.
 * @external AgentID
 * @see {@link https://org-arl.github.io/fjage/jsdoc/|fjåge.js Documentation}
 */

/**
 * Services supported by fjage agents.
 * @external Services
 * @see {@link https://org-arl.github.io/fjage/jsdoc/|fjåge.js Documentation}
 */

/**
 *  An action represented by a message.
 * @external Performative
 * @see {@link https://org-arl.github.io/fjage/jsdoc/|fjåge.js Documentation}
 */

/**
 * Function to creates a unqualified message class based on a fully qualified name.
 * @external MessageClass
 * @see {@link https://org-arl.github.io/fjage/jsdoc/|fjåge.js Documentation}
 */

/**
 * @external Gateway
 * @see {@link https://org-arl.github.io/fjage/jsdoc/|fjåge.js Documentation}
 */

export {AgentID, Services, UnetMessages, Protocol};
