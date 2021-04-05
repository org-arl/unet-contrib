CC = gcc
CFLAGS += -std=c99 -Wall -Wextra -Werror -Wfloat-equal -Wconversion -Wparentheses -pedantic -Wunused-parameter -Wunused-variable -Wreturn-type -Wno-unused-function -Wredundant-decls -Wreturn-type -Wunused-value -Wswitch-default -Wuninitialized -Winit-self -O2
# FJAGE_VER=c8e62495f2283d1b57a559112ccce3ccd608bb2a
FJAGE_VER=master
FJAGE_DIR=fjage-$(FJAGE_VER)

all: libs
	rm -rf *.zip
	rm -rf fjage-*

samples: txdata rxdata range setpowerlevel txsignal pbrecord bbrecord npulses wakeup rs232_wakeup
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

txdata.o: samples/txdata.c unet.h fjage.h
	$(CC) $(CFLAGS) -c samples/txdata.c -o samples/txdata.o

txdata: txdata.o
	$(CC) -o samples/txdata samples/txdata.o libunet.a libfjage.a -lpthread -lm

rxdata.o: samples/rxdata.c unet.h fjage.h
	$(CC) $(CFLAGS) -c samples/rxdata.c -o samples/rxdata.o

rxdata: rxdata.o
	$(CC) -o samples/rxdata samples/rxdata.o libunet.a libfjage.a -lpthread -lm

range.o: samples/range.c unet.h fjage.h
	$(CC) $(CFLAGS) -c samples/range.c -o samples/range.o

range: range.o
	$(CC) -o samples/range samples/range.o libunet.a libfjage.a -lpthread -lm

setpowerlevel.o: samples/setpowerlevel.c unet.h fjage.h
	$(CC) $(CFLAGS) -c samples/setpowerlevel.c -o samples/setpowerlevel.o

setpowerlevel: setpowerlevel.o
	$(CC) -o samples/setpowerlevel samples/setpowerlevel.o libunet.a libfjage.a -lpthread -lm

txsignal.o: samples/txsignal.c unet.h fjage.h
	$(CC) $(CFLAGS) -c samples/txsignal.c -o samples/txsignal.o

txsignal: txsignal.o
	$(CC) -o samples/txsignal samples/txsignal.o libunet.a libfjage.a -lpthread -lm

pbrecord.o: samples/pbrecord.c unet.h fjage.h
	$(CC) $(CFLAGS) -c samples/pbrecord.c -o samples/pbrecord.o

pbrecord: pbrecord.o
	$(CC) -o samples/pbrecord samples/pbrecord.o libunet.a libfjage.a -lpthread -lm

bbrecord.o: samples/bbrecord.c unet.h fjage.h
	$(CC) $(CFLAGS) -c samples/bbrecord.c -o samples/bbrecord.o

bbrecord: bbrecord.o
	$(CC) -o samples/bbrecord samples/bbrecord.o libunet.a libfjage.a -lpthread -lm

npulses.o: samples/npulses.c unet.h fjage.h
	$(CC) $(CFLAGS) -c samples/npulses.c -o samples/npulses.o

npulses: npulses.o
	$(CC) -o samples/npulses samples/npulses.o libunet.a libfjage.a -lpthread -lm

wakeup.o: samples/wakeup.c unet.h fjage.h
	$(CC) $(CFLAGS) -c samples/wakeup.c -o samples/wakeup.o

wakeup: wakeup.o
	$(CC) -o samples/wakeup samples/wakeup.o libunet.a libfjage.a -lpthread -lm

rs232_wakeup.o: samples/rs232_wakeup.c unet.h fjage.h
	$(CC) $(CFLAGS) -c samples/rs232_wakeup.c -o samples/rs232_wakeup.o

rs232_wakeup: rs232_wakeup.o
	$(CC) -o samples/rs232_wakeup samples/rs232_wakeup.o libunet.a libfjage.a -lpthread -lm

test: test_unet
	rm -rf *.zip
	rm -rf fjage-*

test_unet.o: test/test_unet.c unet.h fjage.h
	$(CC) $(CFLAGS) -c test/test_unet.c -o test/test_unet.o

test_unet: test_unet.o
	$(CC) -o test/test_unet test/test_unet.o libunet.a libfjage.a -lpthread -lm

clean:
	rm -rf c-api libunet.a *.o samples/*.o test/*.o test/test_unet samples/wakeup samples/rs232_wakeup samples/txdata samples/rxdata samples/range samples/setpowerlevel samples/txsignal samples/pbrecord samples/recordedsignal.txt samples/bbrecord samples/npulses
	rm -rf "$(FJAGE_DIR)" "$(FJAGE_VER).zip" fjage.h libfjage.a kissfft kiss_fft.h fjage
