import {AgentID, MessageClass, Services, Gateway} from 'fjage';

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
  'DEVICE_INFO': 'org.arl.unet.Services.DEVICE_INFO',
  'DOA': 'org.arl.unet.Services.DOA',
  'SCHEDULER':'org.arl.unet.Services.SCHEDULER'
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
  * Convert coordinates from a local coordinates to GPS coordinate
  * @param {Array} origin - Local coordinate system's origin as `[latitude, longitude]`
  * @param {Number} x - X coordinate of the local coordinate to be converted
  * @param {Number} y - Y coordinate of the local coordinate to be converted
  * @returns {Array} - GPS coordinates (in decimal degrees) as `[latitude, longitude]`
  */

export function toGps(origin, x, y) {
  let coords = [] ;
  let [xScale,yScale] = _initConv(origin[0]);
  coords[1] = x/xScale + origin[1];
  coords[0] = y/yScale + origin[0];
  return coords;
}

/**
  * Convert coordinates from a GPS coordinates to local coordinate
  * @param {Array} origin - Local coordinate system's origin as `[latitude, longitude]`
  * @param {Number} lat - Latitude of the GPS coordinate to be converted
  * @param {Number} lon - Longitude of the GPS coordinate to be converted
  * @returns {Array} - GPS coordinates (in decimal degrees) as `[latitude, longitude]`
  */
export function toLocal(origin, lat, lon) {
  let pos = [];
  let [xScale,yScale] = _initConv(origin[0]);
  pos[0] = (lon-origin[1]) * xScale;
  pos[1] = (lat-origin[0]) * yScale;
  return pos;
}

function _initConv(lat){
  let rlat = lat * Math.PI/180;
  let yScale = 111132.92 - 559.82*Math.cos(2*rlat) + 1.175*Math.cos(4*rlat) - 0.0023*Math.cos(6*rlat);
  let xScale = 111412.84*Math.cos(rlat) - 93.5*Math.cos(3*rlat) + 0.118*Math.cos(5*rlat);
  return [xScale, yScale];
}

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
 * A caching CachingAgentID which caches Agent parameters locally.
 *
 * @class
 * @extends AgentID
 * @param {string | AgentID} name - name of the agent or an AgentID to copy
 * @param {boolean} topic - name of topic
 * @param {Gateway} owner - Gateway owner for this AgentID
 * @param {Boolean} [greedy=true] - greedily fetches and caches all parameters if this Agent
 *
*/
class CachingAgentID extends AgentID {

  constructor(name, topic, owner, greedy=true) {
    if (name instanceof AgentID) {
      super(name.getName(), name.topic, name.owner);
    } else {
      super(name, topic, owner);
    }
    this.greedy = greedy;
    this.cache = {};
    this.specialParams = ['name', 'version'];
  }

  /**
   * Sets parameter(s) on the Agent referred to by this AgentID, and caches the parameter(s).
   *
   * @param {(string|string[])} params - parameters name(s) to be set
   * @param {(Object|Object[])} values - parameters value(s) to be set
   * @param {number} [index=-1] - index of parameter(s) to be set
   * @param {number} [timeout=5000] - timeout for the response
   * @returns {Promise<(Object|Object[])>} - a promise which returns the new value(s) of the parameters
   */
  async set(params, values, index=-1, timeout=5000) {
    let s = await super.set(params, values, index, timeout);
    this._updateCache(params, s, index);
    return s;
  }

  /**
   * Gets parameter(s) on the Agent referred to by this AgentID, getting them from the cache if possible.
   *
   * @param {(string|string[])} params - parameters name(s) to be fetched
   * @param {number} [index=-1] - index of parameter(s) to be fetched
   * @param {number} [timeout=5000] - timeout for the response
   * @param {number} [maxage=5000] - maximum age of the cached result to retreive
   * @returns {Promise<(Object|Object[])>} - a promise which returns the value(s) of the parameters
   */
  async get(params, index=-1, timeout=5000, maxage=5000) {
    if (this._isCached(params, index, maxage)) return this._getCache(params, index);
    if (this.greedy &&
      !(Array.isArray(params) && [...new Set([...params, ...this.specialParams])].length!=0) &&
      !this.specialParams.includes(params)) {
      let rsp = await super.get(null, index, timeout);
      this._updateCache(null, rsp, index);
      if (!rsp) return Array.isArray(params) ? new Array(params.length).fill(null) : null;
      if (!params) return rsp;
      else if (Array.isArray(params)) {
        return params.map(p => {
          let f = Object.keys(rsp).find(rv => this._toNamed(rv) === p);
          return f ? rsp[f] : null;
        });
      } else {
        let f = Object.keys(rsp).find(rv => this._toNamed(rv) === params);
        return f ? rsp[f] : null;
      }
    } else{
      let r = await super.get(params, index, timeout);
      this._updateCache(params, r, index);
      return r;
    }
  }

