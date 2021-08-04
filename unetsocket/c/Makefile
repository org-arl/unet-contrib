CC = gcc
CFLAGS += -std=c99 -Wall -Wextra -Werror -Wfloat-equal -Wconversion -Wparentheses -pedantic -Wunused-parameter -Wunused-variable -Wreturn-type -Wno-unused-function -Wredundant-decls -Wreturn-type -Wunused-value -Wswitch-default -Wuninitialized -Winit-self -O2
FJAGE_VER=b49c7ac1b071ded3dbd2a58db39818da54a202ba
FJAGE_DIR=fjage-$(FJAGE_VER)
BUILD = build
BUILD_API = $(BUILD)/api
CONTRIB_DIR = $(BUILD)/temp

all: libs
	rm -rf *.zip
	rm -rf fjage-*

samples: txdata txdata-reliable rxdata range setpowerlevel txsignal pbrecord bbrecord npulses execcmd wakeup rs232_wakeup
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

txdata-reliable.o: samples/txdata-reliable.c unet.h fjage.h
	$(CC) $(CFLAGS) -c samples/txdata-reliable.c -o samples/txdata-reliable.o

txdata-reliable: txdata-reliable.o
	$(CC) -o samples/txdata-reliable samples/txdata-reliable.o libunet.a libfjage.a -lpthread -lm

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

execcmd.o: samples/execcmd.c unet.h fjage.h unet_ext.o
	$(CC) $(CFLAGS) -c samples/execcmd.c -o samples/execcmd.o

execcmd: execcmd.o unet_ext.o
	$(CC) -o samples/execcmd unet_ext.o samples/execcmd.o libunet.a libfjage.a -lpthread -lm

wakeup.o: samples/wakeup.c unet.h fjage.h unet_ext.o
	$(CC) $(CFLAGS) -c samples/wakeup.c -o samples/wakeup.o

wakeup: wakeup.o unet_ext.o
	$(CC) -o samples/wakeup unet_ext.o samples/wakeup.o libunet.a libfjage.a -lpthread -lm

rs232_wakeup.o: samples/rs232_wakeup.c unet.h fjage.h unet_ext.o
	$(CC) $(CFLAGS) -c samples/rs232_wakeup.c -o samples/rs232_wakeup.o

rs232_wakeup: rs232_wakeup.o unet_ext.o
	$(CC) -o samples/rs232_wakeup unet_ext.o samples/rs232_wakeup.o libunet.a libfjage.a -lpthread -lm

test: test_unet unet_ext.o
	rm -rf *.zip
	rm -rf fjage-*

test_unet.o: test/test_unet.c unet.h fjage.h unet_ext.o
	$(CC) $(CFLAGS) -c test/test_unet.c -o test/test_unet.o

test_unet: test_unet.o
	$(CC) -o test/test_unet unet_ext.o test/test_unet.o libunet.a libfjage.a -lpthread -lm

clean:
	rm -rf c-api libunet.a *.o samples/*.o test/*.o test/test_unet samples/wakeup samples/rs232_wakeup samples/txdata samples/txdata-reliable samples/rxdata samples/range samples/setpowerlevel samples/txsignal samples/pbrecord samples/bbrecordedsignal.txt samples/pbrecordedsignal.txt samples/bbrecord samples/npulses samples/gpio samples/execcmd
	rm -rf "$(FJAGE_DIR)" "$(FJAGE_VER).zip" fjage.h libfjage.a kissfft kiss_fft.h fjage
	rm -rf $(BUILD)

$(CONTRIB_DIR): $(BUILD_API)
	curl -LO https://github.com/org-arl/fjage/archive/$(FJAGE_VER).zip
	mv $(FJAGE_VER).zip $(BUILD)/
	cd $(BUILD) && unzip -a $(FJAGE_VER).zip
	mkdir -p $(BUILD_API)/libs/fjage/gateways
	cp -r $(BUILD)/$(FJAGE_DIR)/gateways/c $(BUILD_API)/libs/fjage/gateways/c
	rsync -av --exclude '$(BUILD)' . $(BUILD_API)
	rm -rf $(BUILD_API)/test
	cp Makefile.package $(BUILD_API)/Makefile
	rm $(BUILD_API)/Makefile.package
	cd $(BUILD_API) && zip -r ../c-api.zip .
	rm -rf $(BUILD_API)
	rm -rf $(BUILD)/$(FJAGE_DIR)
	rm -rf $(BUILD)/$(FJAGE_VER).zip

$(BUILD_API):
	mkdir -p $(BUILD_API)

package: $(CONTRIB_DIR)
