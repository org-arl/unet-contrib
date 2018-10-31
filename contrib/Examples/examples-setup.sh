#!/bin/bash

cd /home/ubuntu/unet

rm -rf ../user-examples
rm ../user-examples.tgz
mkdir ../user-examples

# copy examples
cp -r /home/ubuntu/unet-contrib/contrib/Examples/python ../user-examples/
cp -r /home/ubuntu/unet-contrib/contrib/Examples/groovy ../user-examples/
cp -r /home/ubuntu/unet-contrib/contrib/Examples/c ../user-examples/

# build and copy the relevant jars for groovy examples
gradle clean
gradle modems:yoda:tegrapkg -Dnomanual -Dnogpg
rm -rf ../user-examples/groovy/lib
mkdir ../user-examples/groovy/lib
cp lib/fjage-1.4.2.jar lib/groovy-all-2.4.15.jar lib/gson-2.8.2.jar lib/unet-framework-1.4.jar ../user-examples/groovy/lib/

# build and copy relevant libraries and header file for c examples
rm -rf ../user-examples/c/lib/
rm -rf ../user-examples/c/include/
mkdir ../user-examples/c/lib
mkdir ../user-examples/c/include
make -C /home/ubuntu/unet-contrib/contrib/Unet-C-API/
cp /home/ubuntu/unet-contrib/contrib/Unet-C-API/libfjage.a ../user-examples/c/lib/
cp /home/ubuntu/unet-contrib/contrib/Unet-C-API/libunet.a ../user-examples/c/lib/
cp /home/ubuntu/unet-contrib/contrib/Unet-C-API/unet.h ../user-examples/c/include/

# compress to distribute
tar -C /home/ubuntu -cvzf ../user-examples.tgz user-examples

# clean up 
rm -rf ../user-examples
