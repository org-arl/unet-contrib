/* unet.js v1.0.0 2021-06-18T03:44:59.008Z */

/* fjage.js v1.9.1-rc3 2021-06-16T09:37:46.611Z */

/* global window self */

const isBrowser = typeof window !== 'undefined' && typeof window.document !== 'undefined';

/* eslint-disable no-restricted-globals */
const isWebWorker = typeof self === 'object'
  && self.constructor
  && self.constructor.name === 'DedicatedWorkerGlobalScope';
/* eslint-enable no-restricted-globals */

const isNode = typeof process !== 'undefined'
  && process.versions != null
  && process.versions.node != null;

/**
 * @see https://github.com/jsdom/jsdom/releases/tag/12.0.0
 * @see https://github.com/jsdom/jsdom/issues/1537
 */
/* eslint-disable no-undef */
const isJsDom = () => (typeof window !== 'undefined' && window.name === 'nodejs')
  || navigator.userAgent.includes('Node.js')
  || navigator.userAgent.includes('jsdom');

const SOCKET_OPEN = 'open';
const SOCKET_OPENING = 'opening';

var createConnection;

/**
 * @class
 * @ignore
 */
class TCPconnector {

  /**
    * Create an TCPconnector
    * @param {Object} opts
    * @param {String} opts.hostname - ip address/hostname of the master container to connect to
    * @param {Number} opts.port - port number of the master container to connect to
    */
  constructor(opts = {}) {
    this.url = new URL('tcp://localhost');
    let host = opts.hostname || 'localhost';
    let port = opts.port || -1;
    this.url.hostname = opts.hostname;
    this.url.port = opts.port;
    this._buf = '';
    this.pendingOnOpen = [];              // list of callbacks make as soon as gateway is open
    this.connListeners = [];              // external listeners wanting to listen connection events
    this._sockInit(host, port);
  }


  _sendConnEvent(val) {
    this.connListeners.forEach(l => {
      l && {}.toString.call(l) === '[object Function]' && l(val);
    });
  }

  _sockInit(host, port){
    if (!createConnection){
      try {
        import('net').then(module => {
          createConnection = module.createConnection;
          this._sockSetup(host, port);
        });
      }catch(error){
        if(this.debug) console.log('Unable to import net module');
      }
    }else {
      this._sockSetup(host, port);
    }  
  }

  _sockSetup(host, port){
    if(!createConnection) return;
    try{
      this.sock = createConnection({ 'host': host, 'port': port });
      this.sock.setEncoding('utf8');
      this.sock.on('connect', this._onSockOpen.bind(this));
      this.sock.on('error', this._sockReconnect.bind(this));
      this.sock.on('close', () => {this._sendConnEvent(false);});
      this.sock.send = data => {this.sock.write(data);};
    } catch (error) {
      if(this.debug) console.log('Connection failed to ', this.sock.host + ':' + this.sock.port);
      return;
    }
  }

  _sockReconnect(){
    if (this._firstConn || !this.keepAlive || this.sock.readyState == SOCKET_OPENING || this.sock.readyState == SOCKET_OPEN) return;
    if (this._firstReConn) this._sendConnEvent(false);
    this._firstReConn = false;
    if(this.debug) console.log('Reconnecting to ', this.sock.remoteAddress + ':' + this.sock.remotePort);
    setTimeout(() => {
      this.pendingOnOpen = [];
      this._sockSetup(this.sock.url);
    }, this._reconnectTime);
  }

  _onSockOpen() {
    this._sendConnEvent(true);
    this.sock.on('close', this._sockReconnect.bind(this));
    this.sock.on('data', this._processSockData.bind(this));
    this.pendingOnOpen.forEach(cb => cb());
    this.pendingOnOpen.length = 0;
    this._buf = '';
  }

  _processSockData(s){
    this._buf += s;
    var lines = this._buf.split('\n');
    lines.forEach((l, idx) => {
      if (idx < lines.length-1){
        if (l && this._onSockRx) this._onSockRx.call(this,l);
      } else {
        this._buf = l;
      }
    });
  }

  toString(){
    let s = '';
    s += 'TCPConnector [' + this.sock ? this.sock.remoteAddress.toString() + ':' + this.sock.remotePort.toString() : '' + ']';
    return s;
  }

  /**
   * Write a string to the connector
   * @param {String} s - string to be written out of the connector to the master
   */
  write(s){
    if (!this.sock || this.sock.readyState == SOCKET_OPENING){
      this.pendingOnOpen.push(() => {
        this.sock.send(s+'\n');
      });
      return true;
    } else if (this.sock.readyState == SOCKET_OPEN) {
      this.sock.send(s+'\n');
      return true;
    }
    return false;
  }

  /**
   * Set a callback for receiving incoming strings from the connector
   * @param {TCPConnector~ReadCallback} cb - callback that is called when the connector gets a string
   */
  setReadCallback(cb){
    if (cb && {}.toString.call(cb) === '[object Function]') this._onSockRx = cb;
  }

  /**
   * @callback TCPConnector~ReadCallback
   * @ignore
   * @param {string} s - incoming message string
   */
  
