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
	
Since it won't ask for the input either too early or if it has already been grabbed you can use

	watch bb fetch
	
To automatically grab the input while you're reading the problem.
	
## Solution Map

Generated with:

		tree -P 'p??*' -L 3 --prune > .solution-map

```
.
├── 2015
│   ├── clojure
│   │   ├── p06.clj
│   │   ├── p07.clj
│   │   ├── p08.clj
│   │   ├── p09.clj
│   │   ├── p10.clj
│   │   ├── p11.clj
│   │   ├── p12.clj
│   │   ├── p13.clj
│   │   ├── p14.clj
│   │   ├── p15.clj
│   │   ├── p16.clj
│   │   ├── p17.clj
│   │   ├── p18.clj
│   │   ├── p19.clj
│   │   ├── p20.clj
│   │   ├── p21.clj
│   │   ├── p22.clj
│   │   ├── p23.clj
│   │   ├── p24.clj
│   │   └── p25.clj
│   └── py
│       ├── p01.py
│       ├── p02.py
│       ├── p03.py
│       ├── p04.py
│       ├── p05.py
│       └── p07.py
├── 2016
│   ├── clojure
│   │   ├── p05.clj
│   │   ├── p06.clj
│   │   ├── p07.clj
│   │   ├── p08.clj
│   │   ├── p09.clj
│   │   ├── p10.clj
│   │   ├── p11.clj
│   │   ├── p11.old.clj
│   │   ├── p12.clj
│   │   ├── p13.clj
│   │   ├── p14.clj
│   │   ├── p15.clj
│   │   ├── p16.clj
│   │   ├── p17.clj
│   │   ├── p18.clj
│   │   ├── p19.clj
│   │   ├── p20.clj
│   │   ├── p21.clj
│   │   ├── p22.clj
│   │   ├── p23.clj
│   │   ├── p24.clj
│   │   └── p25.clj
│   ├── nim
│   │   └── p05.nim
│   └── py
│       ├── p01.hy
│       ├── p01.py
│       ├── p02.py
│       ├── p03.py
│       ├── p04.py
│       └── p05.py
├── 2017
│   └── clojure
│       ├── p01.clj
│       ├── p02.clj
│       ├── p03.clj
│       ├── p04.clj
│       ├── p05.clj
│       ├── p06.clj
│       ├── p07.clj
│       ├── p08.clj
│       ├── p09.clj
│       ├── p10.clj
│       ├── p11.clj
│       ├── p12.clj
│       ├── p13.clj
│       ├── p14.clj
│       ├── p15.clj
│       ├── p16.clj
│       ├── p17.clj
│       ├── p18.clj
│       ├── p19.clj
│       ├── p20.clj
│       ├── p21.clj
│       ├── p22.clj
│       ├── p23.clj
│       ├── p24.clj
│       ├── p25.clj
│       └── project.clj
├── 2018
│   ├── clojure
│   │   ├── p15.clj
│   │   ├── p16.clj
│   │   └── p17.clj
│   ├── js
│   │   └── p14.ts
│   ├── nim
│   │   ├── p01.nim
│   │   ├── p02.nim
│   │   ├── p03.nim
│   │   ├── p04.nim
│   │   ├── p05.nim
│   │   ├── p06.nim
│   │   └── p15.nim
│   └── py
│       ├── p01.py
│       ├── p02.py
│       ├── p03.py
│       ├── p04.py
│       ├── p05.py
│       ├── p06.py
│       ├── p07.py
│       ├── p08.py
│       ├── p09.py
│       ├── p10.py
│       ├── p11.py
│       ├── p12.py
│       ├── p13.py
│       ├── p14.py
│       └── p15.py
├── 2019
│   ├── js
│   │   ├── p01.js
│   │   └── p06.js
│   ├── nim
│   │   ├── p01.nim
│   │   ├── p02.nim
│   │   ├── p03.nim
│   │   ├── p04.nim
│   │   ├── p05.nim
│   │   ├── p07.nim
│   │   └── p12.nim
│   └── py
│       ├── p01.py
│       ├── p02.py
│       ├── p03.py
│       ├── p04.py
│       ├── p05.py
│       ├── p06.py
│       ├── p07.py
│       ├── p08.py
│       ├── p09.py
│       ├── p10.py
│       ├── p11.py
│       ├── p12.py
│       ├── p13.py
│       ├── p14.py
│       ├── p15.py
│       ├── p16.py
│       ├── p17.py
│       ├── p18.py
│       ├── p19.py
│       ├── p20.py
│       ├── p21.py
│       ├── p22.py
│       ├── p23.py
│       ├── p24.py
│       └── p25.py
├── 2020
│   ├── hy
│   │   └── p01.hy
│   ├── js
│   │   ├── p01.bun.ts
│   │   ├── p01.ts
│   │   └── p02.ts
│   ├── julia
│   │   └── p01.jl
│   ├── nim
│   │   ├── p01.nim
│   │   ├── p15.nim
│   │   └── p23.nim
│   └── py
│       ├── p01.py
│       ├── p02.py
│       ├── p03.py
│       ├── p04.py
│       ├── p05.py
│       ├── p06.py
│       ├── p07.py
│       ├── p08.py
│       ├── p09.py
│       ├── p10.py
│       ├── p11.py
│       ├── p12.py
│       ├── p13.py
│       ├── p14.py
│       ├── p15.py
│       ├── p16.py
│       ├── p17.py
│       ├── p18.py
│       ├── p19.py
│       ├── p20.py
│       ├── p21.py
│       ├── p22.py
│       ├── p23.py
│       ├── p24.py
│       ├── p25.py
│       ├── parser.py
│       └── pga2d.py
├── 2021
│   ├── clojure
│   │   ├── p01.clj
│   │   ├── p02.clj
│   │   ├── p03.clj
│   │   ├── p04.clj
│   │   ├── p05.clj
│   │   ├── p06.clj
│   │   ├── p07.clj
│   │   ├── p08.clj
│   │   ├── p09.clj
│   │   ├── p10.clj
│   │   ├── p11.clj
│   │   ├── p12.clj
│   │   ├── p13.clj
│   │   ├── p14.clj
│   │   ├── p15.clj
│   │   ├── p16.clj
│   │   ├── p17.clj
│   │   ├── p18.clj
│   │   ├── p19.clj
│   │   ├── p20.clj
│   │   ├── p21.clj
│   │   ├── p22.clj
│   │   ├── p23.clj
│   │   ├── p24.clj
│   │   └── p25.clj
│   ├── fennel
│   │   └── p01.fnl
│   └── py
│       └── p01.py
└── 2022
    └── clojure
        └── p01.clj

30 directories, 204 files
```