  _updateCache(params, vals, index) {
    if (vals == null || Array.isArray(vals) && vals.every(v => v == null)) return;
    if (params == null) {
      params = Object.keys(vals);
      vals = Object.values(vals);
    } else if (!Array.isArray(params)) params = [params];
    if (!Array.isArray(vals)) vals = [vals];
    params = params.map(this._toNamed);
    if (this.cache[index.toString()] === undefined) this.cache[index.toString()] = {};
    let c = this.cache[index.toString()];
    for (let i = 0; i < params.length; i++) {
      if (c[params[i]] === undefined) c[params[i]] = {};
      c[params[i]].value = vals[i];
      c[params[i]].ctime = Date.now();
    }
  }

  _isCached(params, index, maxage) {
    if (maxage <= 0) return false;
    if (params == null) return false;
    let c = this.cache[index.toString()];
    if (!c) {
      return false;
    }
    if (!Array.isArray(params)) params = [params];
    const rv = params.every(p => {
      p = this._toNamed(p);
      return (p in c) && (Date.now() - c[p].ctime <= maxage);
    });
    return rv;
  }

  _getCache(params, index) {
    let c = this.cache[index.toString()];
    if (!c) return null;
    if (!Array.isArray(params)){
      if (params in c) return c[params].value;
      return null;
    }else {
      return params.map(p => p in c ? c[p].value : null);
    }
  }

  _toNamed(param) {
    const idx = param.lastIndexOf('.');
    if (idx < 0) return param;
    else return param.slice(idx+1);
  }

}


class CachingGateway extends Gateway{

  /**
   * Get an AgentID for a given agent name.
   *
   * @param {string} name - name of agent
   * @param {Boolean} [caching=true] - if the AgentID should cache parameters
   * @param {Boolean} [greedy=true] - greedily fetches and caches all parameters if this Agent
   * @returns {AgentID|CachingAgentID} - AgentID for the given name
   */
  agent(name, caching=true, greedy=true) {
    const aid = super.agent(name);
    return caching ? new CachingAgentID(aid, null, null, greedy) : aid;
  }

  /**
   * Returns an object representing the named topic.
   *
   * @param {string|AgentID} topic - name of the topic or AgentID
   * @param {string} topic2 - name of the topic if the topic param is an AgentID
   * @param {Boolean} [caching=true] - if the AgentID should cache parameters
   * @param {Boolean} [greedy=true] - greedily fetches and caches all parameters if this Agent
   * @returns {AgentID|CachingAgentID} - object representing the topic
   */
  topic(topic, topic2, caching=true, greedy=true) {
    const aid = super.topic(topic, topic2);
    return caching ? new CachingAgentID(aid, null, null, greedy) : aid;
  }

  /**
   * Finds an agent that provides a named service. If multiple agents are registered
   * to provide a given service, any of the agents' id may be returned.
   *
   * @param {string} service - the named service of interest
   * @param {Boolean} [caching=true] - if the AgentID should cache parameters
   * @param {Boolean} [greedy=true] - greedily fetches and caches all parameters if this Agent
   * @returns {Promise<?AgentID|CachingAgentID>} - a promise which returns an agent id for an agent that provides the service when resolved
   */
  async agentForService(service, caching=true, greedy=true) {
    const aid = await super.agentForService(service);
    if (!aid) return aid;
    return caching ? new CachingAgentID(aid, null, null, greedy) : aid;
  }

  /**
   * Finds all agents that provides a named service.
   *
   * @param {string} service - the named service of interest
   * @param {Boolean} [caching=true] - if the AgentID should cache parameters
   * @param {Boolean} [greedy=true] - greedily fetches and caches all parameters if this Agent
   * @returns {Promise<?AgentID|CachingAgentID[]>} - a promise which returns an array of all agent ids that provides the service when resolved
   */
  async agentsForService(service, caching=true, greedy=true) {
    const aids = await super.agentsForService(service);
    return caching ? aids.map(a => new CachingAgentID(a, null, null, greedy)) : aids;
  }
}

export {AgentID, Services, UnetMessages, Protocol, CachingGateway, CachingAgentID};