  /**
   * Add listener for connection events
   * @param {function} listener - a listener callback that is called when the connection is opened/closed
   */
  addConnectionListener(listener){
    this.connListeners.push(listener);
  }

  /**
   * Remove listener for connection events
   * @param {function} listener - remove the listener for connection
   */
  removeConnectionListener(listener) {
    let ndx = this.connListeners.indexOf(listener);
    if (ndx >= 0) {
      this.connListeners.splice(ndx, 1);
      return true;
    }
    return false;
  }

  /**
   * Close the connector
   */
  close(){
    if (!this.sock) return;
    if (this.sock.readyState == SOCKET_OPENING) {
      this.pendingOnOpen.push(() => {
        this.sock.send('{"alive": false}\n');
        this.sock.onclose = null;
        this.sock.destroy();
      });
    } else if (this.sock.readyState == SOCKET_OPEN) {
      this.sock.send('{"alive": false}\n');
      this.sock.onclose = null;
      this.sock.destroy();
    }
  }
}

const DEFAULT_RECONNECT_TIME = 5000;       // ms, delay between retries to connect to the server.

/**
 * @class
 * @ignore
 */
class WSConnector {

  /**
   * Create an WSConnector
   * @param {Object} opts
   * @param {String} opts.hostname - hostname/ip address of the master container to connect to
   * @param {Number} opts.port - port number of the master container to connect to
   * @param {String} opts.pathname - path of the master container to connect to
   * @param {boolean} opts.keepAlive - try to reconnect if the connection is lost
   * @param {Number} opts.reconnectTime - time before reconnection is attempted after an error
   */
  constructor(opts = {}) {
    this.url = new URL('ws://localhost');
    this.url.hostname = opts.hostname;      
    this.url.port = opts.port;
    this.url.pathname = opts.pathname;
    this._reconnectTime = opts.reconnectTime || DEFAULT_RECONNECT_TIME;
    this._keepAlive = opts.keepAlive;
    this.debug = opts.debug || false;      // debug info to be logged to console?
    this._firstConn = true;               // if the Gateway has managed to connect to a server before
    this._firstReConn = true;             // if the Gateway has attempted to reconnect to a server before
    this.pendingOnOpen = [];              // list of callbacks make as soon as gateway is open
    this.connListeners = [];              // external listeners wanting to listen connection events
    this._websockSetup(this.url);
  }

  _sendConnEvent(val) {
    this.connListeners.forEach(l => {
      l && {}.toString.call(l) === '[object Function]' && l(val);
    });
  }

  _websockSetup(url){
    try {
      this.sock = new WebSocket(url);
      this.sock.onerror = this._websockReconnect.bind(this);
      this.sock.onopen = this._onWebsockOpen.bind(this);
      this.sock.onclose = () => {this._sendConnEvent(false);};
    } catch (error) {
      if(this.debug) console.log('Connection failed to ', url);
      return;
    }
  }

  _websockReconnect(){
    if (this._firstConn || !this._keepAlive || this.sock.readyState == this.sock.CONNECTING || this.sock.readyState == this.sock.OPEN) return;
    if (this._firstReConn) this._sendConnEvent(false);
    this._firstReConn = false;
    if(this.debug) console.log('Reconnecting to ', this.sock.url);
    setTimeout(() => {
      this.pendingOnOpen = [];
      this._websockSetup(this.sock.url);
    }, this._reconnectTime);
  }

  _onWebsockOpen() {
    if(this.debug) console.log('Connected to ', this.sock.url);
    this._sendConnEvent(true);
    this.sock.onclose = this._websockReconnect.bind(this);
    this.sock.onmessage = event => { if (this._onWebsockRx) this._onWebsockRx.call(this,event.data); };
    this._firstConn = false;
    this._firstReConn = true;
    this.pendingOnOpen.forEach(cb => cb());
    this.pendingOnOpen.length = 0;
  }

  toString(){
    let s = '';
    s += 'WSConnector [' + this.sock ? this.sock.url.toString() : '' + ']';
    return s;
  }

  /**
   * Write a string to the connector
   * @param {String} s - string to be written out of the connector to the master
   */
  write(s){
    if (!this.sock || this.sock.readyState == this.sock.CONNECTING){
      this.pendingOnOpen.push(() => {
        this.sock.send(s+'\n');
      });
      return true;
    } else if (this.sock.readyState == this.sock.OPEN) {
      this.sock.send(s+'\n');
      return true;
    }
    return false;
  }

  /**
   * Set a callback for receiving incoming strings from the connector
   * @param {WSConnector~ReadCallback} cb - callback that is called when the connector gets a string
   * @ignore
   */
  setReadCallback(cb){
    if (cb && {}.toString.call(cb) === '[object Function]') this._onWebsockRx = cb;
  }

  /**
   * @callback WSConnector~ReadCallback
   * @ignore
   * @param {string} s - incoming message string
   */
  
  /**
   * Add listener for connection events
   * @param {function} listener - a listener callback that is called when the connection is opened/closed
   */
  addConnectionListener(listener){
    this.connListeners.push(listener);
  }

