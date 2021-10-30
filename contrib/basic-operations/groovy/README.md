Unet Examples - Groovy
=============

The file `examples.groovy` is an example script which uses the groovy APIs to interact with the modem running UnetStack.

The following steps are implemented:

- Open the gateway connection to modem
- Transmit a CONTROL packet
- Transmit a DATA packet
- Transmit a baseband signal
- Record a baseband signal
- Close the gateway connection to modem

In terminal window (an example):

```bash
./groovy.sh examples.groovy 'unet-modem'
```

Find the relevant [documentation on UnetStack and APIs is avaliable here](https://www.unetstack.net/docs.html)


