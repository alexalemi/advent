# DEVLOG

## 2023-01-03

Revisiting this challenge.  I wrote a dirty dissassembler in `diss.py` which
dumps the program in plain text as `challenge.txt`.  Doing this I can see that
there are only four locations in the program that checks register 7.

		00521: jt reg7 01093
		05451: jf reg7 05605
	  05522: set reg0 reg7
		06042: set reg1 reg7
	
So, this first instruction checks to see if reg7 is nonzero, and if so it jumps
to a part that dumps an error message that says `nonzero reg`, so that's the
instruction I'll have to dissable to start.

At this point we should be able to relaunch the runner, which I've done.

While that is running, let's look at the second place `reg7` is checked.

The nearby instructions are:

	05454: push reg0
	05456: push reg1
	05458: push reg2
	05460: set reg0 28844
	05463: set reg1 01531
	05466: add reg2 01977 09152
	05470: call 01458
	05472: pop reg2
	05474: pop reg1
	05476: pop reg0
	
Looks like we push `reg0`, `reg1` and `reg2` to the stack and then call 
a subroutine after setting:

	reg0 = 28844 = 12909!
  reg1 = 01531 = 2!
  reg2 = 01977 + 09152 = 8 + 20775 = 20783 
	
The subroutine seems to be:

		01457: halt 
		01458: push reg0
		01460: push reg3
		01462: push reg4
		01464: push reg5
		01466: push reg6
		01468: set reg6 reg0
		01471: set reg5 reg1
		01474: rmem reg4 reg0
		01477: set reg1 00000
		01480: add reg3 00001 reg1
		01484: gt reg0 reg3 reg4
		01488: jt reg0 01507
		01491: add reg3 reg3 reg6
		01495: rmem reg0 reg3
		01498: call reg5
		01500: add reg1 reg1 00001
		01504: jt reg1 01480
		01507: pop reg6
		01509: pop reg5
		01511: pop reg4
		01513: pop reg3
		01515: pop reg0
		01517: ret 

Actually, at this point, it looks like we need to execute the program and play the whole thing over again,
with something like

		python arch.py 1 < tape.in
		
it runs through everything and then prints:

		A strange, electronic voice is projected into your mind:

			"Unusual setting detected!  Starting confirmation process!  Estimated time to completion: 1 billion years."
			
So this is the part of the program I need to optimize.  I should figure out where that lives.

## 2022-11-03

I decided I might just as well run all of the different values that we could
place in the 8th register, so I created a little bash script that does that and
executed it with:

		bash runner.sh | tqdm --total 32765
		
So that I have a progress bar.  The script is running the virtual machine with
each of the possible values less than 32765 in the 8th register and piping the
output to `/tmp/synacor/$i.txt` so that I could parse those files after the
fact and find the good one.

The progress bar says it should take half an hour.

I could use something like this:

		md5sum * | datamash -W -s -g 1 count 2 -f
		
or:

		md5sum * | cut -f1 -d" " | sort | uniq
		
to see all of the unique hashes of the output, so far, about half way through,
they are all the same.

So, I ran through all of the samples and never got anywhere, they all return
the same thing which is that they get shutdown when they reach the self-check.
I'm probably gonna have to overwrite the selfcheck.

## 2021-12-23

Decided to start over and try to implement the VM in python this time.

That was pretty straight forward actually, got the thing working, now when I
finish the self-test I got another code: `QieZfTCocnNl`

It seems that this starts a sort of text-based game.  The first code was quite
easy, I simply `take tablet; use tablet` to get `eVtjKqYzXtQU`.

Seems like I could either go through the doorway or go south.
You can immediately pick up the tablet and then head to the doorway and into the cavern,
once you fall off the bridge if you go east you find an empty lantern.

Then we get lost in some twisty passages.

When the grue spots us, if we run and run again we get eaten. If we hide we get eaten.

So, we need something for our lantern.  If we go down the ladder we are in the twisty passageways,
the rooms all seem very similar but they are not exactly the same.  Mapping out the corridors and the names,
I was able to find a room with a `can` which powers our lantern so that we don't have to be afraid of the dark.

