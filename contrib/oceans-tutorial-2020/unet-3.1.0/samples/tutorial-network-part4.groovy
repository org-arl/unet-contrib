//! Simulation

import org.arl.fjage.RealTimePlatform
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*

///////////////////////////////////////////////////////////////////////////////
// display documentation

println '''
Welcome to Oceans 20!
Starting the tutorial network.
You can access Node 1 and Node 4 using their WebUI
--------------

Node 1: http://localhost:8081/shell.html
Node 2:
Node 3:
Node 4: http://localhost:8084/shell.html

'''

///////////////////////////////////////////////////////////////////////////////
// simulation settings

platform = RealTimePlatform           // use real-time mode

///////////////////////////////////////////////////////////////////////////////
// channel settings

channel = [model: ProtocolChannelModel]

///////////////////////////////////////////////////////////////////////////////
// simulation details

simulate {
  node '1', address: 1, location: [0.km, 0.km,  -5.m], web: 8081, stack: "$home/scripts/setup.groovy"

  n2 = node '2', address: 2, location: [1.km, 0.km,  -30.m], stack: "$home/scripts/custom.groovy", shell: true

  node '3', address: 3, location: [1.5.km, 0.5.km,  -20.m], stack: "$home/scripts/custom.groovy"

  node '4', address: 4, location: [3.km, 0.km,  -5.m], web: 8084, stack: "$home/scripts/setup.groovy"

  // node '5', address: 5, location: [4.5.km, 0.km,  -30.m], stack: "$home/scripts/custom.groovy", shell: true

  // Extra configuration at startup of node 2
  n2.startup = {

    /****** For Demo 4.1 ******/
    // Add a Simulated Sensor Agent,
    // which generates 4 bytes of data
    // and sends it over a Socket every 10 seconds.
    container.add 'sensor', new Sensor()


    /****** For Demo 4.2 ******/
    // Portal to Node 4 on UDP Port 5000
    p = new org.arl.unet.portal.UdpPortal(port:5000, peer:4)
    container.add 'portal', p;
    // Use router for multi-hop routing
    p.dsp = 'router'

    // Add a static route to Node 4 via Node 3
    r = org.arl.unet.net.EditRouteReq.newRoute()
    r.to = 4
    r.nextHop = 3
    agent('router') << r;
  }
}
