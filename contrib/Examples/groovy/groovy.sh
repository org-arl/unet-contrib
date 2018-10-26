#!/bin/bash

FILE=$1
ARG=$2
java -cp ':lib/*' groovy.ui.GroovyMain $FILE $ARG
