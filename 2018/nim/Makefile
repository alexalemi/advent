
PROBLEMS := $(patsubst %.nim,%.out,$(wildcard *.nim))

all: $(PROBLEMS)
.PHONY: all runall

runall: $(PROBLEMS)
	for x in $(PROBLEMS); do ./$$x; done;

%.out: %.nim
	nim c -d:release -o:$@ $<

