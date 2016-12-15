//! Simulation: AUV motion patterns
///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/mobility/mobility
///
/// Output trace file: logs/trace.nam
/// Plot results: bin/unet samples/mobility/plot-tracks
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.fjage.*
import org.arl.unet.sim.MotionModel
import static org.arl.unet.Services.*

println '''
Motion model simulation
=======================
'''

//////////////////////////////////////////////////////////////////////////
////// Utility closure to log AUV locations every 10 seconds

trackAuvLocation = {
  def nodeinfo = agentForService NODE_INFO
  trace.moved(nodeinfo.address, nodeinfo.location, null)
  add new TickerBehavior(10000, {
    trace.moved(nodeinfo.address, nodeinfo.location, null)
  })
}

//////////////////////////////////////////////////////////////////////////
////// Linear motion

println 'Simulation AUV-1: Linear motion'
simulate 10.minutes, {
  def n = node('AUV-1', location: [0, 0, 0], mobility: true)
  n.startup = trackAuvLocation
  n.motionModel = [speed: 1.mps, heading: 30.deg]
}

//////////////////////////////////////////////////////////////////////////
////// Circular motion

println 'Simulation AUV-2: Circular motion'
simulate 10.minutes, {
  def n = node('AUV-2', location: [0, 0, 0], mobility: true)
  n.startup = trackAuvLocation
  n.motionModel = [speed: 1.mps, turnRate: 1.dps]
}

//////////////////////////////////////////////////////////////////////////
////// Triangular motion (with diving)

println 'Simulation AUV-3: Triangular motion (with dive)'
simulate 15.minutes, {
  def n = node('AUV-3', location: [-50.m, -50.m, 0], mobility: true)
  n.startup = trackAuvLocation
  n.motionModel = [[time:  0.minutes, heading:  60.deg, speed:       1.mps],
                   [time:  3.minutes, turnRate:  2.dps, diveRate:  0.1.mps],
                   [time:  4.minutes, turnRate:  0.dps, diveRate:    0.mps],
                   [time:  7.minutes, turnRate:  2.dps                    ],
                   [time:  8.minutes, turnRate:  0.dps                    ],
                   [time: 11.minutes, turnRate:  2.dps, diveRate: -0.1.mps],
                   [time: 12.minutes, turnRate:  0.dps, diveRate:    0.mps]]
}

//////////////////////////////////////////////////////////////////////////
////// Lawnmower survey (with diving)

println 'Simulation AUV-4: Lawnmower survey (with dive)'
simulate 1.hour, {
  def n = node('AUV-4', location: [-20.m, -150.m, 0], heading: 0.deg, mobility: true)
  n.startup = trackAuvLocation
  // dive to 30m before starting survey
  n.motionModel = [[duration: 5.minutes, speed: 1.mps, diveRate: 0.1.mps], [diveRate: 0.mps]]
  // then do a lawnmower survey
  n.motionModel += MotionModel.lawnmover(speed: 1.mps, leg: 200.m, spacing: 20.m, legs: 10)
  // finally, come back to the surface and stop
  n.motionModel += [[duration: 5.minutes, speed: 1.mps, diveRate: -0.1.mps], [diveRate: 0.mps, speed: 0.mps]]
}

//////////////////////////////////////////////////////////////////////////
////// Done!

println "\nYou can visualize results by running samples/mobility/plot-tracks.groovy"
