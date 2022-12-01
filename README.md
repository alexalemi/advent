# Advent of Code

This repository contains my solutions in several languages to [Advent Of Code](https://adventofcode.com).

## Getting an access token

https://github.com/wimglenn/advent-of-code-wim/issues/1

But basically, open the inspector, goto network and look for the Cookie.

This should be put into a local file `.token`.

## Utilities

There is `./utils.py` which will try to automatically fetch the current input, as
well as `utils.blj` which you can invoke as a babashka task:

	bb fetch
