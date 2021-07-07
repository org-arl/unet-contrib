#!/bin/bash

# Downloads and sets up UnetStack, runs the example simulated Unet

UNET_URL=https://unetstack.net/downloads/unet-community-3.1.0.tgz
TEST_SCRIPT=2-node-des.groovy 


if [ -z "$(ls -d unet-*)" ]; then 
    wget "$UNET_URL"
    tar -xvzf unet-*.tgz
    rm unet-*.tgz
fi

pattern="unet-*"
files=($pattern)
DIR=${files[0]}

echo "Starting Unet simulation and web-server.."

( "$DIR"/bin/unet "$TEST_SCRIPT" & npx http-server -p 8000 --silent & echo "Ready! Open http://localhost:8000/nodeA.html and http://localhost:8000/nodeB.html" & wait )

PID=$(pgrep -f "java.*unet")
echo "Stopping the Unet simulation : $PID"
kill "$PID"

echo "Done"