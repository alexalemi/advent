CC = gcc
CFLAGS = -O3 -march=native -Wall

SRCS = $(wildcard p??.c)
BINS = $(SRCS:.c=)

all: $(BINS)

# p12 needs OpenMP
p12: p12.c
	$(CC) $(CFLAGS) -fopenmp -o $@ $<

%: %.c
	$(CC) $(CFLAGS) -o $@ $<

clean:
	rm -f $(BINS)

# Generate solution images (converts PPM to PNG)
imgs: p12
	./p12
	@echo "Converting PPM to PNG..."
	@for f in imgs/*.ppm; do \
		convert "$$f" "$${f%.ppm}.png" && rm "$$f"; \
	done
	@echo "Generated $$(ls imgs/*.png | wc -l) images"

.PHONY: all clean imgs
