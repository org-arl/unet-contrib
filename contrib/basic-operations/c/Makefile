CFLAGS=-std=c99 -Wall -O2

all: examples

examples: examples.o lib/libunet.a lib/libfjage.a
	$(CC) -o examples examples.o lib/libunet.a lib/libfjage.a -lpthread

test: lib/libfjage.a examples examples.c
	./examples ${IP}

clean:
	rm -rf *.o examples

.PHONY: all clean
