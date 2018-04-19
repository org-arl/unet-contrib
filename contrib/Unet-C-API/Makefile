CFLAGS=-std=c99 -Wall -O2

all: libfjage.a libunet.a

libfjage.a:
	curl -LO https://github.com/org-arl/fjage/archive/v1.4.2.zip
	unzip -a v1.4.2.zip
	make -C fjage-1.4.2/src/main/c/
	cp fjage-1.4.2/src/main/c/fjage.h .
	cp fjage-1.4.2/src/main/c/libfjage.a .

libunet.a: unet.o
	$(AR) rc $@ $^

%.o: %.c unet.h fjage.h
	$(CC) $(CFLAGS) -c $< -o $@

test_unet: test_unet.o libunet.a libfjage.a
	$(CC) -o test_unet test_unet.o libunet.a libfjage.a -lpthread

test: libfjage.a test_unet
	./test_unet ${IP} ${ADDR}

clean:
	rm -rf libunet.a *.o test_unet fjage-1.4.2 v1.4.2.zip fjage.h libfjage.a

.PHONY: all clean
