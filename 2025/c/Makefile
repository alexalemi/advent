CC = gcc
CFLAGS = -O3 -march=native -Wall

SRCS = $(wildcard p??.c)
BINS = $(SRCS:.c=)

all: $(BINS)

%: %.c
	$(CC) $(CFLAGS) -o $@ $<

clean:
	rm -f $(BINS)

.PHONY: all clean