  /**
   * Remove listener for connection events
   * @param {function} listener - remove the listener for connection
   */
  removeConnectionListener(listener) {
    let ndx = this.connListeners.indexOf(listener);
    if (ndx >= 0) {
      this.connListeners.splice(ndx, 1);
      return true;
    }
    return false;
  }

  /**
   * Close the connector
   */
  close(){
    if (!this.sock) return;
    if (this.sock.readyState == this.sock.CONNECTING) {
      this.pendingOnOpen.push(() => {
        this.sock.send('{"alive": false}\n');
        this.sock.onclose = null;
        this.sock.close();
      });
    } else if (this.sock.readyState == this.sock.OPEN) {
      this.sock.send('{"alive": false}\n');
      this.sock.onclose = null;
      this.sock.close();
    }
  }
}

/* global global Buffer */

const DEFAULT_QUEUE_SIZE = 128;        // max number of old unreceived messages to store

/**
 * An action represented by a message. The performative actions are a subset of the
 * FIPA ACL recommendations for interagent communication.
 */
const Performative = {
  REQUEST: 'REQUEST',               // Request an action to be performed
  AGREE: 'AGREE',                   // Agree to performing the requested action
  REFUSE: 'REFUSE',                 // Refuse to perform the requested action
  FAILURE: 'FAILURE',               // Notification of failure to perform a requested or agreed action
  INFORM: 'INFORM',                 // Notification of an event
  CONFIRM: 'CONFIRM',               // Confirm that the answer to a query is true
  DISCONFIRM: 'DISCONFIRM',         // Confirm that the answer to a query is false
  QUERY_IF: 'QUERY_IF',             // Query if some statement is true or false
  NOT_UNDERSTOOD: 'NOT_UNDERSTOOD', // Notification that a message was not understood
  CFP: 'CFP',                       // Call for proposal
  PROPOSE: 'PROPOSE',               // Response for CFP
  CANCEL: 'CANCEL'                  // Cancel pending request
};

/**
 * An identifier for an agent or a topic.
 */
class AgentID {

  /**
    * Create an AgentID
    * @param {string} name - name of the agent.
    * @param {boolean} topic - name of topic.
    * @param {Gateway} owner - Gateway owner for this AgentID.
    */
  constructor(name, topic, owner) {
    this.name = name;
    this.topic = topic;
    this.owner = owner;
  }

  /**
   * Gets the name of the agent or topic.
   *
   * @return {string} name of agent or topic.
   */
  getName() {
    return this.name;
  }

  /**
   * Returns true if the agent id represents a topic.
   *
   * @return {boolean} true if the agent id represents a topic,
   *         false if it represents an agent.
   */
  isTopic() {
    return this.topic;
  }

  /**
   * Sends a message to the agent represented by this id.
   *
   * @param {string} msg - message to send.
   * @returns {void}
   */
  send(msg) {
    msg.recipient = this.toJSON();
    this.owner.send(msg);
  }

  /**
   * Sends a request to the agent represented by this id and waits for
   * a return message for 1 second.
   *
   * @param {Message} msg - request to send.
   * @param {number} [timeout=1000] - timeout in milliseconds.
   * @return {Message} response.
   */
  request(msg, timeout=1000) {
    msg.recipient = this.toJSON();
    return this.owner.request(msg, timeout);
  }

  /**
   * Gets a string representation of the agent id.
   *
   * @return {string} string representation of the agent id.
   */
  toString() {
    return this.toJSON() + ((this.owner && this.owner.connector) ? ` on ${this.owner.connector.url}` : '');
  }

  /**
   * Gets a JSON string representation of the agent id.
   *
   * @return {string} JSON string representation of the agent id.
   */
  toJSON() {
    return (this.topic ? '#' : '') + this.name;
  }

  /**
   * Sets parameter(s) on the Agent referred to by this AgentID.
   *
   * @param {(string|string[])} params - parameters name(s) to be set.
   * @param {(Object|Object[])} values - parameters value(s) to be set.
   * @param {number} [index=-1] - index of parameter(s) to be set.
   * @param {number} [timeout=5000] - timeout for the response.
   * @return {Promise<(Object|Object[])>} - A promise which returns the new value(s) of the parameters
   */
  async set (params, values, index=-1, timeout=5000) {
    if (!params) return null;
    let msg = new ParameterReq$1();
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
    const rsp = await this.owner.request(msg, timeout);
    var ret = Array.isArray(params) ? new Array(params.length).fill(null) : null;
    if (!rsp || rsp.perf != Performative.INFORM || !rsp.param) return ret;
    if (Array.isArray(params)) {
      if (!rsp.values) rsp.values = {};
      if (rsp.param) rsp.values[rsp.param] = rsp.value;
      const rvals = Object.keys(rsp.values);
      return params.map( p => {
        let f = rvals.find(rv => rv.endsWith(p));
        return f ? rsp.values[f] : null;
      });
    } else {
      return rsp.value;
    }
  }


