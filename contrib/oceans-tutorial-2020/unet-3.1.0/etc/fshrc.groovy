import org.arl.unet.Services

def load(url, cond=null) {
  try {
    if (cond != null) {
      if (cond instanceof Services && !agentForService(cond)) return
      if (cond instanceof Closure && !cond()) return
    }
    run(url)
  } catch (FileNotFoundException ex) {
    log.warning "$url: file not found, skipping it!"
  } catch (ClassNotFoundException ex) {
    log.warning "$url: class not found, skipping it!"
  } catch (ex) {
    log.log(WARNING, "$url: loading failed", ex)
  }
}

home = System.getProperty('unet.home')?:'.'
home = new File(home).getCanonicalFile()
scripts = new File(home, 'scripts')

load 'cls://org.arl.unet.UnetShellExt'
load 'cls://org.arl.unet.state.StateManagerShellExt',   Services.STATE_MANAGER
load 'cls://org.arl.unet.nodeinfo.NodeInfoShellExt',    Services.NODE_INFO
load 'cls://org.arl.unet.addr.ArpShellExt',             Services.ADDRESS_RESOLUTION
load 'cls://org.arl.unet.phy.PhysicalShellExt',         Services.PHYSICAL
load 'cls://org.arl.unet.localization.RangingShellExt', Services.RANGING
load 'cls://org.arl.unet.bb.BasebandShellExt',          Services.BASEBAND
load 'cls://org.arl.unet.mac.MacShellExt',              Services.MAC
load 'cls://org.arl.unet.net.RouterShellExt',           Services.ROUTING
load 'cls://org.arl.unet.transport.TransportShellExt',  Services.TRANSPORT
load 'cls://org.arl.unet.remote.RemoteShellExt',        Services.REMOTE
load 'cls://org.arl.unet.remote.RemoteControlShellExt', Services.REMOTE
load 'cls://org.arl.unet.scheduler.SchedulerShellExt',  Services.SCHEDULER

load 'cls://org.arl.unet.bb.BasebandSignalMonitorShellExt', { agent('bbmon').type }
load 'cls://org.arl.unet.link.ECLinkShellExt',              { agent('uwlink').type == 'org.arl.unet.link.ECLink' }
load 'cls://org.arl.unet.link.ReliableLinkShellExt',        { agent('uwlink').type == 'org.arl.unet.link.ReliableLink' }

load 'cls://org.arl.unet.mac.CSMAShellExt',                 { agentForService(Services.MAC)?.type == 'org.arl.unet.mac.CSMA' }
load 'cls://org.arl.unet.transport.SWTransportShellExt',    { agentForService(Services.TRANSPORT)?.type == 'org.arl.unet.transport.SWTransport' }

def fshrcScript = new File(home, 'scripts/fshrc.groovy')
if (fshrcScript.exists()) {
  run fshrcScript
  return
}
