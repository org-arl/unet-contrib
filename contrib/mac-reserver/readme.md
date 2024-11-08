# MAC Rerserver

The MAC reserver agent is an agent that constantly asks for a reservation of the medium using the [medium access control (MAC) Service](https://unetstack.net/handbook/unet-handbook.html#_medium_access_control) in UnetStack.

This agent is designed to be used to stop UnetStack from transmitting while some other device is using the acoustic communication channel, but when and how often the device would use the channel is not known in advance. So this agent reverses the logic and always keeps the medium reserved for the device. Once the user knows that the other device is done using the channel, the user can send a `CancelReq` message to the MAC reserver agent to allow UnetStack's [LINK service](https://unetstack.net/handbook/unet-handbook.html#_single_hop_links) to start using the channel again.

Optionally, the `CancelReq` message can also include a `cancelDuration` parameter to specify the duration of time (in ms) after which the MAC reserver agent automatically reserves the medium again for the device. If  this parameter is not provided, the user has to manually send a `ReserveReq` message to reserve the medium again.

## Usage

The agent can be added to a running UnetStack instance by copying the `MacReserver.groovy` file to the `classes` directory of the UnetStack installation, and then adding the agent to the stack using the `container.add` command.

For example the following command adds a `MacReserver` agent to the stack and uses it to reserve the medium for the device:

```groovy
container.add 'res', new MacReserver()

> res
« MacReserver »

[MacReserver.Params]
  reserved ⤇ false

res << new CancelReq()  // to cancel the reservation
//...
res << ReserveReq()  // to reserve the medium for
//...
res << new CancelReq(cancelDuration: 10000)  // to cancel the reservation and re-reserve the medium after 10 seconds

```

## Notes
- The `MacReserver` agent exposes the current state of the reservation using the `reserved` parameter. This parameter is `true` when the medium is reserved and `false` when it is not.
- The `MacReserver` agent gets the longest MAC reservation time available in the stack (usually 60 seconds) and reserves the medium for that duration. When this duration is over, the agent reserves the medium again for the same duration. In between the two reservations, there is a small window at which point UnetStack's LINK service might try to transmit a frame. While this is a known edgecase, it's unlikely that this will happen in practice, and if UnetStack isn't able to transmit a frame for more than 60 seconds, the user should consider using a different approach to synchronize the transmissions.