  /**
   * Gets parameter(s) on the Agent referred to by this AgentID.
   *
   * @param {(null|string|string[])} params - parameters name(s) to be get. null implies get value of all parameters on the Agent.
   * @param {number} [index=-1] - index of parameter(s) to be get.
   * @param {number} [timeout=5000] - timeout for the response.
   * @return {Promise<(Object|Object[])>} - A promise which returns the value(s) of the parameters
   */
  async get(params, index=-1, timeout=5000) {
    let msg = new ParameterReq$1();
    msg.recipient = this.name;
    if (params){
      if (Array.isArray(params)) msg.requests = params.map(p => {return {'param': p};});
      else msg.param = params;
    }
    msg.index = Number.isInteger(index) ? index : -1;
    const rsp = await this.owner.request(msg, timeout);
    var ret = Array.isArray(params) ? new Array(params.length).fill(null) : null;
    if (!rsp || rsp.perf != Performative.INFORM || (params && (!rsp.param))) return ret;
    // Request for listing of all parameters.
    if (!params) {
      if (!rsp.values) rsp.values = {};
      if (rsp.param) rsp.values[rsp.param] = rsp.value;
      return rsp.values;
    } else if (Array.isArray(params)) {
      if (!rsp.values) rsp.values = {};
      if (rsp.param) rsp.values[rsp.param] = rsp.value;
      const rvals = Object.keys(rsp.values);
      return params.map(p => {
        let f = rvals.find(rv => rv.endsWith(p));
        return f ? rsp.values[f] : null;
      });
    } else {
      return rsp.value;
    }
  }
}

/**
 * Base class for messages transmitted by one agent to another. Creates an empty message.
 * @param {Message} inReplyTo - message to which this response corresponds to.
 * @param {Performative} perf  - performative
 */
class Message {

  constructor(inReplyTo={msgID:null, sender:null}, perf='') {
    this.__clazz__ = 'org.arl.fjage.Message';
    this.msgID = _guid(8);
    this.sender = null;
    this.recipient = inReplyTo.sender;
    this.perf = perf;
    this.inReplyTo = inReplyTo.msgID || null;
  }

  /**
   * Gets a string representation of the message.
   *
   * @return {string} string representation.
   */
  toString() {
    let s = '';
    let suffix = '';
    if (!this.__clazz__) return '';
    let clazz = this.__clazz__;
    clazz = clazz.replace(/^.*\./, '');
    let perf = this.perf;
    for (var k in this) {
      if (k.startsWith('__')) continue;
      if (k == 'sender') continue;
      if (k == 'recipient') continue;
      if (k == 'msgID') continue;
      if (k == 'perf') continue;
      if (k == 'inReplyTo') continue;
      if (typeof this[k] == 'object') {
        suffix = ' ...';
        continue;
      }
      s += ' ' + k + ':' + this[k];
    }
    s += suffix;
    return clazz+':'+perf+'['+s.replace(/^ /, '')+']';
  }

  // convert a message into a JSON string
  // NOTE: we don't do any base64 encoding for TX as
  //       we don't know what data type is intended
  /** @private */
  _serialize() {
    let clazz = this.__clazz__ || 'org.arl.fjage.Message';
    let data = JSON.stringify(this, (k,v) => {
      if (k.startsWith('__')) return;
      return v;
    });
    return '{ "clazz": "'+clazz+'", "data": '+data+' }';
  }

  // inflate a data dictionary into the message
  /** @private */
  _inflate(data) {
    for (var key in data)
      this[key] = data[key];
  }

  // convert a dictionary (usually from decoding JSON) into a message
  /** @private */
  static _deserialize(obj) {
    if (typeof obj == 'string' || obj instanceof String) {
      try {
        obj = JSON.parse(obj);
      }catch(e){
        return null;
      }
    }
    let qclazz = obj.clazz;
    let clazz = qclazz.replace(/^.*\./, '');
    let rv = MessageClass[clazz] ? new MessageClass[clazz] : new Message();
    rv.__clazz__ = qclazz;
    rv._inflate(obj.data);
    return rv;
  }
}

/**
 * Creates a gateway connecting to a specified master container over Websockets.
 *
 * @param {Object} opts
 * @param {String} opts.hostname - hostname/ip address of the master container to connect to
 * @param {Number} opts.port - port number of the master container to connect to
 * @param {String} opts.pathname - path of the master container to connect to
 * @param {String} opts.keepAlive - try to reconnect if the connection is lost
 * @param {int} opts.queueSize     - size of the queue of received messages that haven't been consumed yet
 * @param {int} opts.timeout     - timeout for fjage level messages
 *//**
 * The old, deprecated way to construct a Gateway
 * @deprecated Since version 1.9.1
 * @param {string} hostname - hostname of the master container to connect to
 * @param {int} port        - port of the master container to connect to
 * @param {string} pathname - path of the master container to connect to
 * @param {int} timeout     - timeout for fjage level messages
 */
class Gateway {

