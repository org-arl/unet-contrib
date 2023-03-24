CC = gcc
CFLAGS += -g -std=c99 -Wall -Wextra -Werror -Wfloat-equal -Wconversion -Wparentheses -pedantic -Wunused-parameter -Wunused-variable -Wreturn-type -Wno-unused-function -Wredundant-decls -Wreturn-type -Wunused-value -Wswitch-default -Wuninitialized -Winit-self -O2
FJAGE_VER=229b37c063eec87870cf129dbf94f766a97d0cb0
FJAGE_DIR=fjage-$(FJAGE_VER)
BUILD = build
BUILD_API = $(BUILD)/api
CONTRIB_DIR = $(BUILD)/temp

SAMPLE_SRC := $(wildcard samples/*.c)
SAMPLES_BIN := $(patsubst samples/%.c, samples/%, $(SAMPLE_SRC))

all: libs

samples: $(SAMPLES_BIN)

test: libs test/test_unet

libs: libfjage.a libunet.a

package: $(CONTRIB_DIR)

clean:
	rm -rf c-api libunet.a *.o samples/*.o test/*.o test/test_unet $(SAMPLES_BIN)
	rm -rf "$(FJAGE_DIR)" "$(FJAGE_VER).zip" fjage.h libfjage.a kissfft kiss_fft.h fjage
	rm -rf $(BUILD)

fjage.h: $(FJAGE_DIR)
	cp $(FJAGE_DIR)/gateways/c/fjage.h .

libfjage.a: $(FJAGE_DIR)
	make -C $(FJAGE_DIR)/gateways/c/
	cp $(FJAGE_DIR)/gateways/c/libfjage.a .

libunet.a: libfjage.a unet.o
	$(AR) rc $@ $^

$(FJAGE_DIR):
	curl -S -L -f -O https://github.com/org-arl/fjage/archive/$(FJAGE_VER).zip -C -
	unzip -an $(FJAGE_VER).zip

unet.o: unet.c unet.h fjage.h
	$(CC) $(CFLAGS) -c -o $@ $<

samples/%.o: samples/%.c unet.h fjage.h unet.o libfjage.a
	$(CC) $(CFLAGS) -c -o $@ $<

%.o: %.c unet.h fjage.h
	$(CC) $(CFLAGS) -c -o $@ $<

samples/%: samples/%.o unet_ext.o unet.o libfjage.a
	$(CC) -o $@ $< unet_ext.o unet.o libfjage.a -lm -lpthread

test/%: test/%.o unet_ext.o unet.o libfjage.a
	$(CC) -o $@ $< unet_ext.o unet.o libfjage.a -lm -lpthread

$(CONTRIB_DIR): $(BUILD_API)
	curl -LO https://github.com/org-arl/fjage/archive/$(FJAGE_VER).zip
	mv $(FJAGE_VER).zip $(BUILD)/
	cd $(BUILD) && unzip -a $(FJAGE_VER).zip
	mkdir -p $(BUILD_API)/libs/fjage/gateways
	cp -r $(BUILD)/$(FJAGE_DIR)/gateways/c $(BUILD_API)/libs/fjage/gateways/c
	rsync -av --exclude '$(BUILD)' --exclude $(FJAGE_DIR) . $(BUILD_API)
	rm -rf $(BUILD_API)/test
	cp Makefile.package $(BUILD_API)/Makefile
	rm $(BUILD_API)/Makefile.package
	cd $(BUILD_API) && zip -r ../c-api.zip .
	rm -rf $(BUILD_API)
	rm -rf $(BUILD)/$(FJAGE_DIR)
	rm -rf $(BUILD)/$(FJAGE_VER).zip

$(BUILD_API):
	mkdir -p $(BUILD_API)

.PHONY: all samples test libs package clean