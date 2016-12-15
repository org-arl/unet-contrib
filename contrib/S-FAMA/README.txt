Slotted FAMA Simulation
===================

Getting started
---------------
Open the Slotted FAMA directory.
You would have the following files in the Slotted FAMA folder.
1. SlottedFama.groovy : This is the MAC agent file.This file contains the protocol implementation.
2. SlottedFamaParam.groovy : This file contains the class where the parameters to be passed to the agent file are declared.
3. TestCaseSim.groovy : This is the simulation file with 6 nodes. 4 of them are deployed at the corners of the rectangle, while the other two are deployed at the centers of the longer sides of the rectangle.
4. BenchmarkedScenarioSim1.groovy : This is the simulation file with 5 nodes deployed randomly within a circular radius of 2.5km while a 6th node is deployed at the center of the circle. All 5 nodes send DATA to the 6th node at the center which acts as a sink.
5. BenchmarkedScenarioSim2.groovy : This is the simulation file with 5 nodes deployed randomly within a circular radius of 0.5km. All nodes can send data to all other nodes. 

To test the Slotted FAMA protocol in UnetSim on your Windows computer, run the UnetIDE application.
In the left side of the UnetIDE window, will be a sidebar showing the list of folders. Click on file in the menu bar and set root directory to the path of the Slotted FAMA folder.
Open the TestCaseSim.groovy file and click on the run button.

For Linux users, open the terminal and change path to the UnetSim directory.

Ti run the simulation, use the command,
  bin/unet Slotted FAMA/TestCaseSim.groovy     (Give correct path of the TestCaseSim.groovy file in the command)

If everything goes well, you should see an output similar to:

  Slotted FAMA simulation
 =========================

  Average internode distance: 1409 m, delay: 939 ms

  TX        RX        Loss %       Offered load      Throughput
 ----      ----      --------     --------------      ------------
  180       180        0.0            0.100              0.095
  360       360        0.0            0.200              0.199
  448       447        0.0            0.300              0.248
  453       453        0.0            0.400              0.252
  485       478        1.2            0.500              0.266
  493       487        1.2            0.600              0.271
  484       483        0.0            0.700              0.268
  480       475        1.0            0.800              0.264
  483       481        0.6            0.900              0.267
  499       496        0.6            1.000              0.276
  502       496        1.0            1.100              0.276
  490       490        0.0            1.200              0.272
  474       473        0.2            1.300              0.263
  497       497        0.0            1.400              0.276
  502       501        0.2            1.500              0.278                 

15 simulation completed in 100.979 seconds

[-END-]


If not, visit the support forum at http://www.unetstack.net/support/
to seek help on resolving the problem.

To get going with your own simulations, refer to the quick start guide at:
http://www.unetstack.net/doc/html/quickstart.html