  constructor(opts = {}, port, pathname='/ws/', timeout=1000) {
    // Support for deprecated constructor
    if (typeof opts === 'string' || opts instanceof String){
      opts = {
        'hostname': opts,
        'port' : port || gObj.location.port, 
        'pathname' : pathname,
        'timeout' : timeout
      };
      console.warn('Deprecated use of Gateway constructor');
    }
    opts = Object.assign({}, GATEWAY_DEFAULTS, opts);
    var url = DEFAULT_URL;
    url.hostname = opts.hostname;
    url.port = opts.port;
    url.pathname = opts.pathname;
    let existing = this._getGWCache(url);
    if (existing) return existing;
    this._timeout = opts.timeout;         // timeout for fjage level messages (agentForService etc)
    this._keepAlive = opts.keepAlive;     // reconnect if connection gets closed/errored
    this._queueSize = opts.queueSize;      // size of queue
    this.pending = {};                    // msgid to callback mapping for pending requests to server
    this.subscriptions = {};              // hashset for all topics that are subscribed
    this.listener = {};                   // set of callbacks that want to listen to incoming messages
    this.eventListeners = {};             // external listeners wanting to listen internal events
    this.queue = [];                      // incoming message queue
    this.debug = false;                   // debug info to be logged to console?
    this.aid = new AgentID(isBrowser ? 'WebGW-' : 'NodeGW-'+_guid(4));         // gateway agent name
    this.connector = this._createConnector(url);
    this._addGWCache(this);
  }

  /** @private */
  _sendEvent(type, val) {
    if (Array.isArray(this.eventListeners[type])) {
      this.eventListeners[type].forEach(l => {
        l && {}.toString.call(l) === '[object Function]' && l(val);
      });
    }
  }

  /** @private */
  _onMsgRx(data) {
    var obj;
    if (this.debug) console.log('< '+data);
    this._sendEvent('rx', data);
    try {
      obj = JSON.parse(data, _decodeBase64);
    }catch(e){
      return;
    }
    this._sendEvent('rxp', obj);
    if ('id' in obj && obj.id in this.pending) {
      // response to a pending request to master
      this.pending[obj.id](obj);
      delete this.pending[obj.id];
    } else if (obj.action == 'send') {
      // incoming message from master
      let msg = Message._deserialize(obj.message);
      if (!msg) return;
      this._sendEvent('rxmsg', msg);
      if ((msg.recipient == this.aid.toJSON() )|| this.subscriptions[msg.recipient]) {
        var consumed = false;
        if (Array.isArray(this.eventListeners['message'])){
          for (var i = 0; i < this.eventListeners['message'].length; i++) {
            if (this.eventListeners['message'][i](msg)) {
              consumed = true;
              break;
            }
          }
        }
        // iterate over internal callbacks, until one consumes the message
        for (var key in this.listener){
          // callback returns true if it has consumed the message
          if (this.listener[key](msg)) {
            consumed = true;
            break;
          }
        }
        if(!consumed) {
          if (this.queue.length >= this._queueSize) this.queue.shift();
          this.queue.push(msg);
        }
      }
    } else {
      // respond to standard requests that every container must
      let rsp = { id: obj.id, inResponseTo: obj.action };
      switch (obj.action) {
      case 'agents':
        rsp.agentIDs = [this.aid.getName()];
        break;
      case 'containsAgent':
        rsp.answer = (obj.agentID == this.aid.getName());
        break;
      case 'services':
        rsp.services = [];
        break;
      case 'agentForService':
        rsp.agentID = '';
        break;
      case 'agentsForService':
        rsp.agentIDs = [];
        break;
      default:
        rsp = undefined;
      }
      if (rsp) this._msgTx(rsp);
    }
  }

  /** @private */
  _msgTx(s) {
    if (typeof s != 'string' && !(s instanceof String)) s = JSON.stringify(s);
    if(this.debug) console.log('> '+s);
    this._sendEvent('tx', s);
    return this.connector.write(s);
  }

  /** @private */
  _msgTxRx(rq) {
    rq.id = _guid(8);
    return new Promise(resolve => {
      let timer = setTimeout(() => {
        delete this.pending[rq.id];
        if (this.debug) console.log('Receive Timeout : ' + rq);
        resolve();
      }, 8*this._timeout);
      this.pending[rq.id] = rsp => {
        clearTimeout(timer);
        resolve(rsp);
      };
      if (!this._msgTx.call(this,rq)) {
        clearTimeout(timer);
        delete this.pending[rq.id];
        if (this.debug) console.log('Transmit Timeout : ' + rq);
        resolve();
      }
    });
  }

  /** @private */
  _createConnector(url){
    let conn;
    if (url.protocol.startsWith('ws')){
      conn =  new WSConnector({
        'hostname':url.hostname,
        'port':url.port,
        'pathname':url.pathname,
        'keepAlive': this._keepAlive
      });
    }else if (url.protocol.startsWith('tcp')){
      conn = new TCPconnector({
        'hostname':url.hostname,
        'port':url.port,
        'keepAlive': this._keepAlive
      });
    } else return null;
    conn.setReadCallback(this._onMsgRx.bind(this));
    conn.addConnectionListener(state => {
      if (state == true){
        this.flush();
        this.connector.write('{"alive": true}');
        this._update_watch();
      }
    });
    return conn;
  }

