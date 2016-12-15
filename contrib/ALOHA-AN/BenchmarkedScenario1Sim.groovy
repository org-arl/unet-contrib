//! Simulation: ALOHA-AN
///////////////////////////////////////////////////////////////////////////////
/// 
/// To run simulation:
///   bin/unet ALOHA-AN/BenchmarkedScenario1Sim.groovy
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
import org.arl.fjage.Agent.*

println '''
Aloha-AN simulation
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

def nodes = 1..6                     // list of nodes
def loadRange = [0.1, 1.5, 0.1] 
def T = 100.minutes                       // simulation horizon
trace.warmup =  10.minutes             // collect statistics after a while

///////////////////////////////////////////////////////////////////////////////
// simulation details

def addressList = new ArrayList<Integer>()
for(int i = 0;i<nodes.size();i++)
{
  addressList.add((i+1))
}

// Deploying nodes randomly within a radius

def max_range = 2.5.km
def nodeLocation = [:]
nodes.each { myAddr ->
  if(myAddr == 1)
  {
    nodeLocation[myAddr] = [0, 0, -10.m]    
  }
  else
  {    
    double theta = rnd(0,2*Math.PI)
    double radius = rnd(2.0, max_range)
    nodeLocation[myAddr] = [radius*Math.cos(theta), radius*Math.sin(theta), -10.m]     
  }
}
 

// compute average distance between nodes for display
def sum = 0
def n = 0
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

    // setup 4 nodes identically
    nodes.each { myAddr ->
    
      float loadPerNode = load/nodes.size()     // divide network load across nodes evenly
      def macAgent = new AlohaAN()
      if(myAddr == 1)
      {
        node_list << node("${myAddr}", address: myAddr, location: nodeLocation[myAddr] , shell : true, stack : { container ->   
          container.add 'mac', macAgent   
        }) 

      }
      else
      {
        node_list << node("${myAddr}", address: myAddr, location: nodeLocation[myAddr] , stack : { container -> 
          container.add 'mac', macAgent  
        })
      }    

      for(int i = 0;i<addressList.size();i++)
      {
        for(int j = 0;j<addressList.size();j++)
        {
          if(propagationDelay[i][j] != propagationDelay[j][i])
          {
            log.warning 'ERROR IN PROPAGATION_DELAY_PARAMETER'
          }  
          else
          {
            if(i == j && propagationDelay[i][i] != 0.0)
            {
              log.warning 'ERROR IN PROPAGATION_DELAY_PARAMETER'
            }
            else
            {
              macAgent.propagationDelay.add(propagationDelay[i][j])                                     
            }            
          }  
        }
      } 

      macAgent.dataMsgDuration = (int)(8000*modem.frameLength[1]/modem.dataRate[1] + 0.5)
      macAgent.controlMsgDuration = (int)(8000*modem.frameLength[0]/modem.dataRate[0] + 0.5)
      macAgent.nodeList  = addressList
      if(myAddr != 1)
      {
        container.add 'load', new LoadGenerator([1], loadPerNode)        
      }
    } // each



  }  // simulate

  // display statistics
  float loss = trace.txCount ? 100*trace.dropCount/trace.txCount : 0
  println sprintf('%6d\t\t%6d\t\t%5.1f\t\t%7.3f\t\t%7.3f',
    [trace.txCount, trace.rxCount, loss, load, trace.throughput])

  // save to file
  out << "${trace.offeredLoad},${trace.throughput}\n"

}

