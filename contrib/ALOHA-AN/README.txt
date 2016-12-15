ALOHA-AN Simulation
===================

Getting started
---------------

Open the ALOHA-AN folder. 
You would have the following files in the ALOHA-AN folder.
1. AlohaAN.groovy : This is the MAC agent file.This file contains the protocol implementation.
2. AlohaANParam.groovy : This file contains the class where the parameters to be passed to the agent file are declared.
3. TestCaseSim.groovy : This is the simulation file with 4 nodes deployed randomly in a square area of 3km*3km.
4. BenchmarkedScenarioSim1.groovy : This is the simulation file with 5 nodes deployed randomly within a circular radius of 2.5km while a 6th node is deployed at the centre of the circle. All 5 nodes send DATA to the 6th node at the center which acts as a sink.
5. BenchmarkedScenarioSim2.groovy : This is the simulation file with 5 nodes deployed randomly within a circular radius of 0.5km. All nodes can send data to all other nodes. 

To test the ALOHA AN protocol in UnetSim on your Windows computer, run the UnetIDE application.
In the left side of the UnetIDE window, will be a sidebar showing the list of folders. Click on File in the menu bar and set root directory to the path of the ALOHA-AN folder.
Open the TestCaseSim.groovy file and click on the run button.

For Linux users, open the terminal and change path to the UnetSim directory.

To run the simulation, use the command,
  bin/unet ALOHA-AN/TestCaseSim.groovy     (Give correct path of the TestCaseSim.groovy file in the command)

If everything goes well, you should see an output similar to:

  Aloha-AN simulation
 =====================

  Average internode distance: 925 m, delay: 422 ms

  TX        RX        Loss %         Offered load       Throughput
 ----      ----      --------       --------------     ------------
  531       525        1.1              0.100              0.097
 1018       985        3.1              0.200              0.182
 1499      1405        6.2              0.300              0.260
 1783      1605       10.0              0.400              0.297
 1943      1624       16.4              0.500              0.301
 2028      1723       15.0              0.600              0.319
 2061      1765       14.4              0.700              0.327
 2085      1775       14.9              0.800              0.329
 2173      1879       13.5              0.900              0.348
 2175      1917       11.9              1.000              0.355
 2329      1999       14.2              1.100              0.370
 2385      2057       13.8              1.200              0.381
 2325      2061       11.4              1.300              0.382
 2406      2138       11.1              1.400              0.396
 2400      2142       10.8              1.500              0.397                 

15 simulation completed in 984.473 seconds

[-END-]


If not, visit the support forum at http://www.unetstack.net/support/
to seek help on resolving the problem.

To get going with your own simulations, refer to the quick start guide at:
http://www.unetstack.net/doc/html/quickstart.html