  /** @private */
  _matchMessage(filter, msg){
    if (typeof filter == 'string' || filter instanceof String) {
      return 'inReplyTo' in msg && msg.inReplyTo == filter;
    } else if (Object.prototype.hasOwnProperty.call(filter, 'msgID')) {
      return 'inReplyTo' in msg && msg.inReplyTo == filter.msgID;
    } else if (filter.__proto__.name == 'Message') {
      return filter.__clazz__ == msg.__clazz__;
    } else if (typeof filter == 'function') {
      return filter(msg);
    } else {
      return msg instanceof filter;
    }
  }

  /** @private */
  _getMessageFromQueue(filter) {
    if (!this.queue.length) return;
    if (!filter) return this.queue.shift();

    let matchedMsg = this.queue.find( msg => this._matchMessage(filter, msg));
    if (matchedMsg) this.queue.splice(this.queue.indexOf(matchedMsg), 1);

    return matchedMsg;
  }

  /** @private */
  _getGWCache(url){
    if (!gObj.fjage || !gObj.fjage.gateways) return null;
    var f = gObj.fjage.gateways.filter(g => g.connector.url.toString() == url.toString());
    if (f.length ) return f[0];
    return null;
  }
  
  /** @private */
  _addGWCache(gw){
    if (!gObj.fjage || !gObj.fjage.gateways) return;
    gObj.fjage.gateways.push(gw);
  }
  
  /** @private */
  _removeGWCache(gw){
    if (!gObj.fjage || !gObj.fjage.gateways) return;
    var index = gObj.fjage.gateways.indexOf(gw);
    if (index != null) gObj.fjage.gateways.splice(index,1);
  }

  /** @private */
  _update_watch() {
    // FIXME : Turning off wantsMessagesFor in fjagejs for now as it breaks multiple browser
    // windows connecting to the same master container.
    //
    // let watch = Object.keys(this.subscriptions);
    // watch.push(this.aid.getName());
    // let rq = { action: 'wantsMessagesFor', agentIDs: watch };
    // this._msgTx(rq);
  }

  /**
   * Add an event listener to listen to various events happening on this Gateway
   *
   * @param {string} type - type of event to be listened to.
   * @param {function} listener - new callback/function to be called when the event happens
   * @returns {void}
   */
  addEventListener(type, listener) {
    if (!Array.isArray(this.eventListeners[type])){
      this.eventListeners[type] = [];
    }
    this.eventListeners[type].push(listener);
  }

  /**
   * Remove an event listener.
   *
   * @param {string} type - type of event the listener was for
   * @param {function} listener - callback/function which was to be called when the event happens
   * @returns {void}
   */
  removeEventListener(type, listener) {
    if (!this.eventListeners[type]) return;
    let ndx = this.eventListeners[type].indexOf(listener);
    if (ndx >= 0) this.eventListeners[type].splice(ndx, 1);
  }

  /**
   * Add a new listener to listen to all {Message}s sent to this Gateway
   *
   * @param {function} listener - new callback/function to be called when a {Message} is received.
   * @returns {void}
   */
  addMessageListener(listener) {
    this.addEventListener('message',listener);
  }

  /**
   * Remove a message listener.
   *
   * @param {function} listener - removes a previously registered listener/callback.
   * @returns {void}
   */
  removeMessageListener(listener) {
    this.removeEventListener('message', listener);
  }

  /**
   * Add a new listener to get notified when the connection to master is created and terminated.
   *
   * @param {function} listener - new callback/function to be called connection to master is created and terminated.
   * @returns {void}
   */
  addConnListener(listener) {
    this.addEventListener('conn', listener);
  }

  /**
   * Remove a connection listener.
   *
   * @param {function} listener - removes a previously registered listener/callback.
   * @returns {void}
   */
  removeConnListener(listener) {
    this.removeEventListener('conn', listener);
  }

  /**
   * Gets the agent ID associated with the gateway.
   *
   * @return {string} agent ID
   */
  getAgentID() {
    return this.aid;
  }

  /**
   * Get an AgentID for a given agent name.
   *
   * @param {string} name - name of agent
   * @return {AgentID} AgentID for the given name.
   */
  agent(name) {
    return new AgentID(name, false, this);
  }

  /**
   * Returns an object representing the named topic.
   *
   * @param {string|AgentID} topic - name of the topic or AgentID.
   * @param {string} topic2 - name of the topic if the topic param is an AgentID.
   * @returns {AgentID} object representing the topic.
   */
  topic(topic, topic2) {
    if (typeof topic == 'string' || topic instanceof String) return new AgentID(topic, true, this);
    if (topic instanceof AgentID) {
      if (topic.isTopic()) return topic;
      return new AgentID(topic.getName()+(topic2 ? '__' + topic2 : '')+'__ntf', true, this);
    }
  }

