/* unet.js v1.0.0 2021-07-07T03:14:51.424Z */

'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

var fjage_js = require('fjage/dist/esm/fjage.js');
var fjage = require('fjage/dist/esm/fjage');

const DatagramReq$1 = fjage.MessageClass('org.arl.unet.DatagramReq');
const DatagramNtf$1 = fjage.MessageClass('org.arl.unet.DatagramNtf');
const BasebandSignal = fjage.MessageClass('org.arl.unet.bb.BasebandSignal');

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

Object.assign(fjage.Services, UnetServices);

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
  'TestReportNtf'          : fjage.MessageClass('org.arl.unet.TestReportNtf'), 
  'AbnormalTerminationNtf' : fjage.MessageClass('org.arl.unet.AbnormalTerminationNtf'), 
  'CapabilityListRsp'      : fjage.MessageClass('org.arl.unet.CapabilityListRsp'), 
  'CapabilityReq'          : fjage.MessageClass('org.arl.unet.CapabilityReq'), 
  'ClearReq'               : fjage.MessageClass('org.arl.unet.ClearReq'), 
  'DatagramCancelReq'      : fjage.MessageClass('org.arl.unet.DatagramCancelReq'), 
  'DatagramDeliveryNtf'    : fjage.MessageClass('org.arl.unet.DatagramDeliveryNtf'), 
  'DatagramFailureNtf'     : fjage.MessageClass('org.arl.unet.DatagramFailureNtf'), 
  'DatagramNtf'            : fjage.MessageClass('org.arl.unet.DatagramNtf'), 
  'DatagramProgressNtf'    : fjage.MessageClass('org.arl.unet.DatagramProgressNtf'), 
  'DatagramReq'            : fjage.MessageClass('org.arl.unet.DatagramReq'), 
  'ParamChangeNtf'         : fjage.MessageClass('org.arl.unet.ParamChangeNtf'), 
  'RefuseRsp'              : fjage.MessageClass('org.arl.unet.RefuseRsp'), 
  'FailureNtf'             : fjage.MessageClass('org.arl.unet.FailureNtf'), 

  // net
  'DatagramTraceReq'       : fjage.MessageClass('org.arl.unet.net.DatagramTraceReq'), 
  'RouteDiscoveryReq'      : fjage.MessageClass('org.arl.unet.net.RouteDiscoveryReq'), 
  'RouteTraceReq'          : fjage.MessageClass('org.arl.unet.net.RouteTraceReq'), 
  'RouteDiscoveryNtf'      : fjage.MessageClass('org.arl.unet.net.RouteDiscoveryNtf'), 
  'RouteTraceNtf'          : fjage.MessageClass('org.arl.unet.net.RouteTraceNtf'), 

  // phy
  'FecDecodeReq'           : fjage.MessageClass('org.arl.unet.phy.FecDecodeReq'), 
  'RxJanusFrameNtf'        : fjage.MessageClass('org.arl.unet.phy.RxJanusFrameNtf'), 
  'TxJanusFrameReq'        : fjage.MessageClass('org.arl.unet.phy.TxJanusFrameReq'), 
  'BadFrameNtf'            : fjage.MessageClass('org.arl.unet.phy.BadFrameNtf'), 
  'BadRangeNtf'            : fjage.MessageClass('org.arl.unet.phy.BadRangeNtf'),
  'ClearSyncReq'           : fjage.MessageClass('org.arl.unet.phy.ClearSyncReq'), 
  'CollisionNtf'           : fjage.MessageClass('org.arl.unet.phy.CollisionNtf'), 
  'RxFrameNtf'             : fjage.MessageClass('org.arl.unet.phy.RxFrameNtf', DatagramNtf$1), 
  'RxFrameStartNtf'        : fjage.MessageClass('org.arl.unet.phy.RxFrameStartNtf'), 
  'SyncInfoReq'            : fjage.MessageClass('org.arl.unet.phy.SyncInfoReq'), 
  'SyncInfoRsp'            : fjage.MessageClass('org.arl.unet.phy.SyncInfoRsp'), 
  'TxFrameNtf'             : fjage.MessageClass('org.arl.unet.phy.TxFrameNtf'), 
  'TxFrameReq'             : fjage.MessageClass('org.arl.unet.phy.TxFrameReq', DatagramReq$1), 
  'TxFrameStartNtf'        : fjage.MessageClass('org.arl.unet.phy.TxFrameStartNtf'), 
  'TxRawFrameReq'          : fjage.MessageClass('org.arl.unet.phy.TxRawFrameReq'), 

  // addr
  'AddressAllocReq'        : fjage.MessageClass('org.arl.unet.addr.AddressAllocReq'), 
  'AddressAllocRsp'        : fjage.MessageClass('org.arl.unet.addr.AddressAllocRsp'), 
  'AddressResolutionReq'   : fjage.MessageClass('org.arl.unet.addr.AddressResolutionReq'), 
  'AddressResolutionRsp'   : fjage.MessageClass('org.arl.unet.addr.AddressResolutionRsp'), 

  // bb
  'BasebandSignal'         : fjage.MessageClass('org.arl.unet.bb.BasebandSignal'), 
  'RecordBasebandSignalReq' : fjage.MessageClass('org.arl.unet.bb.RecordBasebandSignalReq'), 
  'RxBasebandSignalNtf'    : fjage.MessageClass('org.arl.unet.bb.RxBasebandSignalNtf', BasebandSignal), 
  'TxBasebandSignalReq'    : fjage.MessageClass('org.arl.unet.bb.TxBasebandSignalReq', BasebandSignal), 

  // link
  'LinkStatusNtf'          : fjage.MessageClass('org.arl.unet.link.LinkStatusNtf'), 

  // localization
  'RangeNtf'               : fjage.MessageClass('org.arl.unet.localization.RangeNtf'), 
  'RangeReq'               : fjage.MessageClass('org.arl.unet.localization.RangeReq'), 
  'BeaconReq'              : fjage.MessageClass('org.arl.unet.localization.BeaconReq'), 
  'RespondReq'             : fjage.MessageClass('org.arl.unet.localization.RespondReq'), 
  'InterrogationNtf'       : fjage.MessageClass('org.arl.unet.localization.InterrogationNtf'), 


  // mac
  'ReservationAcceptReq'   : fjage.MessageClass('org.arl.unet.mac.ReservationAcceptReq'), 
  'ReservationCancelReq'   : fjage.MessageClass('org.arl.unet.mac.ReservationCancelReq'), 
  'ReservationReq'         : fjage.MessageClass('org.arl.unet.mac.ReservationReq'), 
  'ReservationRsp'         : fjage.MessageClass('org.arl.unet.mac.ReservationRsp'), 
  'ReservationStatusNtf'   : fjage.MessageClass('org.arl.unet.mac.ReservationStatusNtf'), 
  'RxAckNtf'               : fjage.MessageClass('org.arl.unet.mac.RxAckNtf'), 
  'TxAckReq'               : fjage.MessageClass('org.arl.unet.mac.TxAckReq'), 


  // remote
  'RemoteExecReq'          : fjage.MessageClass('org.arl.unet.remote.RemoteExecReq'), 
  'RemoteFailureNtf'       : fjage.MessageClass('org.arl.unet.remote.RemoteFailureNtf'), 
  'RemoteFileGetReq'       : fjage.MessageClass('org.arl.unet.remote.RemoteFileGetReq'), 
  'RemoteFileNtf'          : fjage.MessageClass('org.arl.unet.remote.RemoteFileNtf'), 
  'RemoteFilePutReq'       : fjage.MessageClass('org.arl.unet.remote.RemoteFilePutReq'), 
  'RemoteSuccessNtf'       : fjage.MessageClass('org.arl.unet.remote.RemoteSuccessNtf'), 
  'RemoteTextNtf'          : fjage.MessageClass('org.arl.unet.remote.RemoteTextNtf'), 
  'RemoteTextReq'          : fjage.MessageClass('org.arl.unet.remote.RemoteTextReq'), 

  // scheduler
  'AddScheduledSleepReq'   : fjage.MessageClass('org.arl.unet.scheduler.AddScheduledSleepReq'), 
  'GetSleepScheduleReq'    : fjage.MessageClass('org.arl.unet.scheduler.GetSleepScheduleReq'), 
  'RemoveScheduledSleepReq' : fjage.MessageClass('org.arl.unet.scheduler.RemoveScheduledSleepReq'), 
  'SleepScheduleRsp'       : fjage.MessageClass('org.arl.unet.scheduler.SleepScheduleRsp'), 
  'WakeFromSleepNtf'       : fjage.MessageClass('org.arl.unet.scheduler.WakeFromSleepNtf'), 

  // state
  'ClearStateReq'          : fjage.MessageClass('org.arl.unet.state.ClearStateReq'), 
  'SaveStateReq'           : fjage.MessageClass('org.arl.unet.state.SaveStateReq')
};

