/* global it expect spyOn describe AgentID CachingGateway CachingAgentID UnetMessages Gateway Services beforeAll isBrowser isJsDom isNode UnetSocket Protocol toGps toLocal beforeEach jasmine*/

const DatagramNtf = UnetMessages.DatagramNtf;

let gwOpts = [];
if (isBrowser){
  gwOpts = [{
    hostname: 'localhost',
    port : '8081',
    pathname: '/ws/'
  }, {
    hostname: 'localhost',
    port : '8082',
    pathname: '/ws/'
  }];
} else if (isJsDom || isNode){
  gwOpts = [{
    hostname: 'localhost',
    port : '1101',
    pathname: ''
  }, {
    hostname: 'localhost',
    port : '1102',
    pathname: ''
  }, ];
}

function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

describe('A UnetSocket', function () {
  it('should be able to be constructed', async function () {
    let usock = await new UnetSocket(gwOpts[0].hostname, gwOpts[0].port);
    expect(usock).toBeInstanceOf(UnetSocket);
    usock.close();
  });

  it('should be able to get local address', async function () {
    let usock = await new UnetSocket(gwOpts[0].hostname, gwOpts[0].port);
    let localAddr = await usock.getLocalAddress();
    expect(localAddr).toBe(232);
    usock.close();
  });

  it('should be able get correct IDs for host names', async function(){
    let usock = await new UnetSocket(gwOpts[0].hostname, gwOpts[0].port);
    let hostA = await usock.host('A');
    expect(hostA).toBe(232);
    let hostB = await usock.host('B');
    expect(hostB).toBe(31);
    usock.close();
  });

  it('should be able to get access to Agents for given Service', async function(){
    let usock = await new UnetSocket(gwOpts[0].hostname, gwOpts[0].port);
    let shell = await usock.agentForService(Services.SHELL);
    expect(shell).toBeInstanceOf(AgentID);
    usock.close();
  });

  it('should be able to get access to Agents for given name', async function(){
    let usock = await new UnetSocket(gwOpts[0].hostname, gwOpts[0].port);
    let node = usock.agent('node');
    expect(node).toBeInstanceOf(AgentID);
    usock.close();
  });

  it('should close only when closed', async function(){
    let usock = await new UnetSocket(gwOpts[0].hostname, gwOpts[0].port);
    expect(usock.isClosed()).toBeFalse();
    usock.close();
    expect(usock.isClosed()).toBeTrue();
  });

  it('should give access the underlying Gateway', async function(){
    let usock = await new UnetSocket(gwOpts[0].hostname, gwOpts[0].port);
    let gw = usock.getGateway();
    expect(gw).toBeInstanceOf(Gateway);
    usock.close();
  });

  it('should be able to get parameters on Agents', async function(){
    let usock = await new UnetSocket(gwOpts[0].hostname, gwOpts[0].port);
    let node = usock.agent('node');
    expect(node).toBeInstanceOf(AgentID);
    expect(await node.get('address')).toBe(232);
    expect(await node.get('nodeName')).toBe('A');
    let phy = await usock.agentForService(Services.PHYSICAL);
    expect(phy).toBeInstanceOf(AgentID);
    expect(await phy.get('name')).toBe('phy');
    expect(await phy.get('MTU')).toBeGreaterThan(0);
    usock.close();
  });

  it('should be able to bind and unbind properly', async function(){
    let usock = await new UnetSocket(gwOpts[0].hostname, gwOpts[0].port);
    expect(usock.getLocalProtocol()).toBe(-1);
    expect(usock.isBound()).toBeFalse();
    usock.bind(42);
    expect(usock.isBound()).toBeTrue();
    expect(usock.getLocalProtocol()).toBe(42);
    usock.unbind();
    expect(usock.isBound()).toBeFalse();
    expect(usock.getLocalProtocol()).toBe(-1);
    expect(usock.getRemoteAddress()).toBe(-1);
    expect(usock.getRemoteProtocol()).toBe(0);
    expect(usock.isConnected()).toBeFalse();
    usock.connect(31, 0);
    expect(usock.getRemoteAddress()).toBe(31);
    expect(usock.getRemoteProtocol()).toBe(0);
    expect(usock.isConnected()).toBeTrue();
    usock.disconnect();
    expect(usock.isConnected()).toBeFalse();
    expect(usock.getRemoteAddress()).toBe(-1);
    expect(usock.getRemoteProtocol()).toBe(0);
    usock.close();
  });


  it('should honour timeouts', async function(){
    let usock = await new UnetSocket(gwOpts[0].hostname, gwOpts[0].port);
    usock.bind(0);
    expect(usock.getTimeout()).toBe(0);
    usock.setTimeout(1000);
    expect(usock.getTimeout()).toBe(1000);
    let t1 = Date.now();
    expect(await usock.receive()).toBeUndefined();
    let dt = Date.now() - t1;
    expect(dt).toBeGreaterThanOrEqual(1000);
    usock.setTimeout(0);
    expect(usock.getTimeout()).toBe(0);
    t1 = Date.now();
    expect(await usock.receive()).toBeUndefined();
    dt = Date.now() - t1;
    expect(dt).toBeLessThanOrEqual(500);
    usock.close();
  });

  it('should be only able to communicate bound to', async function(){
    let usock1 = await new UnetSocket(gwOpts[0].hostname, gwOpts[0].port);
    let usock2 = await new UnetSocket(gwOpts[1].hostname, gwOpts[1].port);
    expect(usock2.bind(Protocol.USER)).toBeTrue();
    usock2.setTimeout(1000);

    expect(await usock1.send([1,2,3])).toBeFalse();
    expect(await usock1.send([4,5,6], 31)).toBeTrue();
    expect(await usock2.receive()).toBeUndefined();
    expect(await usock1.send([7,8,9], 31, Protocol.USER)).toBeTrue();
    let ntf = await usock2.receive();
    expect(ntf).toBeInstanceOf(DatagramNtf);
    expect(ntf.data).toEqual([7,8,9]);

    usock1.close();
    usock2.close();
  });

  it('should be only able to communicate on the protocol connected to', async function(){
    let usock1 = await new UnetSocket(gwOpts[0].hostname, gwOpts[0].port);
    let usock2 = await new UnetSocket(gwOpts[1].hostname, gwOpts[1].port);
    expect(usock2.bind(Protocol.USER)).toBeTrue();
    usock2.setTimeout(1000);

    usock1.connect(31, Protocol.USER);
    expect(await usock1.send([1,2,3])).toBeTrue();
    let ntf = await usock2.receive();
    expect(ntf).toBeInstanceOf(DatagramNtf);
    expect(ntf.data).toEqual([1,2,3]);
    expect(await usock1.send([1,2,3], 31, 0)).toBeTrue();
    expect(await usock2.receive()).toBeUndefined();
    expect(await usock1.send([4,5,6], 27, Protocol.USER)).toBeTrue();
    expect(await usock2.receive()).toBeUndefined();
    expect(await usock1.send([7,8,9])).toBeTrue();
    ntf = await usock2.receive();
    expect(ntf).toBeInstanceOf(DatagramNtf);
    expect(ntf.data).toEqual([7,8,9]);

    usock1.close();
    usock2.close();
  });

  it('should be able to communicate - 3', async function(){
    let usock1 = await new UnetSocket(gwOpts[0].hostname, gwOpts[0].port);
    let usock2 = await new UnetSocket(gwOpts[1].hostname, gwOpts[1].port);
    expect(usock2.bind(Protocol.USER)).toBeTrue();
    usock2.setTimeout(1000);
    usock1.connect(31, Protocol.USER);

    usock1.disconnect();
    expect(await usock1.send([1,2,3])).toBeFalse();
    expect(await usock1.send([4,5,6], 31, Protocol.USER)).toBeTrue();
    let ntf = await usock2.receive();
    expect(ntf).toBeInstanceOf(DatagramNtf);
    expect(ntf.data).toEqual([4,5,6]);

    usock1.close();
    usock2.close();
  });

});

