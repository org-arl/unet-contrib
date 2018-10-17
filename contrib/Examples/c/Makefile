CFLAGS=-std=c99 -Wall -O2

all: libfjage.a libunet.a

examples: examples.o libunet.a libfjage.a
	$(CC) -o examples examples.o libunet.a libfjage.a -lpthread

test: libfjage.a examples examples.c
	./examples ${IP}

clean:
	rm -rf *.o examples

.PHONY: all clean
