# Getting an access token

https://github.com/wimglenn/advent-of-code-wim/issues/1

But basically, open the inspector, goto network and look for the Cookie.

This should be put into a local file `.token`.

There is `./utils.py` which will try to automatically fetch the current input, as
well as `utils.blj` which you can invoke as a babashka task:

	bb fetch
