import {AgentID, MessageClass, Performative, Services} from '../node_modules/fjage/dist/esm/fjage.js';

const ParameterReq = MessageClass('org.arl.unet.ParameterReq');
const DatagramReq = MessageClass('org.arl.unet.DatagramReq');
const DatagramNtf = MessageClass('org.arl.unet.DatagramNtf');
const BasebandSignal = MessageClass('org.arl.unet.bb.BasebandSignal');

let AgentIDMixin = {
  toString() {
    return this.name + (this.owner ? ' on ' + this.owner.sock.url : '');
  }
};

let ParamGetterSetterMixin = {
  set (params, values, index=-1, timeout=5000) {
    if (!params) return null;
    let msg = new ParameterReq();
    msg.recipient = this.name;
    if (Array.isArray(params)){
      msg.requests = params.map((p, i) => {
        return {
          'param': p,
          'value': values[i]
        };
      });
    } else {
      msg.param = params;
      msg.value = values;
    }
    msg.index = Number.isInteger(index) ? index : -1;
    return this.owner.request(msg, timeout).then(rsp => {
      return new Promise(resolve => {
        var ret = Array.isArray(params) ? new Array(params.length).fill(null) : null;
        if (!rsp || rsp.perf != Performative.INFORM || !rsp.param){
          console.warn(`Parameter(s) ${params} could not be set`);
          resolve(ret);
          return;
        }

        if (Array.isArray(params)){
          if (!rsp.values) rsp.values = {};
          if (rsp.param) rsp.values[rsp.param] = rsp.value;
          const rvals = Object.keys(rsp.values);
          ret = params.map((p, i) => {
            let f = rvals.find(rv => rv.endsWith(p));
            if (f){
              if (rsp.values[f] != values[i]){
                console.warn(`WARNING: Parameter ${p} set to ${rsp.values[f]}`);
              }
              return rsp.values[f];
            }else null;
          });
        }else{
          if (rsp.value != values){
            console.warn(`WARNING: Parameter ${params} set to ${rsp.value}`);
          }
          ret = rsp.value;
        }
        resolve(ret);
      });
    });
  },

  get(params, index=-1, timeout=5000) {
    let msg = new ParameterReq();
    msg.recipient = this.name;
    if (params){
      if (Array.isArray(params)){
        msg.requests = params.map(p => {return {'param': p};});
      }else{
        msg.param = params;
      }
    }
    msg.index = Number.isInteger(index) ? index : -1;
    return this.owner.request(msg, timeout).then(rsp => {
      return new Promise(resolve => {
        var ret = Array.isArray(params) ? new Array(params.length).fill(null) : null;
        if (!rsp || rsp.perf != Performative.INFORM || (params && (!rsp.param))){
          console.warn(`Parameter(s) ${params} could not be fetched`);
          resolve(ret);
          return;
        }
        // Request for listing of all parameters.
        if (!params){
          if (!rsp.values) rsp.values = {};
          if (rsp.param) rsp.values[rsp.param] = rsp.value;
          ret = rsp.values;
        } else if (Array.isArray(params)) {
          if (!rsp.values) rsp.values = {};
          if (rsp.param) rsp.values[rsp.param] = rsp.value;
          const rvals = Object.keys(rsp.values);
          ret = params.map(p => {
            let f = rvals.find(rv => rv.endsWith(p));
            return f ? rsp.values[f] : null ;
          });
        } else{
          ret = rsp.value;
        }

        resolve(ret);
      });
    });
  }
};

if (!AgentID.prototype.set){
  Object.assign(AgentID.prototype, ParamGetterSetterMixin);
}

Object.assign(AgentID.prototype, AgentIDMixin);

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

export {AgentID, Services, UnetMessages, Protocol};
