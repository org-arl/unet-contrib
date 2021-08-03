/* global it expect describe */

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

