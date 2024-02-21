#!/bin/bash

TEST_SCRIPT=2-node-des.groovy

pattern="test/unet/unet-*"
files=($pattern)
DIR=${files[0]}

if [ "$1" == "start" ]; then
  "$DIR"/bin/unet test/"$TEST_SCRIPT" > /dev/null 2>&1 &
fi

if [ "$1" == "stop" ]; then
  for pid in $(pgrep -f "java.*unet"); do kill -9 $pid > /dev/null 2>&1; done
fi