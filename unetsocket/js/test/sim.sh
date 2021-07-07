#!/bin/bash 

TEST_SCRIPT=2-node-des.groovy 

pattern="test/unet/unet-*"
files=($pattern)
DIR=${files[0]}

if [ "$1" == "start" ]; then
  "$DIR"/bin/unet test/"$TEST_SCRIPT" &
fi

if [ "$1" == "stop" ]; then
  PID=$(pgrep -f "java.*unet")
  kill "$PID"
fi