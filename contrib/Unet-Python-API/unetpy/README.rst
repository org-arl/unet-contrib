Python API for UnetStack
========================

This python package `unetpy` provides APIs to interact with any modems running UnetStack. The `unetpy` package is built upon `fjagepy`. This package allows the developers and users to interact with the modem using an interface implementd in python. All the requests made to the modem are using JSON messages. The relevant JSON messages are constructed and sent over some low level networked connection like TCP. The UnetStack client running on the modem understands these messages, takes corresponding actions and returns the notifications and/or responses back in the form of JSON messages which are parsed by `unetpy`.

General modules
---------------

The following modules provide all the functionalities needed to interact with the modems running UnetStack remotely:

    * `unetpy.org_arl_unet`
    * `unetpy.org_arl_unet_bb`
    * `unetpy.org_arl_unet_phy`


Usage
-----

Installation::

    pip install unetpy

To import all general modules::

    from unetpy import *

Useful links
------------

        * `unetpy home <https://github.com/org-arl/unet-contrib/tree/master/contrib/Unet-Python-API>`
        * `unetpy documentation <>`