const REQUEST_TIMEOUT = 1000;

const AddressResolutionReq = UnetMessages.AddressResolutionReq;
const DatagramReq = UnetMessages.DatagramReq;
const DatagramNtf = UnetMessages.DatagramNtf;
const RxFrameNtf = UnetMessages.RxFrameNtf;

/**
 * Creates a new UnetSocket to connect to a running Unet instance. This constructor returns a 
 * {@link Promise} instead of the constructed UnetSocket object. Use `await` or `.then()` to get 
 * a reference to the UnetSocket object. Based on if this is run in a Browser or Node.js, 
 * it will internally connect over WebSockets or TCP respectively.
 *
 * 
 * @class UnetSocket
 * @param {string} [hostname] - hostname/ip address of the master container to connect to
 * @param {number} [port] - port number of the master container to connect to
 * @param {string} [path='']  - path of the master container to connect to (for WebSockets)
 * @returns {Promise<UnetSocket>} - Promise which resolves to the UnetSocket object being constructed
 * 
 * @example
 * let socket = await new UnetSocket('localhost', 8081, '/ws/');
 */
class UnetSocket {

  constructor(hostname, port, path='') { 
    return (async () => {
      this.gw = new fjage_js.Gateway({
        hostname : hostname,
        port : port,
        path : path
      });
      this.localProtocol = -1;
      this.remoteAddress = -1;
      this.remoteProtocol = Protocol.DATA;
      this.timeout = 0;
      this.provider = null;
      const alist = await this.gw.agentsForService(fjage.Services.DATAGRAM);
      alist.forEach(a => {this.gw.subscribe(this.gw.topic(a));});
      return this;
    })();
  }

