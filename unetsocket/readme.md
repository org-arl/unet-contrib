# UnetSocket API

[UnetSocket API](https://unetstack.net/handbook/unet-handbook_unetsocket_api.html), is a high-level [Socket-like](https://en.wikipedia.org/wiki/Network_socket) API for communicating over Unets. This API is implemented by [UnetStack](https://unetstack.net/).

This directory contains a collection of libaries and examples for using UnetSocket API in various programming languages.

## Libraries

- [Python](python/): Python library for UnetSocket API
- [C](c/): C library for UnetSocket API
- [JavaScipt](js/): JavaScript library for UnetSocket API
- [MATLAB](matlab/): MATLAB library for UnetSocket API

> NOTE: The Java library for UnetSocket API is already included in the [UnetStack](https://unetstack.net/).

Along with the UnetSocket API, these libaries also provide language-specific (and language idiomatic) helpers for working with UnetStack. For example, type definitions, service definitions, and other language-specific utilities. Individual library documentation will provide more details.

## Examples

Along with the libraries, this directory also contains examples of using UnetSocket API in various programming languages to perform common tasks. These examples are intended to be used as a starting point for building your own applications.

The examples include :

- `txwav` : Transmit a waveform out of a modem
- `record` : Record a waveform on a modem
- `rxwav` : Receive a waveform on a modem
- `txframe` : Transmit a [Physical layer] frame through a Unet
- `rxframe` : Receive a [Physical layer] frame through a Unet
- `txdatagram` : Transmit a [Link or higher layer] packet through a Unet
- `rxdatagram` : Receive a [Link or higher layer] packet through a Unet
- `range` : Measure range to a node
- `exec` : Execute a command on a modem
<!-- - `ping` : Ping a node over a Unet -->
<!-- - `discover` : Discover nodes in the network -->