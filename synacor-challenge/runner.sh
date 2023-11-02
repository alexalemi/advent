#!/bin/bash

for i in {1..32765}
do
	python arch.py $i < tape.in 2> /dev/null > /tmp/synacor/$i.txt
	SIZE=$(stat --printf="%s" /tmp/synacor/$i.txt)
	echo $i ":" $SIZE
done
