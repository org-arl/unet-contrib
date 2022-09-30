# UnetStack 3 compatibility for UnetStack4

This directory contains files for making a device running UnetStack 4.x.x be able to communicate acoustically with a device running UnetStack 3.x.x . 

## Eligibility

This is currently tested on Subnero WNC-M25M* devices only.

## Differences

The differences are in the default configuration of the PHYSICAL and BASEBAND services.

- `phy[].fec` : UnetStack 3.x.x uses an older set of FEC algorithms. UnetStack 4.x.x does support the older `XXXv3` algorithms, but those need to be choosen specifically.
- `bb[].preamble` : The preambles used for detection have changed between UnetStack 4.x.x and UnetStack 3.x.x. 
- `phy[].frameLength` : The default length of a PHYSICAL frame was different on UnetStack 4.x.x
- `bb[].threshold` : The threshold of the detector needs to be updated to 0.25 for UnetStack 3.x.x

## Usage
- Copy the `compat.groovy` file into the `scripts` directory of the device running UnetStack 4.x.x.
- Run the command `compat` on the shell. The PHYSICAL and BASEBAND services will be configured to be compatible with UnetStack 3.x.x

If you need to have the `compat` command run at every boot add the following line to `startup.groovy`.



```
run('compat')
```
