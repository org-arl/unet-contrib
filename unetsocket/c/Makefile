CC = gcc
CFLAGS += -std=c99 -Wall -Wextra -Werror -Wfloat-equal -Wconversion -Wparentheses -pedantic -Wunused-parameter -Wunused-variable -Wreturn-type -Wno-unused-function -Wredundant-decls -Wreturn-type -Wunused-value -Wswitch-default -Wuninitialized -Winit-self -O2
FJAGE_VER=master
FJAGE_DIR=fjage-$(FJAGE_VER)

CONTRIB_VER = e91c0e81397d8df3e588a535306e78a78f2e7a48
BUILD = build
BUILD_API = $(BUILD)/api
CONTRIB_DIR = $(BUILD)/unet-contrib-$(CONTRIB_VER)

all: libs
	rm -rf *.zip
	rm -rf fjage-*

samples: txdata rxdata range setpowerlevel txsignal pbrecord bbrecord npulses wakeup rs232_wakeup gpio
	rm -rf *.zip
	rm -rf fjage-*

libs: libfjage.a libunet.a

libfjage.a: $(FJAGE_DIR)
	make -C $(FJAGE_DIR)/gateways/c/
	cp $(FJAGE_DIR)/gateways/c/fjage.h .
	cp $(FJAGE_DIR)/gateways/c/libfjage.a .

$(FJAGE_DIR):
	curl -LO https://github.com/org-arl/fjage/archive/$(FJAGE_VER).zip
	unzip -a $(FJAGE_VER).zip

libunet.a: unet.o
	$(AR) rc $@ $^

unet_ext.o: unet.h fjage.h
	$(CC) $(CFLAGS) -c unet_ext.c -o unet_ext.o

txdata.o: samples/txdata.c unet.h fjage.h
	$(CC) $(CFLAGS) -c samples/txdata.c -o samples/txdata.o

txdata: txdata.o
	$(CC) -o samples/txdata samples/txdata.o libunet.a libfjage.a -lpthread -lm

rxdata.o: samples/rxdata.c unet.h fjage.h
	$(CC) $(CFLAGS) -c samples/rxdata.c -o samples/rxdata.o

rxdata: rxdata.o
	$(CC) -o samples/rxdata samples/rxdata.o libunet.a libfjage.a -lpthread -lm

range.o: samples/range.c unet.h fjage.h unet_ext.o
	$(CC) $(CFLAGS) -c samples/range.c -o samples/range.o

range: range.o unet_ext.o
	$(CC) -o samples/range unet_ext.o samples/range.o libunet.a libfjage.a -lpthread -lm

setpowerlevel.o: samples/setpowerlevel.c unet.h fjage.h unet_ext.o
	$(CC) $(CFLAGS) -c samples/setpowerlevel.c -o samples/setpowerlevel.o

setpowerlevel: setpowerlevel.o unet_ext.o
	$(CC) -o samples/setpowerlevel unet_ext.o samples/setpowerlevel.o libunet.a libfjage.a -lpthread -lm

txsignal.o: samples/txsignal.c unet.h fjage.h unet_ext.o
	$(CC) $(CFLAGS) -c samples/txsignal.c -o samples/txsignal.o

txsignal: txsignal.o
	$(CC) -o samples/txsignal unet_ext.o samples/txsignal.o libunet.a libfjage.a -lpthread -lm

pbrecord.o: samples/pbrecord.c unet.h fjage.h unet_ext.o
	$(CC) $(CFLAGS) -c samples/pbrecord.c -o samples/pbrecord.o

pbrecord: pbrecord.o unet_ext.o
	$(CC) -o samples/pbrecord unet_ext.o samples/pbrecord.o libunet.a libfjage.a -lpthread -lm

bbrecord.o: samples/bbrecord.c unet.h fjage.h unet_ext.o
	$(CC) $(CFLAGS) -c samples/bbrecord.c -o samples/bbrecord.o

bbrecord: bbrecord.o
	$(CC) -o samples/bbrecord unet_ext.o samples/bbrecord.o libunet.a libfjage.a -lpthread -lm

npulses.o: samples/npulses.c unet.h fjage.h unet_ext.o
	$(CC) $(CFLAGS) -c samples/npulses.c -o samples/npulses.o

npulses: npulses.o unet_ext.o
	$(CC) -o samples/npulses unet_ext.o samples/npulses.o libunet.a libfjage.a -lpthread -lm

wakeup.o: samples/wakeup.c unet.h fjage.h unet_ext.o
	$(CC) $(CFLAGS) -c samples/wakeup.c -o samples/wakeup.o

wakeup: wakeup.o unet_ext.o
	$(CC) -o samples/wakeup unet_ext.o samples/wakeup.o libunet.a libfjage.a -lpthread -lm

rs232_wakeup.o: samples/rs232_wakeup.c unet.h fjage.h unet_ext.o
	$(CC) $(CFLAGS) -c samples/rs232_wakeup.c -o samples/rs232_wakeup.o

rs232_wakeup: rs232_wakeup.o unet_ext.o
	$(CC) -o samples/rs232_wakeup unet_ext.o samples/rs232_wakeup.o libunet.a libfjage.a -lpthread -lm

gpio.o: samples/gpio.c unet.h fjage.h unet_ext.o
	$(CC) $(CFLAGS) -c samples/gpio.c -o samples/gpio.o

gpio: gpio.o unet_ext.o
	$(CC) -o samples/gpio unet_ext.o samples/gpio.o libunet.a libfjage.a -lpthread -lm

test: test_unet
	rm -rf *.zip
	rm -rf fjage-*

test_unet.o: test/test_unet.c unet.h fjage.h
	$(CC) $(CFLAGS) -c test/test_unet.c -o test/test_unet.o

test_unet: test_unet.o
	$(CC) -o test/test_unet test/test_unet.o libunet.a libfjage.a -lpthread -lm

clean:
	rm -rf c-api libunet.a *.o samples/*.o test/*.o test/test_unet samples/wakeup samples/rs232_wakeup samples/txdata samples/rxdata samples/range samples/setpowerlevel samples/txsignal samples/pbrecord samples/recordedsignal.txt samples/bbrecord samples/npulses samples/gpio
	rm -rf "$(FJAGE_DIR)" "$(FJAGE_VER).zip" fjage.h libfjage.a kissfft kiss_fft.h fjage
	rm -rf $(BUILD)

$(CONTRIB_DIR): $(BUILD_API)
	curl -s -L https://github.com/org-arl/unet-contrib/archive/$(CONTRIB_VER).zip --output $(BUILD)/unet-contrib-$(CONTRIB_VER).zip
	unzip -q -o -a $(BUILD)/unet-contrib-$(CONTRIB_VER).zip -d $(BUILD)
	cp -r $(CONTRIB_DIR)/unetsocket/c/* $(BUILD_API)
	rm -rf $(BUILD_API)/test
	rm -rf $(CONTRIB_DIR)
	rm -rf $(CONTRIB_DIR).zip
	cd $(BUILD_API) && zip -r ../c-api.zip .
	rm -rf $(BUILD_API)

$(BUILD_API):
	mkdir -p $(BUILD_API)

package: $(CONTRIB_DIR)
