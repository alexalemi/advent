
CC=clang
CFLAGS = -Wall -Wextra -Wpedantic -Werror -Wshadow -fno-strict-aliasing -march=native -std=c11 -O2 -Os

SOURCES = $(wildcard p[0-9][0-9].c)
EXECUTABLES = $(SOURCES:.c=)

all: $(EXECUTABLES)

p??: p??.c
	$(CC) $(CFLAGS) -o $@ $<

clean:
	rm -rf $(EXECUTABLES)
