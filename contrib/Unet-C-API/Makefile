CFLAGS=-std=c99 -Wall -O2

all: libfjage.a libunet.a

libfjage.a: fjage-dev
	make -C fjage-dev/src/main/c/
	cp fjage-dev/src/main/c/fjage.h .
	cp fjage-dev/src/main/c/libfjage.a .

fjage-dev:
	curl -LO https://github.com/org-arl/fjage/archive/dev.zip
	unzip -a dev.zip

libunet.a: unet.o
	$(AR) rc $@ $^

%.o: %.c unet.h fjage.h
	$(CC) $(CFLAGS) -c $< -o $@

test_unet: test_unet.o libunet.a libfjage.a
	$(CC) -o test_unet test_unet.o libunet.a libfjage.a -lpthread

test: libfjage.a test_unet
	./test_unet ${IP} ${ADDR} ${DEVICE}

clean:
	rm -rf libunet.a *.o test_unet fjage-dev dev.zip fjage.h libfjage.a

.PHONY: all clean
