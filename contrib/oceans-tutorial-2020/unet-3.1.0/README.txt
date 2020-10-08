UnetStack3 Community Edition
============================

Requirements
------------

Mac OS X / Linux / Windows*
Java 8 or higher installed
Chrome 61+ / Firefox 60+ / Safari 10.1+
PortAudio** (on Mac OS X and Linux)

*  Unet audio not supported on Windows yet
** only needed for Unet audio

Getting started
---------------

To open the simulator IDE on your computer, open a terminal window in the unet folder
(where this README.txt is) and try:

  bin/unet sim

This will start the simulator and open a browser window with a web-based IDE. In
the command shell in the IDE (bottom window), type:

  sim.run 'samples/aloha.groovy'

This will run the simulation script within the IDE. To find out more commands in
the IDE, type 'help' at the command shell within the IDE.

If you prefer to run simulations directly from the command line (without the IDE),
in the terminal window, you could directly try:

  bin/unet samples/aloha.groovy

If everything goes well, you should see an output similar to:

  Pure ALOHA simulation
  =====================

  TX Count        RX Count        Offered Load    Throughput
  --------        --------        ------------    ----------
     614             525            0.068           0.058
    1228             962            0.137           0.107
    1871            1249            0.209           0.139
    2480            1407            0.277           0.156
    3093            1535            0.347           0.171
    3759            1616            0.421           0.180
    4273            1665            0.479           0.183
    4971            1599            0.558           0.178
    5540            1605            0.622           0.178
    6256            1532            0.702           0.170
    6940            1375            0.783           0.153
    7338            1407            0.826           0.156
    7992            1338            0.904           0.149
    8598            1282            0.972           0.142
    9394            1048            1.062           0.116

  15 simulations completed in 102.494 seconds

If not, visit the support forum at http://www.unetstack.net/ to seek help on
resolving the problem.

To get going with developing your own networks, protocol and simulations, read
the Unet handbook at http://www.unetstack.net/handbook/

Note for Mac OS X users
-----------------------

When you run UnetStack, if your Mac complains about opening an app from an
unidentified developer, you may need to update your security settings to say its okay.

After canceling the dialog about an unidentified developer, go to your Settings,
Security & Privacy, General, and say "Allow Anyway" for the warning telling you that the
libyoda library was blocked. Run UnetStack again, and say "Open" when you are warned
about this again. The Mac will then remember that it is okay, and will not ask you again.

More information:
* https://support.apple.com/en-sg/guide/mac-help/mh40616/mac
* https://www.macworld.co.uk/how-to/mac-software/mac-app-unidentified-developer-3669596/

Bundled dependencies
--------------------

The Unet community edition depends on the several open-source software libraries that
are bundled together in this package in binary form:

Groovy - Apache License v2 (http://www.apache.org/licenses/LICENSE-2.0.html)
Apache Commons - Apache License v2 (http://www.apache.org/licenses/LICENSE-2.0.html)
fjåge - BSD License (https://raw.github.com/org-arl/fjage/master/LICENSE.txt)
jline - BSD License (https://raw.github.com/jline/jline2/master/LICENSE.txt)
JTransforms - BSD License(https://github.com/wendykierp/JTransforms/blob/master/LICENSE)
GSON - Apache License v2 (http://www.apache.org/licenses/LICENSE-2.0.html)
OpenRQ - Apache License v2 (http://www.apache.org/licenses/LICENSE-2.0.html)
Objenesis - Apache License v2 (http://www.apache.org/licenses/LICENSE-2.0.html)
cloning - Apache License v2 (http://www.apache.org/licenses/LICENSE-2.0.html)
jetty - Eclipse Public License - v 1.0 (https://www.eclipse.org/org/documents/epl-v10.php)
jSerialComm - Apache License v2 (http://www.apache.org/licenses/LICENSE-2.0.html)
