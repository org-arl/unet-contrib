//! Simulation: Slotted FAMA
///////////////////////////////////////////////////////////////////////////////
/// 
/// To run simulation:
///   bin/unet Slotted FAMA/BenchmarkedScenario2Sim.groovy
///
/// Output trace file: logs/trace.nam
///
///////////////////////////////////-////////////////////////////////////////////
import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*
import static org.arl.unet.Services.*
import static org.arl.unet.phy.Physical.*

println '''
Slotted-FAMA simulation
===================
'''

///////////////////////////////////////////////////////////////////////////////
// modem and channel model to use

modem.dataRate = [2400, 2400].bps
modem.frameLength = [4, 512].bytes
modem.preambleDuration = 0
modem.txDelay = 0
modem.clockOffset = 0.s
modem.headerLength = 0.s

channel.model = ProtocolChannelModel
channel.soundSpeed = 1500.mps
channel.communicationRange = 5.km
channel.interferenceRange = 5.km
channel.detectionRange = 5.km

//platform = org.arl.fjage.RealTimePlatform

///////////////////////////////////////////////////////////////////////////////
// simulation settings

def nodes = 1..5                     // list of nodes
def loadRange = [0.1, 1.5, 0.1] 
def T = 100.minutes                    // simulation horizon
trace.warmup =  10.minutes            // collect statistics after a while

///////////////////////////////////////////////////////////////////////////////
// simulation details

// generate random network geometry

def addressList = new ArrayList<Integer>()
for(int i = 0;i<nodes.size();i++)
{
  addressList.add((i+1))
}

double radius = 0.5.km
double depth  = 5.0.m
def nodeLocation = [:]
nodes.each { myAddr ->
  double theta = rnd(0,2*Math.PI)
  nodeLocation[myAddr] = [radius*Math.cos(theta), radius*Math.sin(theta), (-1*myAddr*depth)]     
}

// compute average distance between nodes for display
def sum = 0
def n = 0
int maxPropagationDelay = 0
def propagationDelay = new Integer[nodes.size()][nodes.size()]
nodes.each { n1 ->
  nodes.each { n2 ->
    if (n1 < n2) {
      n++
      sum += distance(nodeLocation[n1], nodeLocation[n2])
    }
    propagationDelay[n1-1][n2-1] = (int)(distance(nodeLocation[n1],nodeLocation[n2]) * 1000 / channel.soundSpeed + 0.5)
  }
}

def avgRange = sum/n
println """Average internode distance: ${Math.round(avgRange)} m, delay: ${Math.round(1000*avgRange/channel.soundSpeed)} ms

TX Count\tRX Count\tLoss %\t\tOffered Load\tThroughput
--------\t--------\t------\t\t------------\t----------"""   

File out = new File("logs/results.txt")
out.text = ''

// simulate at various arrival rates
for (def load = loadRange[0]; load <= loadRange[1]; load += loadRange[2]) {
  simulate T, {
    def node_list = []
    // setup nodes 
    nodes.each { myAddr ->
    
      float loadPerNode = load/nodes.size()     // divide network load across nodes evenly
      def macAgent = new SlottedFama()
      node_list << node("${myAddr}", address: myAddr, location: nodeLocation[myAddr] , stack : { container -> 
      container.add 'mac', macAgent        
      }) 

    for(int i = 0; i<nodes.size(); i++)
    {
      for(int j = 0; j<nodes.size(); j++)
      {
        if(propagationDelay[i][j] > maxPropagationDelay)
        {
          maxPropagationDelay = propagationDelay[i][j]
        }
      } 
    }        

      macAgent.timerCtsTimeoutOpMode = 2
      macAgent.maxPropagationDelay = maxPropagationDelay
      macAgent.dataMsgDuration = (int)(8000*modem.frameLength[1]/modem.dataRate[1] + 0.5)
      macAgent.controlMsgDuration = (int)(8000*modem.frameLength[0]/modem.dataRate[0] + 0.5)
      container.add 'load', new LoadGenerator(nodes-myAddr, loadPerNode)
    } // each       

  }  // simulate

  // display statistics
  float loss = trace.txCount ? 100*trace.dropCount/trace.txCount : 0
  println sprintf('%6d\t\t%6d\t\t%5.1f\t\t%7.3f\t\t%7.3f',
    [trace.txCount, trace.rxCount, loss, load, trace.throughput])

  // save to file
  out << "${trace.offeredLoad},${trace.throughput}\n"

} 