describe('Unet Utils', function () {
  function coordTester(res, val) {
    const tolerance = 0.000100;
    const min = res - tolerance;
    const max = parseFloat(res) + tolerance;
    return (val >= min && val <=max);
  }

  beforeEach(function() {
    jasmine.addCustomEqualityTester(coordTester);
  });

  it('should be able to convert local coordinates to GPS coordinates', function () {
    const origin=[1.34286,103.84109];
    let x=100, y=100;
    let loc = toGps(origin,x,y);
    expect(loc.length).toBe(2);
    expect(loc[0]).toBeInstanceOf(Number);
    expect(loc[1]).toBeInstanceOf(Number);
    expect(loc[0]).toEqual(1.343764);
    expect(loc[1]).toEqual(103.841988);

    x=0, y=14.5;
    loc = toGps(origin,x,y);
    expect(loc.length).toBe(2);
    expect(loc[0]).toBeInstanceOf(Number);
    expect(loc[1]).toBeInstanceOf(Number);
    expect(loc[0]).toEqual(1.342991);
    expect(loc[1]).toEqual(103.84109);
  });

  it('should be able to convert GPS coordinates to local coordinates', function () {
    const origin=[1.34286,103.84109];
    let lat=1.343764, lon=103.841988;
    let loc = toLocal(origin,lat,lon);
    expect(loc.length).toBe(2);
    expect(loc[0]).toBeInstanceOf(Number);
    expect(loc[1]).toBeInstanceOf(Number);
    expect(loc[0]).toEqual(99.937602);
    expect(loc[1]).toEqual(99.959693);

    lat=1.342991, lon=103.84109;
    loc = toLocal(origin,lat,lon);
    expect(loc.length).toBe(2);
    expect(loc[0]).toBeInstanceOf(Number);
    expect(loc[1]).toBeInstanceOf(Number);
    expect(loc[0]).toEqual(0);
    expect(loc[1]).toEqual(14.485309);
  });

});

