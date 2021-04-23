# VizTrace
**Trace visualization tool for UnetStack JSON trace files**

UnetStack 3.3 introduced the event logging framework and JSON trace file format that contains detailed information for automated analysis of network traces. To illustrate the power of the event logging framework, we have built a simple viztrace tool to automatically draw sequence diagrams from the JSON trace files. The tool is written in Julia, and will require a working installation of [Julia](https://julialang.org/downloads/) on your machine to run.

To illustrate the power of the tool, let us simulate a simple [2-node network](https://unetstack.net/handbook/unet-handbook_getting_started.html) and make a range measurment from node A to B. On node A:

```
> range host('B')
999.99976
```

If you look in the `logs` folder in the simulator, you'll find a `trace.nam` file. We can analyze it using the `viztrace` tool:

```sh
$ julia --project viztrace.jl trace.json
Specify a trace:
1: 1617881734525 [B] AddressAllocReq ⟦ node → arp ⟧ (1 events)
2: 1617881734525 [A] AddressAllocReq ⟦ node → arp ⟧ (1 events)
3: 1617881734531 [A] AddressResolutionReq ⟦ websh → arp ⟧ (1 events)
4: 1617881734595 [A] RangeReq ⟦ websh → ranging ⟧ (23 events)
```

So the tool tells us that there are 4 event traces in the `trace.json` file. The first 2 traces are related to address allocations on nodes B and A. The third trace is an address resolution for node B, when we called `host('B')`. The final trace is the actual ranging event, consisting of 23 individual sub-events. Let's explore that in more detail:

```sh
$ julia --project viztrace.jl -t 4 trace.json > event4.mmd
```

This generates a [mermaid](https://mermaid-js.github.io/) sequence diagram for all the events in trace 4:

```
sequenceDiagram
  participant websh_A as websh/A
  participant ranging_A as ranging/A
  participant mac_A as mac/A
  participant phy_A as phy/A
  participant phy_B as phy/B
  participant ranging_B as ranging/B
  websh_A->>ranging_A: RangeReq
  ranging_A-->>websh_A: AGREE
  ranging_A->>mac_A: ReservationReq
  mac_A->>ranging_A: ReservationRsp
  mac_A->>ranging_A: ReservationStatusNtf
  ranging_A->>phy_A: ClearReq
  phy_A-->>ranging_A: AGREE
  ranging_A->>phy_A: TxFrameReq
  phy_A-->>ranging_A: AGREE
  phy_A->>ranging_A: TxFrameNtf
  phy_A->>phy_B: HalfDuplexModem$TX
  phy_B->>ranging_B: RxFrameNtf
  ranging_B->>phy_B: ClearReq
  phy_B-->>ranging_B: AGREE
  ranging_B->>phy_B: TxFrameReq
  phy_B-->>ranging_B: AGREE
  phy_B->>ranging_B: TxFrameNtf
  phy_B->>phy_A: HalfDuplexModem$TX
  phy_A->>ranging_A: RxFrameNtf
  ranging_A->>websh_A: RangeNtf
  ranging_A->>mac_A: ReservationCancelReq
  mac_A-->>ranging_A: AGREE
  mac_A->>ranging_A: ReservationStatusNtf
```

We can easily convert this to a nice sequence diagram using the [mermaid command-line interface](https://github.com/mermaid-js/mermaid-cli) or the [mermaid online live editor](https://mermaid-js.github.io/mermaid-live-editor):

![](https://mermaid.ink/img/eyJjb2RlIjoic2VxdWVuY2VEaWFncmFtXG4gIHBhcnRpY2lwYW50IHdlYnNoX0EgYXMgd2Vic2gvQVxuICBwYXJ0aWNpcGFudCByYW5naW5nX0EgYXMgcmFuZ2luZy9BXG4gIHBhcnRpY2lwYW50IG1hY19BIGFzIG1hYy9BXG4gIHBhcnRpY2lwYW50IHBoeV9BIGFzIHBoeS9BXG4gIHBhcnRpY2lwYW50IHBoeV9CIGFzIHBoeS9CXG4gIHBhcnRpY2lwYW50IHJhbmdpbmdfQiBhcyByYW5naW5nL0JcbiAgd2Vic2hfQS0-PnJhbmdpbmdfQTogUmFuZ2VSZXFcbiAgcmFuZ2luZ19BLS0-PndlYnNoX0E6IEFHUkVFXG4gIHJhbmdpbmdfQS0-Pm1hY19BOiBSZXNlcnZhdGlvblJlcVxuICBtYWNfQS0-PnJhbmdpbmdfQTogUmVzZXJ2YXRpb25Sc3BcbiAgbWFjX0EtPj5yYW5naW5nX0E6IFJlc2VydmF0aW9uU3RhdHVzTnRmXG4gIHJhbmdpbmdfQS0-PnBoeV9BOiBDbGVhclJlcVxuICBwaHlfQS0tPj5yYW5naW5nX0E6IEFHUkVFXG4gIHJhbmdpbmdfQS0-PnBoeV9BOiBUeEZyYW1lUmVxXG4gIHBoeV9BLS0-PnJhbmdpbmdfQTogQUdSRUVcbiAgcGh5X0EtPj5yYW5naW5nX0E6IFR4RnJhbWVOdGZcbiAgcGh5X0EtPj5waHlfQjogSGFsZkR1cGxleE1vZGVtJFRYXG4gIHBoeV9CLT4-cmFuZ2luZ19COiBSeEZyYW1lTnRmXG4gIHJhbmdpbmdfQi0-PnBoeV9COiBDbGVhclJlcVxuICBwaHlfQi0tPj5yYW5naW5nX0I6IEFHUkVFXG4gIHJhbmdpbmdfQi0-PnBoeV9COiBUeEZyYW1lUmVxXG4gIHBoeV9CLS0-PnJhbmdpbmdfQjogQUdSRUVcbiAgcGh5X0ItPj5yYW5naW5nX0I6IFR4RnJhbWVOdGZcbiAgcGh5X0ItPj5waHlfQTogSGFsZkR1cGxleE1vZGVtJFRYXG4gIHBoeV9BLT4-cmFuZ2luZ19BOiBSeEZyYW1lTnRmXG4gIHJhbmdpbmdfQS0-PndlYnNoX0E6IFJhbmdlTnRmXG4gIHJhbmdpbmdfQS0-Pm1hY19BOiBSZXNlcnZhdGlvbkNhbmNlbFJlcVxuICBtYWNfQS0tPj5yYW5naW5nX0E6IEFHUkVFXG4gIG1hY19BLT4-cmFuZ2luZ19BOiBSZXNlcnZhdGlvblN0YXR1c050ZlxuIiwibWVybWFpZCI6e30sInVwZGF0ZUVkaXRvciI6ZmFsc2V9)
