# Accessing UnetStack Java APIs using MATLAB

#### Key steps:

1. Setup MATLAB to use Java 1.8
2. Add relevant jars to MATLAB's static classpath
3. Open a connection to modem
4. Send and receive messages
5. Close the connection to modem

## Set up MATLAB to use Java 1.8

UnetStack3 uses Java 1.8, and so we need to ensure MATLAB uses Java 1.8 or higher as well. Up until MATLAB 2017b, it ships with Java 1.7. But it's easy to switch to Java 1.8:

Find out your Java 1.8 home directory:

```bash
$ java ‐XshowSettings:properties
    :
    :
java.home = /usr/local/jdk1.8.0_72.jdk/jre
    :
    :
```

**Setup on MAC OS:**

Set your MATLAB_JAVA environment variable to point to it:

```bash
$ export MATLAB_JAVA=/usr/local/jdk1.8.0_72.jdk/jre
```
Start MATLAB from the command line. On Mac OS X:
```bash
open ‐a MATLAB_2016a
```
Alternatively the steps can also be followed as provided at the following link: [Change JVM that MATLAB is using on Mac OS](https://www.mathworks.com/matlabcentral/answers/103056-how-do-i-change-the-java-virtual-machine-jvm-that-matlab-is-using-on-macos).

**Setup on Linux:**

Steps for changing JVM that MATLAB is using on Linux: [Change JVM that MATLAB is using on Linux](https://www.mathworks.com/matlabcentral/answers/130360-how-do-i-change-the-java-virtual-machine-jvm-that-matlab-is-using-for-linux).

**Setup on Windows:**

Steps for changing JVM that MATLAB is using on Windows: [Change JVM that MATLAB is using on Windows](https://www.mathworks.com/matlabcentral/answers/130359-how-do-i-change-the-java-virtual-machine-jvm-that-matlab-is-using-on-windows).

## Add relevant jars to MATLAB's static classpath

MATLAB's dynamic classpath is managed by classloader that does not work well with dynamic class loading. So it is important to add the jars to the static classpath.

To do this, first find out where MATLAB stores it's preferences. In MATLAB:

```bash
>> prefdir
/home/myname/.matlab/R2016a
```
Now, edit/create a file called `/home/myname/.matlab/R2016a/javaclasspath.txt` and add the following jars to it:

```
/home/myname/matlab-api/jars/commons-lang3-3.6.jar
/home/myname/matlab-api/jars/fjage-1.5.2-SNAPSHOT.jar
/home/myname/matlab-api/jars/gson‐2.8.2.jar
/home/myname/matlab-api/jars/unet-framework-1.4.jar
/home/myname/matlab-api/jars/unet-stack-1.4.jar
/home/myname/matlab-api/jars/unet-yoda-1.4.jar
```

Of course, when doing this, use your actual folders, not `/home/myname/...`.

And restart MATLAB once you've edited the static classpath. If you type `javaclasspath`
in MATLAB, you should be able to see these files on the static classpath.

## Create a fjåge Gateway and connect to modem, send messages, and shutdown

In MATLAB, connect to the modem (e.g. 192.168.0.42), send messages (e.g. ask for a baseband recording), and shutdown:

```matlab
>> modem = org.arl.fjage.remote.Gateway('192.168.0.42', 1100)
>> bb = modem.agentForService(org.arl.unet.Services.BASEBAND)
>> msg = org.arl.unet.bb.RecordBasebandSignalReq()
>> msg.setRecipient(bb)
>> modem.request(msg, 1000)
ans =
AGREE
>> ntf = modem.receive()
ntf =
RxBasebandSignalNtf:INFORM rxTime:42836833 rssi:‐79.5 adc:1 fc:12000 fs:12000 (12000 samples)
>> plot(ntf.getSignal())
>> modem.shutdown()
```
## Example of recording a signal

```matlab
% subscribe to the agent providing the baseband service
agent = modem.agentForService(org.arl.unet.Services.BASEBAND); 
modem.subscribe(agent);

% create the message with relevant attributes to be sent to the modem 
req = org.arl.unet.bb.RecordBasebandSignalReq(); 
req.setRecipient(agent);

% send the message to the modem and wait for the response 
rsp = modem.request(req, 5000);

% check if the message was successfully sent
if isjava(rsp) && rsp.getPerformative() == org.arl.fjage.Performative.AGREE
	cls = org.arl.unet.bb.RxBasebandSignalNtf().getClass(); % receive the notification message containing the signal ntf = modem.receive(cls, 5000);
end

% plot the recorded signal 
plot(ntf.getSignal())
```

## Example of transmitting a frame

```matlab
% subscribe to the agent providing the physical service
agent = modem.agentForService(org.arl.unet.Services.PHYSICAL); 
modem.subscribe(agent);

% create the message with relevant attributes to be sent to the modem req = org.arl.unet.phy.TxFrameReq();
req.setRecipient(agent);

% send the message to the modem and wait for the response 
rsp = modem.request(req, 5000);

% check if the message was successfully sent
if isjava(rsp) && rsp.getPerformative() == org.arl.fjage.Performative.AGREE
	cls = org.arl.unet.phy.TxFrameNtf().getClass();
	% receive the notification message
	ntf = modem.receive(cls, 5000);
end
```

## Example of transmitting a baseband signal

```matlab
% load the baseband signal
% signal.txt contains interleaved real and imaginary values in a single column % with values normalized between +1 and ‐1
x = load('signal.txt');

% open the modem gateway
modem = org.arl.fjage.remote.Gateway('192.168.0.42', 1100);

% subscribe to the agent providing baseband service
bb = modem.agentForService(org.arl.unet.Services.BASEBAND);

% create the message with relevant attributes to be sent to the modem 
msg = org.arl.unet.bb.TxBasebandSignalReq();
msg.setSignal(x);
msg.setRecipient(bb);

% send the message to modem
rsp = modem.request(msg, 1000);

% check if the message was successfully sent
if isjava(rsp) && rsp.getPerformative() == org.arl.fjage.Performative.AGREE
	cls = org.arl.unet.phy.TxFrameNtf().getClass(); 
	% receive the notification message
	ntf = modem.receive(cls, 5000);
end
```

## Example of transmitting a passband signal

```matlab
% load the passband signal
% signal.txt must contain the real values in single column sampled at 192KHz % with values normalized between +1 and ‐1
x = load('signal.txt');

% open the modem gateway
modem = org.arl.fjage.remote.Gateway('192.168.0.42', 1100);

% subscribe to the agent providing baseband service
bb = modem.agentForService(org.arl.unet.Services.BASEBAND);

% create the message with relevant attributes to be sent to the modem
msg = org.arl.unet.bb.TxBasebandSignalReq(); 
msg.setSignal(x);
msg.setRecipient(bb);

% to transmit the passband signal the carrier frequency attribute is set to 0
msg.setCarrierFrequency(0)

% send the message to modem
rsp = modem.request(msg, 1000);

% check if the message was successfully sent
if isjava(rsp) && rsp.getPerformative() == org.arl.fjage.Performative.AGREE
	cls = org.arl.unet.phy.TxFrameNtf().getClass(); 
	% receive the notification message
	ntf = modem.receive(cls, 5000);
end
```
















