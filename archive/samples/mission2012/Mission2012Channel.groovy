///////////////////////////////////////////////////////////////////////////////
///
/// MISSION 2012 channel model
///
/// Reference:
/// [1] M. Chitre, I. Topor, R. Bhatnagar and V. Pallayil,
///     "Variability in link performance of an underwater acoustic network,"
///     IEEE OCEANS'13 Bergen, 2013.
///
/// This model is exactly the same as org.arl.unet.sim.channels.Mission2012a
/// and is included in the samples folder for illustration.
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.unet.sim.*
import org.arl.unet.sim.channels.ProtocolChannelModel

class Mission2012Channel extends ProtocolChannelModel {
  
  static final def nodes = [21, 22, 27, 28, 29]
  static final def nodeLocation = [
    21: [   0,    0,  -5],
    22: [ 398, -105, -18],
    27: [-434, -499, -12],
    28: [ -32,  279, -20],
    29: [-199, -307, -12]
  ]
  static def pNoDetect = [
    [    0, 0.047, 0.095, 0.026, 0.056],
    [0.032,     0, 0.228, 0.139, 0.081],
    [0.047, 0.174,     0, 0.025, 0.011],
    [0.019, 0.060, 0.040,     0, 0.420],
    [0.026, 0.018, 0.009, 0.048,     0]
  ]
  static def pNoDetectOrDecode = [
    [    0, 0.157, 0.643, 0.197, 0.239],
    [0.184,     0, 0.870, 0.639, 0.435],
    [0.326, 0.826,     0, 0.975, 0.023],
    [0.038, 0.160, 0.760,     0, 0.900],
    [0.070, 0.070, 0.018, 0.871,     0]
  ]

  float getProbabilityDetection(Reception rx) {
    int from = nodes.indexOf(rx.from)
    int to = nodes.indexOf(rx.address)
    if (from < 0 || to < 0) return 0
    return 1-pNoDetect[from][to]
  }

  float getProbabilityDecoding(Reception rx) {
    int from = nodes.indexOf(rx.from)
    int to = nodes.indexOf(rx.address)
    if (from < 0 || to < 0) return 0
    return (1-pNoDetectOrDecode[from][to])/(1-pNoDetect[from][to])
  }

}
