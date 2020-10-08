import org.arl.fjage.RealTimePlatform

///////////////////////////////////////////////////////////////////////////////
// display documentation

println '''
Netiquette 3-node network
-------------------------

Node A: tcp://localhost:1101, http://localhost:8081/
Node B: tcp://localhost:1102, http://localhost:8082/
Node C: tcp://localhost:1103, http://localhost:8083/
'''

///////////////////////////////////////////////////////////////////////////////
// simulator configuration

platform = RealTimePlatform   // use real-time mode
origin = [1.216, 103.851]

simulate {
  node 'A', location: [121.m,  137.m, -10.m], web: 8081, api: 1101, stack: "$home/etc/setup"
  node 'B', location: [160.m, -232.m, -15.m], web: 8082, api: 1102, stack: "$home/etc/setup"
  node 'C', location: [651.m,  140.m,  -5.m], web: 8083, api: 1103, stack: "$home/etc/setup"
}
