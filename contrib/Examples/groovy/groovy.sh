#!/bin/bash

FILE=$1
java -cp ':lib/*' groovy.ui.GroovyMain $FILE
