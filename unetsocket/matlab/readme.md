# How to connect to modem with MATLAB

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

## Open a connection to modem

In MATLAB, connect to the modem (e.g. 192.168.1.42),
```matlab
modem = modem_open_eth('192.168.1.42')
```

## Example of transmit and record

Transmit a pulse train and record immediately,
```matlab
signal = ones(100, 1)  % example signal
npulses = 10           % number of times signal transmission is repeated
pri = 100              % pulse repitition interval in ms
recbufsize = 200000    % number of samples to record

[recbuf, txtimes, status] = modem_tx_and_record(modem, signal, npulses, pri, recbufsize)
```
`recbuf` contains the recorded signal, `txtimes` contains the transmission start times and `status` indicates whether the operation was successful.

## Close connection to modem

To close the connection to modem:

```matlab
modem_close(modem)
```


## Creating a `tx.txt` file for `tx_and_record_api_example`

```
Fs = 192000
T = 3e-3
fc = 30000
t = (0:fix(T*Fs) - 1 )./Fs
tx = sin(2*pi*fc*t)
save('tx.txt', '-ascii', 'tx')
```
