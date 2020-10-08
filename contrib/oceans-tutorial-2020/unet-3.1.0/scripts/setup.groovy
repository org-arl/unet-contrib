import org.arl.fjage.Agent

boolean loadAgentByClass(String name, String clazz) {
  try {
    container.add name, Class.forName(clazz).newInstance()
    return true
  } catch (Exception ex) {
    return false
  }
}

boolean loadAgentByClass(String name, String... clazzes) {
  for (String clazz: clazzes) {
    if (loadAgentByClass(name, clazz)) return true
  }
  return false
}

loadAgentByClass 'arp',          'org.arl.unet.addr.AddressResolution'
loadAgentByClass 'ranging',      'org.arl.unet.localization.Ranging'
loadAgentByClass 'mac',          'org.arl.unet.mac.CSMA'
loadAgentByClass 'uwlink',       'org.arl.unet.link.ECLink', 'org.arl.unet.link.ReliableLink'
loadAgentByClass 'transport',    'org.arl.unet.transport.SWTransport'
loadAgentByClass 'router',       'org.arl.unet.net.Router'
loadAgentByClass 'rdp',          'org.arl.unet.net.RouteDiscoveryProtocol'
loadAgentByClass 'statemanager', 'org.arl.unet.state.StateManager'

container.add 'remote', new org.arl.unet.remote.RemoteControl(cwd: new File(home, 'scripts'), enable: false)
container.add 'bbmon',  new org.arl.unet.bb.BasebandSignalMonitor(new File(home, 'logs/signals-0.txt').path, 64)
