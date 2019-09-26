CGLAGS = $(CGLAGS) -std=c99 -Wall -Wextra -Werror -O2
FJAGE_VER=edc9ecffd444adaf789d7eb2ecc650d1d7044d84
FJAGE_DIR=fjage-$(FJAGE_VER)

all: libs wakeup rs232_wakeup txdata rxdata

libs: libfjage.a libunet.a

libfjage.a: $(FJAGE_DIR)
	make -C $(FJAGE_DIR)/src/main/c/
	cp $(FJAGE_DIR)/src/main/c/fjage.h .
	cp $(FJAGE_DIR)/src/main/c/libfjage.a .

$(FJAGE_DIR):
# 	curl -LO https://github.com/org-arl/fjage/archive/v$(FJAGE_VER).zip
	curl -LO https://github.com/org-arl/fjage/archive/$(FJAGE_VER).zip
# 	unzip -a v$(FJAGE_VER).zip
	unzip -a $(FJAGE_VER).zip

libunet.a: unet.o
	$(AR) rc $@ $^

%.o: %.c unet.h fjage.h
	$(CC) $(CFLAGS) -c $< -o $@

test_unet: libs test_unet.o
	$(CC) -o test_unet test_unet.o libunet.a libfjage.a -lpthread -lm

wakeup: libs wakeup.o
	$(CC) -o wakeup wakeup.o libunet.a libfjage.a -lpthread -lm

rs232_wakeup: libs rs232_wakeup.o
	$(CC) -o rs232_wakeup rs232_wakeup.o libunet.a libfjage.a -lpthread -lm

txdata: libs txdata.o
	$(CC) -o txdata txdata.o libunet.a libfjage.a -lpthread -lm

rxdata: libs rxdata.o
	$(CC) -o rxdata rxdata.o libunet.a libfjage.a -lpthread -lm

test: libs test_unet
	./test_unet ${IP} ${ADDR} ${DEVICE}

deepclean: clean
	rm -rf "$(FJAGE_DIR)" "$(FJAGE_VER).zip" fjage.h libfjage.a kissfft kiss_fft.h

clean:
	rm -rf c-api libunet.a *.o test_unet wakeup rs232_wakeup txdata rxdata txandrecord recfile.txt txtimes.txt


api-bundle: libs
	mkdir -p c-api/kissfft
	mkdir -p c-api/$(FJAGE_DIR)/src/main/c
	cp *.{c,h} c-api/
	cp Makefile c-api/

	cp kissfft/*.{c,h} c-api/kissfft
	cp kissfft/Makefile c-api/kissfft/Makefile

	cp $(FJAGE_DIR)/src/main/c/*.{c,h}	c-api/$(FJAGE_DIR)/src/main/c/
	cp $(FJAGE_DIR)/src/main/c/Makefile	c-api/$(FJAGE_DIR)/src/main/c/Makefile
	zip -r c-api.zip c-api/*
	rm -r c-api

.PHONY: all clean
