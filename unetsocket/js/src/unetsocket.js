import {Gateway, Performative, Services} from '../node_modules/fjage/dist/esm/fjage.js';
import {Protocol, UnetMessages} from './unet-utils.js';

const REQUEST_TIMEOUT = 1000;

const AddressResolutionReq = UnetMessages.AddressResolutionReq;
const DatagramReq = UnetMessages.DatagramReq;
const DatagramNtf = UnetMessages.DatagramNtf;
const RxFrameNtf = UnetMessages.RxFrameNtf;

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

  close() {
    this.gw.close();
    this.gw = null;
  }

  isClosed() { 
    return this.gw == null;
  }

  bind(protocol) {
    if (protocol == Protocol.DATA || (protocol >= Protocol.USER && protocol <= Protocol.MAX)) {
      this.localProtocol = protocol;
      return true;
    }
    return false;
  }

  unbind() { this.localProtocol = -1;}

  isBound() { return this.localProtocol >= 0;}

  connect(to, protocol) {
    if (to >= 0 && (protocol == Protocol.DATA || (protocol >= Protocol.USER && protocol <= Protocol.MAX))) {
      this.remoteAddress = to;
      this.remoteProtocol = protocol;
      return true;
    }
    return false;
  }

  disconnect() { 
    this.remoteAddress = -1;
    this.remoteProtocol = 0;
  }

  isConnected() { return this.remoteAddress >= 0; }

  async getLocalAddress() { 
    if (this.gw == null) return -1;
    const nodeinfo = await this.gw.agentForService(Services.NODE_INFO);
    if (nodeinfo == null) return -1;
    const addr = await nodeinfo.get('address');
    return addr != null ? addr : -1;
  }

  getLocalProtocol() { return this.localProtocol; }

  getRemoteAddress() { return this.remoteAddress; }

  getRemoteProtocol() { return this.remoteProtocol; }

  setTimeout(ms) { 
    if (ms < 0) ms = 0;
    this.timeout = ms;
  }

  getTimeout() { return this.timeout; }

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
    //   log.warning('No idea what to do with ', data);
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

  getGateway() { return this.gw; }

  async agentForService(svc) {
    if (this.gw == null) return null;
    return await this.gw.agentForService(svc);
  }

  async agentsForService(svc) {
    if (this.gw == null) return null;
    return await this.gw.agentsForService(svc);
  }

  agent(name) {
    if (this.gw == null) return null;
    return this.gw.agent(name);
  }

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