  /**
   * Subscribes the gateway to receive all messages sent to the given topic.
   *
   * @param {AgentID} topic - the topic to subscribe to.
   * @return {boolean} true if the subscription is successful, false otherwise.
   */
  subscribe(topic) {
    if (!topic.isTopic()) topic = new AgentID(topic.getName() + '__ntf', true, this);
    this.subscriptions[topic.toJSON()] = true;
    this._update_watch();
  }

  /**
   * Unsubscribes the gateway from a given topic.
   *
   * @param {AgentID} topic - the topic to unsubscribe.
   * @returns {void}
   */
  unsubscribe(topic) {
    if (!topic.isTopic()) topic = new AgentID(topic.getName() + '__ntf', true, this);
    delete this.subscriptions[topic.toJSON()];
    this._update_watch();
  }

  /**
   * Finds an agent that provides a named service. If multiple agents are registered
   * to provide a given service, any of the agents' id may be returned.
   *
   * @param {string} service - service the named service of interest.
   * @return {Promise} - A promise which returns an agent id for an agent that provides the service when resolved.
   */
  async agentForService(service) {
    let rq = { action: 'agentForService', service: service };
    let rsp = await this._msgTxRx(rq);
    if (!rsp || !rsp.agentID) return;
    return new AgentID(rsp.agentID, false, this);
  }

  /**
   * Finds all agents that provides a named service.
   *
   * @param {string} service - service the named service of interest.
   * @return {Promise} - A promise which returns an array of all agent ids that provides the service when resolved.
   */
  async agentsForService(service) {
    let rq = { action: 'agentsForService', service: service };
    let rsp = await this._msgTxRx(rq);
    let aids = [];
    if (!rsp || !Array.isArray(rsp.agentIDs)) return aids;
    for (var i = 0; i < rsp.agentIDs.length; i++)
      aids.push(new AgentID(rsp.agentIDs[i], false, this));
    return aids;
  }

  /**
   * Sends a message to the recipient indicated in the message. The recipient
   * may be an agent or a topic.
   *
   * @param {Message} msg - message to be sent.
   * @returns {boolean} status - if sending was successful.
   */
  send(msg) {
    msg.sender = this.aid.toJSON();
    if (msg.perf == '') {
      if (msg.__clazz__.endsWith('Req')) msg.perf = Performative.REQUEST;
      else msg.perf = Performative.INFORM;
    }
    this._sendEvent('txmsg', msg);
    let rq = JSON.stringify({ action: 'send', relay: true, message: '###MSG###' });
    rq = rq.replace('"###MSG###"', msg._serialize());
    return !!this._msgTx(rq);
  }

  /**
   * Flush the Gateway queue for all pending messages. This drops all the pending messages.
   * @returns {void}
   *
   */
  flush() {
    this.queue.length = 0;
  }

  /**
   * Sends a request and waits for a response. This method returns a {Promise} which resolves when a response is received or if no response is received after the timeout.
   *
   * @param {string} msg - message to send.
   * @param {number} [timeout=1000] - timeout in milliseconds.
   * @return {Promise<Message|null>} a promise which resolves with the received response message, null on timeout.
   */
  async request(msg, timeout=1000) {
    this.send(msg);
    return this.receive(msg, timeout);
  }

  /**
   * Returns a response message received by the gateway. This method returns a {Promise} which resolves when a response is received or if no response is received after the timeout.
   *
   * @param {function} [filter=undefined] - original message to which a response is expected, or a MessageClass of the type of message to match, or a closure to use to match against the message.
   * @param {number} [timeout=0] - timeout in milliseconds.
   * @return {Promise<Message|null>} received response message, null on timeout.
   */
  async receive(filter=undefined, timeout=0) {
    return new Promise(resolve => {
      let msg = this._getMessageFromQueue.call(this,filter);
      if (msg) {
        resolve(msg);
        return;
      }
      if (timeout == 0) {
        if (this.debug) console.log('Receive Timeout : ' + filter);
        resolve();
        return;
      }
      let lid = _guid(8);
      let timer;
      if (timeout > 0){
        timer = setTimeout(() => {
          delete this.listener[lid];
          if (this.debug) console.log('Receive Timeout : ' + filter);
          resolve();
        }, timeout);
      }
      this.listener[lid] = msg => {
        if (!this._matchMessage(filter, msg)) return false;
        if(timer) clearTimeout(timer);
        delete this.listener[lid];
        resolve(msg);
        return true;
      };
    });
  }

  /**
   * Closes the gateway. The gateway functionality may not longer be accessed after
   * this method is called.
   * @returns {void}
   */
  close() {
    this.connector.close();
    this._removeGWCache(this);
  }

}

/**
 * Services supported by fjage agents.
 */
const Services = {
  SHELL : 'org.arl.fjage.shell.Services.SHELL'
};

/**
 * Creates a unqualified message class based on a fully qualified name.
 * @param {string} name - fully qualified name of the message class to be created.
 * @param {function} [parent] - class of the parent MessageClass to inherit from.
 * @returns {function} constructor for the unqualified message class.
 */
