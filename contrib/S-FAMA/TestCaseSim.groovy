//! Simulation: Slotted FAMA
///////////////////////////////////////////////////////////////////////////////
/// 
/// To run simulation:
///   bin/unet Slotted FAMA/TestCaseSim.groovy
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

modem.dataRate         = [1000, 1000].bps
modem.frameLength      = [12, 375].bytes
modem.preambleDuration = 0
modem.txDelay          = 0
modem.clockOffset      = 0.s
modem.headerLength     = 0.s

channel.model               = ProtocolChannelModel
channel.soundSpeed          = 1500.mps
channel.communicationRange  = 1.5.km
channel.interferenceRange   = 1.5.km
channel.detectionRange      = 1.5.km


///////////////////////////////////////////////////////////////////////////////
// simulation settings

def nodes = 1..6                    // list of nodes
def loadRange = [0.1, 1.5, 0.1]     // min, max, step
def T         = 100.minutes          // simulation horizon
trace.warmup  = 10.0.minutes        // collect statistics after a while

///////////////////////////////////////////////////////////////////////////////
// simulation details

// generate random network geometry
def nodeLocation = [:]

def xPos = new Integer[6]
xPos[0] = 1
xPos[1] = 2
xPos[2] = 3
xPos[3] = 1
xPos[4] = 2
xPos[5] = 3

nodes.each { myAddr ->
  int yPos = myAddr/4
  nodeLocation[myAddr] = [xPos[myAddr-1].km, yPos.km, -15.m]
}

def addressList = new ArrayList<Integer>()
for(int i = 0;i<nodes.size();i++)
{
  addressList.add((i+1))
}

ArrayList<ArrayList<Integer>> destinationList  = new ArrayList<ArrayList<Integer>>()
for(int i = 0;i<nodes.size();i++)
{
  destinationList.add(new ArrayList<Integer>())   
}

destinationList[0].add(2)
destinationList[0].add(4)
destinationList[0].add(5)

destinationList[1].add(1)
destinationList[1].add(3)
destinationList[1].add(4)
destinationList[1].add(5)
destinationList[1].add(6)

destinationList[2].add(2)
destinationList[2].add(5)
destinationList[2].add(6)

destinationList[3].add(1)
destinationList[3].add(2)
destinationList[3].add(5)

destinationList[4].add(1)
destinationList[4].add(2)
destinationList[4].add(3)
destinationList[4].add(4)
destinationList[4].add(6)

destinationList[5].add(2)
destinationList[5].add(3)
destinationList[5].add(5)

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
      container.add 'load', new LoadGenerator(destinationList[myAddr-1], loadPerNode)        

    } // each       

  }  // simulate

  // display statistics
  float loss = trace.txCount ? 100*trace.dropCount/trace.txCount : 0
  println sprintf('%6d\t\t%6d\t\t%5.1f\t\t%7.3f\t\t%7.3f',
    [trace.txCount, trace.rxCount, loss, load, trace.throughput])

  // save to file
  out << "${trace.offeredLoad},${trace.throughput}\n"

} 

