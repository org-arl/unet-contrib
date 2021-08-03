JavaScript Helper Library for UnetStack
==========================

The JavaScript Helper Library for UnetStack is a JavaScript library that enables controlling of a UnetStack Node from JavaScript, both Browser-based (over WebSockets) and Node.JS (TCP).

The library contains helper methods, commonly used [Messages](https://fjage.readthedocs.io/en/latest/messages.html) and [Services](https://fjage.readthedocs.io/en/latest/services.html) in Unet, and an implementation of the [UnetSocket API](https://unetstack.net/handbook/unet-handbook_unetsocket_api.html), which is a high-level [Socket-like](https://en.wikipedia.org/wiki/Network_socket) API for communicating over an Unet.


## Installation

```sh
$ npm install unetjs
```

## Documentation

The API documentation of the latest version of unet.js is published at https://github.com/org-arl/unet-contrib/tree/master/unetsocket/js/docs


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

The UnetSocket API is a high-level API exposed by UnetStack to allow users to communicate over an Unet. It is a socket-style API, which allows a user to send [Datagrams]() to specific nodes, or as a broadcast, and similarly listen to Datagrams from specific nodes, etc. A detailed explanation of UnetSocket API can be found in the [Unet Handbook](https://unetstack.net/handbook/unet-handbook_unetsocket_api.html)

The JavaScript version of the UnetSocket API allows a user to connect to a node in an Unet from a browser/Node.JS-based application and communicate with other nodes in the Unet. The Datagrams received on those nodes could be consumed by other instances of the UnetSocket, either directly on the node, or on a remote [Gateway](https://fjage.readthedocs.io/en/latest/remote.html#interacting-with-agents-using-a-gateway) connected to that node.

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