  /**
   * Closes the socket. The socket functionality may not longer be accessed after this method is called.
   * @returns {void}
   */
  close() {
    this.gw.close();
    this.gw = null;
  }

  /**
   * Checks if a socket is closed.
   * @returns {boolean} - true if closed, false if open
   */
  isClosed() { 
    return this.gw == null;
  }

  /**
   * Binds a socket to listen to a specific protocol datagrams.
   * Protocol numbers between Protocol.DATA+1 to Protocol.USER-1 are reserved protocols 
   * and cannot be bound. Unbound sockets listen to all unreserved
   * @param {Protocol} protocol - protocol number to listen for
   * @returns {boolean} - true on success, false on failure
   */
  bind(protocol) {
    if (protocol == Protocol.DATA || (protocol >= Protocol.USER && protocol <= Protocol.MAX)) {
      this.localProtocol = protocol;
      return true;
    }
    return false;
  }
  
  /**
   * Unbinds a socket so that it listens to all unreserved protocols. 
   * Protocol numbers between Protocol.DATA+1 to Protocol.USER-1 are considered reserved.
   * @returns {void}
   */
  unbind() { this.localProtocol = -1;}

  /**
   * Checks if a socket is bound.
   * @returns {boolean} - true if bound to a protocol, false if unbound
   */
  isBound() { return this.localProtocol >= 0;}

