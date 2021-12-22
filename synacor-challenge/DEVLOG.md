# DEVLOG

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

## 2021-12-22

Mananged to get the self check passed but then I'm running into an invalid
ascii code somehow.  Must have some kind of subtle bug that isn't being picked
up by the tests.

I changed the registers to use indicies from 32768 to 32775.
