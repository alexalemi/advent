# Advent of Code

This repository contains my solutions in several languages to [Advent Of Code](https://adventofcode.com).

![2015](./scripts/img/2015.svg)
![2016](./scripts/img/2016.svg)
![2017](./scripts/img/2017.svg)
![2018](./scripts/img/2018.svg)
![2019](./scripts/img/2019.svg)
![2020](./scripts/img/2020.svg)
![2021](./scripts/img/2021.svg)
![2022](./scripts/img/2022.svg)

![Total](./scripts/img/Total.svg)

## Getting an access token

https://github.com/wimglenn/advent-of-code-wim/issues/1

But basically, open the inspector, goto network and look for the Cookie.

This should be put into a local file `.token`.

## Utilities

There is [`scripts/fetch.clj`](scripts/fetch.clj) which will try to automatically fetch the current input, as which you can invoke as a babashka task:

	bb fetch
	
Since it won't ask for the input either too early or if it has already been grabbed you can use

	watch bb fetch
	
To automatically grab the input while you're reading the problem.

There is also a task to update the badges [`scripts/badges.clj`](scripts/badges.clj) for the readme with:

	bb badges

which was forked from [genmeblog](https://github.com/genmeblog/advent-of-code/blob/master/badges/badges.bb).
	

