# Accessing UnetStack Java APIs using MATLAB

#### Overview

- Setup MATLAB to use Java 1.8
- Add relevant jars to MATLAB's static classpath
- Create a unet socket connection
- Example of transmitting a frame
- Example of receiving a frame
- Example of recording a signal
- Example of transmitting a baseband signal
- Example of transmitting a passband signal
- Close the socket connection

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
/home/myname/matlab-api/jars/commons-lang*.jar
/home/myname/matlab-api/jars/fjage-*.jar
/home/myname/matlab-api/jars/gson‐*.jar
/home/myname/matlab-api/jars/unet-framework-*.jar
/home/myname/matlab-api/jars/unet-basic-*.jar
/home/myname/matlab-api/jars/unet-yoda-*.jar
/home/myname/matlab-api/jars/groovy-*.jar
/home/myname/matlab-api/jars/websocket-servlet-*.jar

```

Of course, when doing this, use your actual folders, not `/home/myname/...`.

And restart MATLAB once you've edited the static classpath. If you type `javaclasspath` in MATLAB, you should be able to see these files on the static classpath. All the UnetStack JAVA APIs can be directly accessed in MATLAB once the jars are included in the `javaclasspath`. In the rest of this article we will show few examples of accessing the UnetStack Java APIs from MATLAB and interaction with UnetStack.

## Create a unet socket connection

In MATLAB, open a unet socket connection to the modem (e.g. 192.168.0.42):

```matlab
>> sock = org.arl.unet.api.UnetSocket('192.168.0.42', 1100)
```

### Look for agents providing specific services

In order to search for agents providing a specific service, we can use the `agentForService` method. An example is shown below where we are looking for agent providing the BASEBAND service.

```matlab
>> bb = sock.agentForService(org.arl.unet.Services.BASEBAND)
```

### Get the fjåge gateway

It is easy to get access to the gateway class and it's methods as shown below:

```matlab
>> modem = sock.getGateway()
```

### Create a message

We can create messages that UnetStack supports and understands for interacting with it. For example, to create a message to record a signal, we can create a [`RecordBasebandSignalReq`](https://unetstack.net/javadoc/3.0/org/arl/unet/bb/RecordBasebandSignalReq.html) message as shown below:

```matlab
>> msg = org.arl.unet.bb.RecordBasebandSignalReq()
>> msg.setRecipient(bb)
```

In the code snippet shown above, we created a `RecordBasebandSignalReq` message and set it's recipient to be the `AgentID` of the agent providing the BASEBAND service.

### Send the message to UnetStack running on modem

To send a message to UnetStack and receive the response message back, we can use `request` method as shown below:

```matlab
>> modem.request(msg, 1000)
ans =
AGREE
```

### Receive notifications from UnetStack

Sometimes there are unsolicited notifications that are published on the agent's topic and anyone subscribing to the topic receives these notifications. In addition, certain notifications are generated and sent to the requestor. An example to receive these notifications on the gateway is shown below:

```matlab
>> ntf = modem.receive()
ntf =
RxBasebandSignalNtf:INFORM rxTime:42836833 rssi:‐79.5 adc:1 fc:12000 fs:12000 (12000 samples)
```
Since, we are receiving this message in MATLAB, we can utilize it to extract and analyze the data. Since in this case, we recorded a signal, we can plot it as shown below:

```matlab
plot(ntf.getSignal())
```

## Example of transmitting a frame

The code snippet below is an example for transmitting a frame

```matlab
% subscribe to the agent providing the physical service
agent = modem.agentForService(org.arl.unet.Services.PHYSICAL);
modem.subscribe(agent);

% create the message with relevant attributes to be sent to the modem
req = org.arl.unet.phy.TxFrameReq();
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

## Example of receiving a frame

The code snippet below should be run on the receiver modem,

```matlab
% subscribe to the agent providing the physical service
agent = modem.agentForService(org.arl.unet.Services.PHYSICAL);
modem.subscribe(agent);

% receive the notification message
cls = org.arl.unet.phy.RxFrameNtf().getClass();
ntf = modem.receive(cls, 5000);
```
If the `RxFrameNtf` is successfully received, the following output can be seen in the `ntf` variable 

```matlab
ntf =
RxFrameNtf:INFORM[type:CONTROL from:200 rxTime:6066483160 rssi:-2.6 cfo:0.0 ber:3/432]
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
	% receive the notification message containing the signal
	cls = org.arl.unet.bb.RxBasebandSignalNtf().getClass();
	ntf = modem.receive(cls, 5000);
end

% plot the recorded signal
plot(ntf.getSignal())
```

## Example of transmitting a baseband signal

```matlab
% load the baseband signal
% signal.txt contains interleaved real and imaginary values in a single column
% with values normalized between +1 and ‐1
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
% signal.txt must contain the real values in single column sampled at 192KHz
% with values normalized between +1 and ‐1
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

## Close the socket connection

To close the socket connection

```matlab
sock.close()
```

Once the connection is closed, the socket and the gaetway methods can no longer be accessed.
