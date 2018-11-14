CFLAGS=-std=c99 -Wall -O2

all: libfjage.a libunet.a test

test: test.o libfjage.a libunet.a
	$(CC) -o test test.o libunet.a libfjage.a -lpthread -lm

clean:
	rm -rf libunet.a *.o test fjage-dev dev.zip fjage.h libfjage.a

libfjage.a: fjage-dev
	make -C fjage-dev/src/main/c/
	cp fjage-dev/src/main/c/fjage.h .
	cp fjage-dev/src/main/c/libfjage.a .

libunet.a: unet.o
	$(AR) rc $@ $^

fjage-dev:
	curl -LO https://github.com/org-arl/fjage/archive/dev.zip
	unzip -a dev.zip

%.o: %.c unet.h fjage.h
	$(CC) $(CFLAGS) -c $< -o $@

.PHONY: all clean
