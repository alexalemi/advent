
PROBLEMS := $(patsubst %.nim,%.out,$(wildcard *.nim))

all: $(PROBLEMS)
.PHONY: all

%.out: *.nim
	nim c -d:release -o:$@ $<

