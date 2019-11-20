// Enable web server to serve htmls from scripts folder

import org.arl.fjage.connectors.WebServer

def port = WebServer.servers.keySet()[0]
if (!WebServer.servers[port].hasContext('/scripts')) {
  WebServer.servers[port].add('/scripts', new File(home, 'scripts'))
}

htmls = []
scripts.traverse(type: groovy.io.FileType.FILES, maxDepth: 0) {
  if (it.name.endsWith('.html')) htmls << it.name
}

NetworkInterface.getNetworkInterfaces().collect {
  it.inetAddresses.collect {
    def s = it.hostAddress
    if (s) {
      htmls.each {
        println("http://${s}:${port}/scripts/${it}")
      }
    }
  }
}
