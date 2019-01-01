
PROBLEMS := $(patsubst %.nim,%,$(wildcard *.nim))

all: $(PROBLEMS)
.PHONY: all

%: *.nim
	nim c -o:$@ $<