describe('A CachingAgentID', function () {
  var gw;
  beforeAll(() => {
    gw = new CachingGateway(gwOpts[0]);
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;
  });

  it('should be creatable from an AgentID', async function () {
    const caid = new CachingAgentID('agent-name', true, gw);
    const caid1 = new CachingAgentID(new AgentID('agent-name', true, gw));
    expect(caid.getName()).toBe('agent-name');
    expect(caid.isTopic()).toBe(true);
    expect(caid.toJSON()).toBe('#agent-name');
    expect(caid1.getName()).toBe('agent-name');
    expect(caid1.isTopic()).toBe(true);
    expect(caid1.toJSON()).toBe('#agent-name');
  });

  it('should only return cached values for parameters if within maxage', async function () {
    let phy = await gw.agentForService(Services.PHYSICAL);
    expect(phy).toBeInstanceOf(CachingAgentID);
    spyOn(gw.connector.sock, 'send').and.callThrough();
    let clockOffset = await phy.get('clockOffset');
    let maxFrameLength = await phy.get('maxFrameLength',1);
    await phy.get(null); // get all
    gw.connector.sock.send.calls.reset();
    let clockOffset2 = await phy.get('clockOffset');
    let maxFrameLength2 = await phy.get('maxFrameLength',1);
    await phy.get('refPowerLevel');
    expect(gw.connector.sock.send).not.toHaveBeenCalled();
    expect(maxFrameLength).toBe(maxFrameLength2);
    expect(clockOffset).toBe(clockOffset2);
  });

  it('should not greedily get all parameters when asked for special parameters ', async function () {
    let phy = await gw.agentForService(Services.PHYSICAL);
    expect(phy).toBeInstanceOf(CachingAgentID);
    spyOn(gw.connector.sock, 'send').and.callThrough();
    gw.connector.sock.send.calls.reset();
    await phy.get('name');
    await phy.get('clockOffset');
    expect(gw.connector.sock.send).toHaveBeenCalledTimes(2);
  });

  it('should not greedily get all parameters when asked for special parameters as a part of a list', async function () {
    let phy = await gw.agentForService(Services.PHYSICAL);
    expect(phy).toBeInstanceOf(CachingAgentID);
    spyOn(gw.connector.sock, 'send').and.callThrough();
    gw.connector.sock.send.calls.reset();
    await phy.get(['name', 'refPowerLevel']);
    await phy.get('clockOffset');
    expect(gw.connector.sock.send).toHaveBeenCalledTimes(2);
  });

  it('should return new values for parameters if beyond maxage', async function () {
    let phy = await gw.agentForService(Services.PHYSICAL);
    expect(phy).toBeInstanceOf(CachingAgentID);
    spyOn(gw.connector.sock, 'send').and.callThrough();
    let clockOffset = await phy.get('clockOffset');
    let maxFrameLength = await phy.get('maxFrameLength',1);
    await phy.get(null); // get all
    gw.connector.sock.send.calls.reset();
    await delay(6000);
    await phy.get('refPowerLevel');
    let clockOffset2 = await phy.get('clockOffset');
    let maxFrameLength2 = await phy.get('maxFrameLength',1);
    expect(gw.connector.sock.send).toHaveBeenCalledTimes(2);
    expect(maxFrameLength).toBe(maxFrameLength2);
    expect(clockOffset).toBe(clockOffset2);
  });


  it('should not fetch all parameters if not configured to be greedy', async function () {
    let phy = await gw.agentForService(Services.PHYSICAL);
    expect(phy).toBeInstanceOf(CachingAgentID);
    let nongreedyphy = new CachingAgentID(phy, null, null, false);
    spyOn(gw.connector.sock, 'send').and.callThrough();
    gw.connector.sock.send.calls.reset();
    await nongreedyphy.get('clockOffset');
    expect(gw.connector.sock.send).toHaveBeenCalledTimes(1);
    gw.connector.sock.send.calls.reset();
    await nongreedyphy.get('clockOffset');
    expect(gw.connector.sock.send).toHaveBeenCalledTimes(0);
    gw.connector.sock.send.calls.reset();
    await nongreedyphy.get('MTU');
    expect(gw.connector.sock.send).toHaveBeenCalledTimes(1);
    gw.connector.sock.send.calls.reset();
    await nongreedyphy.get('maxFrameLength',1);
    expect(gw.connector.sock.send).toHaveBeenCalledTimes(1);
    gw.connector.sock.send.calls.reset();
    await nongreedyphy.get('maxFrameLength',1);
    expect(gw.connector.sock.send).toHaveBeenCalledTimes(0);
  });
});