  /**
   * Sets the default destination address and destination protocol number for datagrams sent 
   * using this socket. The defaults can be overridden for specific send() calls. 
   * The default protcol number when a socket is opened is Protcol.DATA. 
   * The default node address is undefined. 
   * Protocol numbers between Protocol.DATA+1 to Protocol.USER-1 are considered reserved, 
   * and cannot be used for sending datagrams using the socket.
   * 
   * @param {number} to - default destination node address
   * @param {Protocol} protocol - default protocol number
   * @returns {boolean} - true on success, false on failure
   */
  connect(to, protocol) {
    if (to >= 0 && (protocol == Protocol.DATA || (protocol >= Protocol.USER && protocol <= Protocol.MAX))) {
      this.remoteAddress = to;
      this.remoteProtocol = protocol;
      return true;
    }
    return false;
  }

  /**
   * Resets the default destination address to undefined, and the default protocol number 
   * to Protocol.DATA.
   * @returns {void}
   */
  disconnect() { 
    this.remoteAddress = -1;
    this.remoteProtocol = 0;
  }

  /**
   * Checks if a socket is connected, i.e., has a default destination address and protocol number.
   * @returns {boolean} - true if connected, false otherwise
   */
  isConnected() { return this.remoteAddress >= 0; }

  /**
   * Gets the local node address of the Unet node connected to.
   * @returns {Promise<int>} - local node address, or -1 on error
   */
  async getLocalAddress() { 
    if (this.gw == null) return -1;
    const nodeinfo = await this.gw.agentForService(fjage.Services.NODE_INFO);
    if (nodeinfo == null) return -1;
    const addr = await nodeinfo.get('address');
    return addr != null ? addr : -1;
  }

  /**
   * Gets the protocol number that the socket is bound to.
   * @returns {number}} - protocol number if socket is bound, -1 otherwise
   */
  getLocalProtocol() { return this.localProtocol; }

  /**
   * Gets the default destination node address for a connected socket.
   * @returns {number}} - default destination node address if connected, -1 otherwise
   */
  getRemoteAddress() { return this.remoteAddress; }

  /**
   * Gets the default transmission protocol number.
   * @returns {number}} - default protocol number used to transmit a datagram
   */
  getRemoteProtocol() { return this.remoteProtocol; }

  /**
   * Sets the timeout for datagram reception. A timeout of 0 means the 
   * {@link UnetSocket#receive|receive method} will check any appropriate 
   * Datagram has already been received (and is cached) else return immediately.
   * 
   * @param {number} ms - timeout in milliseconds
   * @returns {void}
   */
  setTimeout(ms) { 
    if (ms < 0) ms = 0;
    this.timeout = ms;
  }

  /**
   * Gets the timeout for datagram reception.
   * @returns {number} - timeout in milliseconds
   */
  getTimeout() { return this.timeout; }

  /**
   * Transmits a datagram to the specified node address using the specified protocol.
   * Protocol numbers between Protocol.DATA+1 to Protocol.USER-1 are considered reserved,
   * and cannot be used for sending datagrams using the socket.
   * @param {number[]|DatagramReq} data - data to be sent over the socket as an Array of bytes or DatagramReq
   * @param {number} to - destination node address
   * @param {number} protocol - protocol number
   * @returns {Promise<boolean>} - true if the Unet node agreed to send out the Datagram, false otherwise
   */
  async send(data, to=this.remoteAddress, protocol=this.remoteProtocol) {
    if (to < 0 || this.gw == null) return false;
    var req;
    if (Array.isArray(data)){
      req = new DatagramReq();
      req.data = data;
      req.to = to;
      req.protocol = protocol;
    } else if (data instanceof DatagramReq){
      req = data;
    } else {
      return false;
    }
    let p = req.protocol;
    if (p != Protocol.DATA && (p < Protocol.USER || p > Protocol.MAX)) return false;
    if (req.recipient == null) {
      if (this.provider == null) this.provider = await this.gw.agentForService(fjage.Services.TRANSPORT);
      if (this.provider == null) this.provider = await this.gw.agentForService(fjage.Services.ROUTING);
      if (this.provider == null) this.provider = await this.gw.agentForService(fjage.Services.LINK);
      if (this.provider == null) this.provider = await this.gw.agentForService(fjage.Services.PHYSICAL);
      if (this.provider == null) this.provider = await this.gw.agentForService(fjage.Services.DATAGRAM);
      if (this.provider == null) return false;
      req.recipient = this.provider;
    }
    const rsp = await this.gw.request(req, REQUEST_TIMEOUT);
    return (rsp != null && rsp.perf == fjage_js.Performative.AGREE);
  }

