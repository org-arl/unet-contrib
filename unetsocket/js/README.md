# JavaScript Helper Library for UnetStack

The JavaScript Helper Library for UnetStack is a JavaScript library that enables controlling of a UnetStack Node from JavaScript, both Browser-based (over WebSockets) and Node.JS (TCP).

The library contains helper methods, commonly used [Messages](https://fjage.readthedocs.io/en/latest/messages.html) and [Services](https://fjage.readthedocs.io/en/latest/services.html) in Unet, and an implementation of the [UnetSocket API](https://unetstack.net/handbook/unet-handbook_unetsocket_api.html), which is a high-level [Socket-like](https://en.wikipedia.org/wiki/Network_socket) API for communicating over an Unet.

## Installation

```sh
$ npm install unetjs
...
```

## Documentation

The API documentation of the latest version of unet.js is published at [https://github.com/org-arl/unet-contrib/tree/master/unetsocket/js/docs](https://github.com/org-arl/unet-contrib/tree/master/unetsocket/js/docs)

## Usage

### Pre-defined Messages and Services

This library provides 2 major components, the pre-defined `Message` and `Service` dictionaries, and the `UnetSocket` implementation.

The pre-defined `Message` and `Service` dictionaries are a convenient way to define commonly used Messages and Services in your code.

```js
// Instead of writing this,

import {MessageClass, Gateway} from 'unetjs'
let gw = new Gateway({...});
const DatagramReq = MessageClass('org.arl.unet.DatagramReq');
let req = new DatagramReq();
...
req.recipient = await gw.agentForService('org.arl.unet.Services.DATAGRAM')
let rsp = await gw.send(req);
...

// you can write this
import {UnetMessages, Gateway} from 'unetjs'
let gw = new Gateway({...});
let req = new UnetMessages.DatagramReq();
...
req.recipient = await gw.agentForService(Services.DATAGRAM)
let rsp = await gw.send(req);
```

The pre-defined `Message` also follows the class hierarchy that is implemented in UnetStack, so syntax like `instanceof` will yield the same results as in UnetStack.

```js
// RxFrameNtf is a subclass of DatagramNtf
let rxNtf = new UnetMessages.RxFrameNtf();

rxNtf instanceof UnetMessages.DatagramReq; // returns true;
```

### UnetSocket

The UnetSocket API is a high-level API exposed by UnetStack to allow users to communicate over an Unet. It is a socket-style API, which allows a user to send [Datagrams](https://unetstack.net/handbook/unet-handbook_datagram_service.html) to specific nodes, or as a broadcast, and similarly listen to Datagrams from specific nodes, etc. A detailed explanation of UnetSocket API can be found in the [Unet Handbook](https://unetstack.net/handbook/unet-handbook_unetsocket_api.html)

The JavaScript version of the UnetSocket API allows a user to connect to a node in an Unet from a browser/Node.JS-based application and communicate with other nodes in the Unet. The Datagrams received on those nodes could be consumed by other instances of the UnetSocket, either directly on the node, or on a remote [Gateway](https://fjage.readthedocs.io/en/latest/remote.html#interacting-with-agents-using-a-gateway) connected to that node.

### Caching Parameter Responses

The UnetSocket API allows a user to cache responses to parameter requests. This is useful in a scenario where the parameters aren't changing very often, and the user wants to reduce the round trip time for parameter requests.

> This behaviour used to be enabled by default in unetsocket.js v2.0.0 till v2.0.10. From v3.0.0 onwards, this behaviour is **disabled** by default but a available as an option.

You can use the `CachingGateway` class instead of the `Gateway` class to enable this behaviour.

UnetSocket API acheives this using two mechanism, firstly, it can request ALL of the parameters from an Agent instead of just one, and then cache the responses. This is called the `greedy` mode. The greedy mode is enabled by default but can be disabled by setting the `greedy` property to `false` in the `CachingAgentID` constructor.

Secondly, the UnetSocket API caches the responses to parameter requests for a limited time. If the user requests the same parameter again, the UnetSocket will return the cached response. The time to cache a response is set by the `cacheTime` property in the `CachingAgentID` constructor.

```js
import {UnetMessages, CachingGateway} from 'unetjs'
let gw = new CachingGateway({...});
let nodeinfo = await gw.agentForService(Services.NODE_INFO); // returns a CachingAgentID
let cLoc = nodeinfo.get('location'); // this will request all the Parameters from the Agent, and cache the responses.
...
cLoc = nodeinfo.get('location', maxage=5000); // this will return the cached response if it was called within 5000ms of the original request.
...
cLoc = nodeinfo.get('location', maxage=0); // this will force the Gateway to request the parameter again.
```

```js
import {UnetMessages, CachingGateway} from 'unetjs'
let gw = new CachingGateway({...});
let nodeinfo = await gw.agentForService(Services.NODE_INFO); // returns a CachingAgentID
let nonCachingNodeInfo = await gw.agentForService(Services.NODE_INFO, false); // returns an AgentID without caching (original fjage.js functionality).
let cLoc = nonCachingNodeInfo.get('location'); // this will request the `location` parameter from the Agent.
...
cLoc = nonCachingNodeInfo.get('location'); // this will request the `location` parameter from the Agent again.

let nonGreedyNodeInfo = await gw.agentForService(Services.NODE_INFO, true, false); // returns an CachingAgentID that's not greedy.
let cLoc = nonGreedyNodeInfo.get('location'); // this will request the `location` parameter from the Agent.
...
cLoc = nonCachingNodeInfo.get('location'); // this will request the `location` parameter from the cache.
...
let cLoc = nonGreedyNodeInfo.get('heading'); // this will request the `heading` parameter from the Agent.
```

### Importing/Modules

A distribution-ready bundle is available for types of module systems commonly used in the JS world. Examples of how to use it for the different module systems are available in the [examples](/examples) directory.

At runtime, fjage.js (the underlying library used to connect to an Unet node) will check its context (browser or Node.js) and accordingly use the appropriate `Connector` (WebSocket or TCP) for connecting to the Unet node.

Here are some code snippets of how you can start using unet.js in the various module systems.

### [CommonJS](dist/cjs)

```js
const { Performative, AgentID, Message, Gateway, MessageClass } = require('unetjs');
const shell = new AgentID('shell');
const gw = new Gateway({
    hostname: 'localhost',
    port : '5081',
});
```

### [ECMAScript modules](dist/esm)

```js
import { Performative, AgentID, Message, Gateway, MessageClass } from 'unetjs'
const shell = new AgentID('shell');
const gw = new Gateway({
    hostname: 'localhost',
    port : '5081',
});
```

### [UMD](dist)

```js
<script src="unet.min.js"></script>
<script>
    const shell = new fjage.AgentID('shell');
    const gw = new fjage.Gateway({
        hostname: 'localhost',
        port : '8080',
        pathname: '/ws/'
    });
</script>
```
