Unet Examples - C
=============

The file `examples.c` is an example code which uses the C APIs to interact with the modem running UnetStack.

The following steps are implemented:

- Open the gateway connection to modem
- Transmit a CONTROL packet
- Transmit a DATA packet
- Transmit a baseband signal
- Record a baseband signal
- Close the gateway connection to modem

In terminal window (an example):

```bash
make
./examples 'unet-modem'
```

Find the relevant [documentation on UnetStack and APIs is avaliable here](https://www.unetstack.net/docs.html)


