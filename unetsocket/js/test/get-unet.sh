#!/bin/bash

# Downloads and sets up UnetStack for testing unet.js.

UNET_URL=https://unetstack.net/downloads/unet-community-3.1.0.tgz

if [ ! -d "test/unet" ]; then 
    mkdir -p test/unet
    wget -P test/unet/ "$UNET_URL"
    tar -C test/unet/ -xvzf test/unet/unet-community*.tgz
    rm test/unet/unet-community*.tgz
fi 

