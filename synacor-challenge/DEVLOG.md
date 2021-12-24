# DEVLOG

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

