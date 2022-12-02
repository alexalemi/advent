# Advent of Code

This repository contains my solutions in several languages to [Advent Of Code](https://adventofcode.com).

![2015](./badges/img/2015.svg)
![2016](./badges/img/2016.svg)
![2017](./badges/img/2017.svg)
![2018](./badges/img/2018.svg)
![2019](./badges/img/2019.svg)
![2020](./badges/img/2020.svg)
![2021](./badges/img/2021.svg)
![2022](./badges/img/2022.svg)

## Getting an access token

https://github.com/wimglenn/advent-of-code-wim/issues/1

But basically, open the inspector, goto network and look for the Cookie.

This should be put into a local file `.token`.

## Utilities

There is `./utils.py` which will try to automatically fetch the current input, as
well as `utils.blj` which you can invoke as a babashka task:

	bb fetch
	
Since it won't ask for the input either too early or if it has already been grabbed you can use

	watch bb fetch
	
To automatically grab the input while you're reading the problem.