function MessageClass(name, parent=Message) {
  let sname = name.replace(/^.*\./, '');
  if (MessageClass[sname]) return MessageClass[sname];
  let cls = class extends parent {
    constructor(params) {
      super();
      this.__clazz__ = name;
      if (params){
        const keys = Object.keys(params);
        for (let k of keys) {
          this[k] = params[k];
        }
      }
    }
  };
  cls.__clazz__ = name;
  MessageClass[sname] = cls;
  return cls;
}

////// private utilities

// generate random ID with length 4*len characters
/** @private */
function _guid(len) {
  function s4() {
    return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
  }
  let s = s4();
  for (var i = 0; i < len-1; i++)
    s += s4();
  return s;
}
  
// convert from base 64 to array
/** @private */
function _b64toArray(base64, dtype, littleEndian=true) {
  let s = gObj.atob(base64);
  let len = s.length;
  let bytes = new Uint8Array(len);
  for (var i = 0; i < len; i++)
    bytes[i] = s.charCodeAt(i);
  let rv = [];
  let view = new DataView(bytes.buffer);
  switch (dtype) {
  case '[B': // byte array
    for (i = 0; i < len; i++)
      rv.push(view.getUint8(i));
    break;
  case '[S': // short array
    for (i = 0; i < len; i+=2)
      rv.push(view.getInt16(i, littleEndian));
    break;
  case '[I': // integer array
    for (i = 0; i < len; i+=4)
      rv.push(view.getInt32(i, littleEndian));
    break;
  case '[J': // long array
    for (i = 0; i < len; i+=8)
      rv.push(view.getInt64(i, littleEndian));
    break;
  case '[F': // float array
    for (i = 0; i < len; i+=4)
      rv.push(view.getFloat32(i, littleEndian));
    break;
  case '[D': // double array
    for (i = 0; i < len; i+=8)
      rv.push(view.getFloat64(i, littleEndian));
    break;
  default:
    return;
  }
  return rv;
}

// base 64 JSON decoder
/** @private */
function _decodeBase64(k, d) {
  if (d === null) {
    return null;
  }
  if (typeof d == 'object' && 'clazz' in d) {
    let clazz = d.clazz;
    if (clazz.startsWith('[') && clazz.length == 2 && 'data' in d) {
      let x = _b64toArray(d.data, d.clazz);
      if (x) d = x;
    }
  }
  return d;
}

////// global

const GATEWAY_DEFAULTS = {};
let gObj = {};
let DEFAULT_URL;
if (isBrowser || isWebWorker){
  gObj = window;
  Object.assign(GATEWAY_DEFAULTS, {
    'hostname': gObj.location.hostname,
    'port': gObj.location.port,
    'pathname' : '/ws/',
    'timeout': 1000,
    'keepAlive' : true,
    'queueSize': DEFAULT_QUEUE_SIZE
  });
  DEFAULT_URL = new URL('ws://localhost');
  // Enable caching of Gateways
  if (typeof gObj.fjage === 'undefined') gObj.fjage = {};
  if (typeof gObj.fjage.gateways == 'undefined')gObj.fjage.gateways = [];
} else if (isJsDom || isNode){
  gObj = global;
  Object.assign(GATEWAY_DEFAULTS, {
    'hostname': 'localhost',
    'port': '1100',
    'pathname': '',
    'timeout': 1000,
    'keepAlive' : true,
    'queueSize': DEFAULT_QUEUE_SIZE
  });
  DEFAULT_URL = new URL('tcp://localhost');
  gObj.atob = a => Buffer.from(a, 'base64').toString('binary');
}

const ParameterReq$1 = MessageClass('org.arl.fjage.param.ParameterReq');

const ParameterReq = MessageClass('org.arl.unet.ParameterReq');
const DatagramReq$1 = MessageClass('org.arl.unet.DatagramReq');
const DatagramNtf$1 = MessageClass('org.arl.unet.DatagramNtf');
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
            }
          });
        }else {
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
      }else {
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
        } else {
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
  'RxFrameNtf'             : MessageClass('org.arl.unet.phy.RxFrameNtf', DatagramNtf$1), 
  'RxFrameStartNtf'        : MessageClass('org.arl.unet.phy.RxFrameStartNtf'), 
  'SyncInfoReq'            : MessageClass('org.arl.unet.phy.SyncInfoReq'), 
  'SyncInfoRsp'            : MessageClass('org.arl.unet.phy.SyncInfoRsp'), 
  'TxFrameNtf'             : MessageClass('org.arl.unet.phy.TxFrameNtf'), 
  'TxFrameReq'             : MessageClass('org.arl.unet.phy.TxFrameReq', DatagramReq$1), 
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

const REQUEST_TIMEOUT = 1000;

const AddressResolutionReq = UnetMessages.AddressResolutionReq;
const DatagramReq = UnetMessages.DatagramReq;
const DatagramNtf = UnetMessages.DatagramNtf;
const RxFrameNtf = UnetMessages.RxFrameNtf;

class UnetSocket {

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

export { AgentID, Gateway, Message, MessageClass, Performative, Protocol, Services, UnetMessages, UnetSocket };