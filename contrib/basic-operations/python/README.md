Unet Examples - Python
=============

The file `examples.py` is an example script which uses the python APIs to interact with the modem running UnetStack.

The following steps are implemented:

- Open the gateway connection to modem
- Transmit a CONTROL packet
- Transmit a DATA packet
- Transmit a baseband signal
- Record a baseband signal
- Close the gateway connection to modem

In order to run this example script, you need `unetpy` python module installed. If not already installed, please install it by running the following command in terminal window 

```bash
pip3 install unetpy
```

In terminal window (an example):

``` bash
python3 examples.py 'unet-modem'
```

Find the relevant [documentation on UnetStack and APIs is avaliable here](https://www.unetstack.net/docs.html)


