import {Gateway, Performative} from 'fjage';
import {Services, UnetMessages, Protocol} from './unetutils';

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
 * @param {string} [port] - port number of the master container to connect to
 * @param {string} [path='']  - path of the master container to connect to (for WebSockets)
 * @returns {Promise<UnetSocket>} - Promise which resolves to the UnetSocket object being constructed
 *
 * @example
 * let socket = await new UnetSocket('localhost', 8081, '/ws/');
 */
export default class UnetSocket {

  constructor(hostname, port, path='') {
    return (async () => {
      this.gw = new Gateway({
        hostname : hostname,
        port : port,
        path : path
      });
      this.localProtocol = -1;
      this.remoteAddress = -1;
      this.remoteProtocol = Protocol.DATA;
      this.timeout = 0;
      this.provider = null;
      const alist = await this.gw.agentsForService(Services.DATAGRAM);
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
    const nodeinfo = await this.gw.agentForService(Services.NODE_INFO);
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
      if (this.provider == null) this.provider = await this.gw.agentForService(Services.TRANSPORT);
      if (this.provider == null) this.provider = await this.gw.agentForService(Services.ROUTING);
      if (this.provider == null) this.provider = await this.gw.agentForService(Services.LINK);
      if (this.provider == null) this.provider = await this.gw.agentForService(Services.PHYSICAL);
      if (this.provider == null) this.provider = await this.gw.agentForService(Services.DATAGRAM);
      if (this.provider == null) return false;
      req.recipient = this.provider;
    }
    const rsp = await this.gw.request(req, REQUEST_TIMEOUT);
    return (rsp != null && rsp.perf == Performative.AGREE);
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
   * @param {Boolean} caching - if the AgentID should cache parameters
   * @returns {Promise<?AgentID>} - a promise which returns an {@link AgentID} that provides the service when resolved
   */
  async agentForService(svc, caching=true) {
    if (this.gw == null) return null;
    return await this.gw.agentForService(svc, caching);
  }

  /**
   *
   * @param {string} svc - the named service of interest
   * @param {Boolean} caching - if the AgentID should cache parameters
   * @returns {Promise<AgentID[]>} - a promise which returns an array of {@link AgentID|AgentIDs} that provides the service when resolved
   */
  async agentsForService(svc, caching=true) {
    if (this.gw == null) return null;
    return await this.gw.agentsForService(svc, caching``);
  }

  /**
   * Gets a named AgentID for low-level access to UnetStack.
   * @param {string} name - name of agent
   * @param {Boolean} caching - if the AgentID should cache parameters
   * @returns {AgentID} - AgentID for the given name
   */
  agent(name, caching=true) {
    if (this.gw == null) return null;
    return this.gw.agent(name, caching);
  }

  /**
   * Resolve node name to node address.
   * @param {string} nodeName - name of the node to resolve
   * @returns {Promise<?number>} - address of the node, or null if unable to resolve
   */
  async host(nodeName) {
    const arp = await this.agentForService(Services.ADDRESS_RESOLUTION);
    if (arp == null) return null;
    const req = new AddressResolutionReq(nodeName);
    req.name = nodeName;
    req.recipient = arp;
    const rsp = await this.gw.request(req, REQUEST_TIMEOUT);
    if (rsp == null || ! Object.prototype.hasOwnProperty.call(rsp, 'address')) return null;
    return rsp.address;
  }
}