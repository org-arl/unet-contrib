Python API for UnetStack
========================

This python package `unetpy` provides APIs to interact with any modems running UnetStack. The `unetpy` package is built upon `fjagepy`. This package allows the developers and users to interact with the modem using an interface implementd in python. All the requests made to the modem are using JSON messages. The relevant JSON messages are constructed and sent over some low level networked connection like TCP. The UnetStack server running on the modem understands these messages, takes corresponding actions and returns the notifications and/or responses back in the form of JSON messages which are parsed by `unetpy`.


Usage
-----

Installation::

    pip install unetpy

To import all general modules::

    from unetpy import *

Sample notebook:

    `python-gateway-tutorial.ipynb <https://github.com/org-arl/unet-contrib/blob/master/contrib/Unet-Python-API/python-gateway-tutorial.ipynb>`_

Useful links
------------

        * `unetpy home <https://github.com/org-arl/unet-contrib/tree/master/contrib/Unet-Python-API>`_
        * `Unet project home <http://www.unetstack.net>`_
