CC = gcc
CGLAGS = $(CGLAGS) -std=c99 -Wall -Wextra -Werror -O2
FJAGE_VER=c8e62495f2283d1b57a559112ccce3ccd608bb2a
FJAGE_DIR=fjage-$(FJAGE_VER)

all: libs
	rm -rf *.zip
	rm -rf fjage-*

samples: libs txdata rxdata
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

txdata: libs txdata.o
	$(CC) -o samples/txdata samples/txdata.o libunet.a libfjage.a -lpthread -lm

rxdata.o: samples/rxdata.c unet.h fjage.h
	$(CC) $(CFLAGS) -c samples/rxdata.c -o samples/rxdata.o

rxdata: libs rxdata.o
	$(CC) -o samples/rxdata samples/rxdata.o libunet.a libfjage.a -lpthread -lm

test: libs test_unet
	rm -rf *.zip
	rm -rf fjage-*

test_unet.o: test/test_unet.c unet.h fjage.h
	$(CC) $(CFLAGS) -c test/test_unet.c -o test/test_unet.o

test_unet: libs test_unet.o
	$(CC) -o test/test_unet test/test_unet.o libunet.a libfjage.a -lpthread -lm

clean:
	rm -rf c-api libunet.a *.o samples/*.o test/*.o test/test_unet samples/wakeup samples/rs232_wakeup samples/txdata samples/rxdata
	rm -rf "$(FJAGE_DIR)" "$(FJAGE_VER).zip" fjage.h libfjage.a kissfft kiss_fft.h fjage
