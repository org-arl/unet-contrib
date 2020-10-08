## Demo 4.1

### On Node 1

Remote enable sensor on Node2

```
rsh 2, 'sensor.enable=true;'
```

Receives data and prints the values
```
s = new UnetSocket(this);
s.bind(Protocol.DATA);
while(1){
	rx = s.receive();
	if (rx) println("Received : ${rx.data as List<Byte>}")
	delay(1000)
}
```

## Demo 4.2

### On Node 4
```
container.add 'portal', new org.arl.unet.portal.UdpPortal(clientIP: 'localhost', clientPort: 5005);
addroute 2, 3
```

### On CLI
```
python ./sensor.py
```


 Netcat in UDP mode listening on port 5005
```
nc -ul 5005
```