After that we can head back up and now go through the dark passageway which eventually leads us to a 
run down Ruins area. In the rooms we found 5 coins as well as an inscription:

    _ + _ * _ ^2 + _ ^3 - _ = 399
    
The coins we find are: 
 - a red coin with 2 dots
 - a blue coin with 9 dots
 - a shiny coin with a pentagon
 - a concave coin with 7 dots
 - and a corroded coin with a triangle

We can solve that formula with these numbers as:

  9 + 2 * 5 ^2 + 7 ^3 - 3 == 399
  
So presumably we can use the coins to unlock the locked door at the north end?

Yes, we use the coins in the right order and we unlock the next room, which has a teleporter.
Using the teleporter gives us our next code: `mgItDnwCCDxh`.

Inside the synacor headquarters I found a strange book:

		The cover of this book subtly swirls with colors. It is titled "A Brief
		Introduction to Interdimensional Physics". It reads: 

		Recent advances in interdimensional physics have produced fascinating
		predictions about the fundamentals of our universe! For example,
		interdimensional physics seems to predict that the universe is, at its root, a
		purely mathematical construct, and that all events are caused by the
		interactions between eight pockets of energy called "registers". Furthermore,
		it seems that while the lower registers primarily control mundane things like
		sound and light, the highest register (the so-called "eighth register") is used
		to control interdimensional events such as teleportation. 

		A hypothetical such teleportation device would need to have have exactly two
		destinations. One destination would be used when the eighth register is at its
		minimum energy level - this would be the default operation assuming the user
		has no way to control the eighth register. In this situation, the teleporter
		should send the user to a preconfigured safe location as a default. 

		The second destination, however, is predicted to require a very specific energy
		level in the eighth register. The teleporter must take great care to confirm
		that this energy level is exactly correct before teleporting its user! If it is
		even slightly off, the user would (probably) arrive at the correct location,
		but would briefly experience anomalies in the fabric of reality itself - this
		is, of course, not recommended. Any teleporter would need to test the energy
		level in the eighth register and abort teleportation if it is not exactly
		correct. 

		This required precision implies that the confirmation mechanism would be very
		computationally expensive. While this would likely not be an issue for large-
		scale teleporters, a hypothetical hand-held teleporter would take billions of
		years to compute the result and confirm that the eighth register is correct. 

		If you find yourself trapped in an alternate dimension with nothing but a
		hand-held teleporter, you will need to extract the confirmation algorithm,
		reimplement it on more powerful hardware, and optimize it. This should, at the
		very least, allow you to determine the value of the eighth register which would
		have been accepted by the teleporter's confirmation mechanism. 

		Then, set the eighth register to this value, activate the teleporter, and
		bypass the confirmation mechanism. If the eighth register is set correctly, no
		anomalies should be experienced, but beware - if it is set incorrectly, the
		now-bypassed confirmation mechanism will not protect you! 

		Of course, since teleportation is impossible, this is all totally ridiculous. 

Sounds like I need to disassemble my binary and figure out what the
confirmation run on the eighth register is.


## 2021-12-22

Mananged to get the self check passed but then I'm running into an invalid
ascii code somehow.  Must have some kind of subtle bug that isn't being picked
up by the tests.

I changed the registers to use indicies from 32768 to 32775.

Checking against a python implementation, it looks like my version should be working,
and it does for 299,438 steps, until something seems go wrong.

My register suddenly gets set to zero and I'm not sure why. We're near instruction 1730
where we are doing a rmem. In the implementation I'm checking against this works
but in my version this somehow has the memory set to zero at that point.



## 2021-12-21

Decided I'd finally download and take a look at this thing. Not sure which
language to use for this thing. Looks like we're going to be building a virtual
machine for a 15 bit language.  I'd normally use Nim for this kind of thing but
sorta wanted to try Zig, not sure though as that might be difficult just coming
in.

The first of the 8 codes was in the `arch-spec`: `HdwYeBxnKiIj`

I've decided to switch back to Nim. 

Started the implementation, going with a sort of basic design here. Gonna have
an object holding the registers, memory, a pointer to the current location, and
a clock.

After implementing the first few instructions got another code: `YurxnAikRjxu`

Now it looks like I have to implement more codes.  After implmenting some jump
codes it looks like I'm kind of stuck.

