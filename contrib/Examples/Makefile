UNET_DIR?=/home/ubuntu/unet

all: bundle

bundle: groovy c
	tar --exclude='./.gradle' -cvzf ../Examples.tgz ../Examples

groovy: groovy/lib/unet-framework*.jar

groovy/lib/unet-framework*.jar:
	rm -f groovy/lib/*
	cd $(UNET_DIR); gradle
	mkdir -p groovy/lib/
	cp $(UNET_DIR)/lib/fjage*.jar $(UNET_DIR)/lib/groovy*.jar $(UNET_DIR)/lib/gson*.jar $(UNET_DIR)/lib/unet-framework*.jar $(UNET_DIR)/lib/commons-lang*.jar groovy/lib/

c: c/lib/libunet.a
	make -C ./c/

c/lib/libunet.a:
	make -C ../Unet-C-API/
	mkdir -p c/lib/
	cp ../Unet-C-API/libfjage.a c/lib/
	cp ../Unet-C-API/libunet.a c/lib/
	mkdir -p c/include/
	cp ../Unet-C-API/unet.h c/include/

clean:
	if [ -d $(UNET_DIR) ]; then cd $(UNET_DIR); gradle clean; fi
	make -C ./c/ clean
	rm -f c/lib/*.a
	rm -f c/include/unet.h
	rm -f groovy/lib/*
	rm -f Examples.tgz

.PHONY : groovy clean