  /**
   * Receives a datagram sent to the local node and the bound protocol number. If the socket is unbound, 
   * then datagrams with all unreserved protocols are received. Any broadcast datagrams are also received.
   * 
   * @returns {Promise<?DatagramNtf>} - datagram received by the socket
   */
  async receive() { 
    if (this.gw == null) return null;
    return await this.gw.receive(msg => {
      if (msg.__clazz__ != DatagramNtf.__clazz__ && msg.__clazz__ != RxFrameNtf.__clazz__ ) return false;
      let p = msg.protocol;
      if (p == Protocol.DATA || p >= Protocol.USER) {
        return this.localProtocol < 0 || this.localProtocol == p;
      }
      return false;
    }, this.timeout);
  }

  /**
   * Gets a Gateway to provide low-level access to UnetStack.
   * @returns {Gateway} - underlying fjage Gateway supporting this socket
   */
  getGateway() { return this.gw; }

  /**
   * Gets an AgentID providing a specified service for low-level access to UnetStack
   * @param {string} svc - the named service of interest
   * @returns {Promise<?AgentID>} - a promise which returns an {@link AgentID} that provides the service when resolved
   */
  async agentForService(svc) {
    if (this.gw == null) return null;
    return await this.gw.agentForService(svc);
  }

  /**
   * 
   * @param {string} svc - the named service of interest
   * @returns {Promise<AgentID[]>} - a promise which returns an array of {@link AgentID|AgentIDs} that provides the service when resolved
   */
  async agentsForService(svc) {
    if (this.gw == null) return null;
    return await this.gw.agentsForService(svc);
  }

  /**
   * Gets a named AgentID for low-level access to UnetStack.
   * @param {string} name - name of agent
   * @returns {AgentID} - AgentID for the given name
   */
  agent(name) {
    if (this.gw == null) return null;
    return this.gw.agent(name);
  }

  /**
   * Resolve node name to node address.
   * @param {string} nodeName - name of the node to resolve
   * @returns {Promise<?number>} - address of the node, or null if unable to resolve
   */
  async host(nodeName) { 
    const arp = await this.agentForService(fjage.Services.ADDRESS_RESOLUTION);
    if (arp == null) return null;
    const req = new AddressResolutionReq(nodeName);
    req.name = nodeName;
    req.recipient = arp;
    const rsp = await this.gw.request(req, REQUEST_TIMEOUT);
    if (rsp == null || ! Object.prototype.hasOwnProperty.call(rsp, 'address')) return null;
    return rsp.address;
  }
}

Object.defineProperty(exports, 'Gateway', {
  enumerable: true,
  get: function () {
    return fjage_js.Gateway;
  }
});
Object.defineProperty(exports, 'Message', {
  enumerable: true,
  get: function () {
    return fjage_js.Message;
  }
});
Object.defineProperty(exports, 'MessageClass', {
  enumerable: true,
  get: function () {
    return fjage_js.MessageClass;
  }
});
Object.defineProperty(exports, 'Performative', {
  enumerable: true,
  get: function () {
    return fjage_js.Performative;
  }
});
Object.defineProperty(exports, 'AgentID', {
  enumerable: true,
  get: function () {
    return fjage.AgentID;
  }
});
Object.defineProperty(exports, 'Services', {
  enumerable: true,
  get: function () {
    return fjage.Services;
  }
});
exports.Protocol = Protocol;
exports.UnetMessages = UnetMessages;
exports.UnetSocket = UnetSocket;
