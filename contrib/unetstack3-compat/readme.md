# UnetStack 3 compatibility for UnetStack4

This directory contains files for making a device running UnetStack 4.x.x be able to communicate acoustically with a device running UnetStack 3.x.x .

The differences are in the default configuration of the PHYSICAL and BASEBAND services.

## Usage
- Copy the `compat.groovy` file into the `scripts` directory of the device running UnetStack 4.x.x.
- Run the command `compat` on the shell. The PHYSICAL and BASEBAND services will be configured to be compatible with UnetStack 3.x.x

If you need to have the `compat` command run at every boot add the following line to `startup.groovy`.

```
run('compat')
```
