import collections
import functools
import dataclasses
from typing import Dict, FrozenSet, Sequence
import string

import library
import p18
from utils import data19

data = data19(20)

Location = complex


@functools.total_ordering
@dataclasses.dataclass(eq=True, frozen=True)
class World:
    loc: Location
    end: Location
    moves: int
    walls: FrozenSet[Location]
    portals: Dict[Location, Location]

    def __lt__(self, other):
        return self.moves < other.moves

    def __hash__(self):
        return hash(self.loc)


def render(world, inp):
    board = p18.process(inp)
    board[world.loc] = "@"
    bounds = library.get_bounds(board)
    library.render(bounds, board, default=" ")
    print(f"Moves = {world.moves}")


def make(inp: str) -> World:
    board = p18.process(inp)
    moves = 0
    walls = frozenset(loc for loc, c in board.items() if c != ".")
    letter_locs = (loc for loc, c in board.items() if c in set(string.ascii_uppercase))
    portal_locs = collections.defaultdict(list)
    for loc in letter_locs:
        # test if this one is valid
        for n in library.neighbors(loc):
            if board.get(n) == ".":
                other_loc = loc - (n - loc)
                portal_name = "".join(
                    board[complex(*x)]
                    for x in sorted(map(library.wrap, (loc, other_loc)))
                )
                portal_locs[portal_name].append(n)
    loc = portal_locs.pop("AA")[0]
    end = portal_locs.pop("ZZ")[0]
    portals = {}
    for p, (a, b) in portal_locs.items():
        portals[a] = b
        portals[b] = a
    return World(loc, end, moves, walls, portals)


tests = [
    (
        """         A           
         A           
  #######.#########  
  #######.........#  
  #######.#######.#  
  #######.#######.#  
  #######.#######.#  
  #####  B    ###.#  
BC...##  C    ###.#  
  ##.##       ###.#  
  ##...DE  F  ###.#  
  #####    G  ###.#  
  #########.#####.#  
DE..#######...###.#  
  #.#########.###.#  
FG..#########.....#  
  ###########.#####  
             Z       
             Z       """,
        23,
    ),
    (
        """                   A               
                   A               
  #################.#############  
  #.#...#...................#.#.#  
  #.#.#.###.###.###.#########.#.#  
  #.#.#.......#...#.....#.#.#...#  
  #.#########.###.#####.#.#.###.#  
  #.............#.#.....#.......#  
  ###.###########.###.#####.#.#.#  
  #.....#        A   C    #.#.#.#  
  #######        S   P    #####.#  
  #.#...#                 #......VT
  #.#.#.#                 #.#####  
  #...#.#               YN....#.#  
  #.###.#                 #####.#  
DI....#.#                 #.....#  
  #####.#                 #.###.#  
ZZ......#               QG....#..AS
  ###.###                 #######  
JO..#.#.#                 #.....#  
  #.#.#.#                 ###.#.#  
  #...#..DI             BU....#..LF
  #####.#                 #.#####  
YN......#               VT..#....QG
  #.###.#                 #.###.#  
  #.#...#                 #.....#  
  ###.###    J L     J    #.#.###  
  #.....#    O F     P    #.#...#  
  #.###.#####.#.#####.#####.###.#  
  #...#.#.#...#.....#.....#.#...#  
  #.#####.###.###.#.#.#########.#  
  #...#.#.....#...#.#.#.#.....#.#  
  #.###.#####.###.###.#.#.#######  
  #.#.........#...#.............#  
  #########.###.###.#############  
           B   J   C               
           U   P   P               """,
        58,
    ),
]


def gen_neighbors(world):
    def neighbors(x):
        x = library.unwrap(x)
        for n in library.neighbors(x):
            if n not in world.walls:
                yield library.wrap(n)
        if x in world.portals:
            yield library.wrap(world.portals[x])

    return neighbors


def answer1(inp):
    world = make(inp)
    path = library.astar(
        start=library.wrap(world.loc),
        goal=lambda x: x == library.wrap(world.end),
        cost=lambda x, y: 1,
        neighbors=gen_neighbors(world),
        heuristic=lambda x: library.distance(library.unwrap(x), world.end),
    )
    return len(path) - 1


tests2 = [
    (
        """         A           
         A           
  #######.#########  
  #######.........#  
  #######.#######.#  
  #######.#######.#  
  #######.#######.#  
  #####  B    ###.#  
BC...##  C    ###.#  
  ##.##       ###.#  
  ##...DE  F  ###.#  
  #####    G  ###.#  
  #########.#####.#  
DE..#######...###.#  
  #.#########.###.#  
FG..#########.....#  
  ###########.#####  
             Z       
             Z       """,
        26,
    ),
    (
        """             Z L X W       C                 
             Z P Q B       K                 
  ###########.#.#.#.#######.###############  
  #...#.......#.#.......#.#.......#.#.#...#  
  ###.#.#.#.#.#.#.#.###.#.#.#######.#.#.###  
  #.#...#.#.#...#.#.#...#...#...#.#.......#  
  #.###.#######.###.###.#.###.###.#.#######  
  #...#.......#.#...#...#.............#...#  
  #.#########.#######.#.#######.#######.###  
  #...#.#    F       R I       Z    #.#.#.#  
  #.###.#    D       E C       H    #.#.#.#  
  #.#...#                           #...#.#  
  #.###.#                           #.###.#  
  #.#....OA                       WB..#.#..ZH
  #.###.#                           #.#.#.#  
CJ......#                           #.....#  
  #######                           #######  
  #.#....CK                         #......IC
  #.###.#                           #.###.#  
  #.....#                           #...#.#  
  ###.###                           #.#.#.#  
XF....#.#                         RF..#.#.#  
  #####.#                           #######  
  #......CJ                       NM..#...#  
  ###.#.#                           #.###.#  
RE....#.#                           #......RF
  ###.###        X   X       L      #.#.#.#  
  #.....#        F   Q       P      #.#.#.#  
  ###.###########.###.#######.#########.###  
  #.....#...#.....#.......#...#.....#.#...#  
  #####.#.###.#######.#######.###.###.#.#.#  
  #.......#.......#.#.#.#.#...#...#...#.#.#  
  #####.###.#####.#.#.#.#.###.###.#.###.###  
  #.......#.....#.#...#...............#...#  
  #############.#.#.###.###################  
               A O F   N                     
               A A D   M                     """,
        396,
    ),
]


def gen_neighbors2(world):
    bounds = library.get_bounds(world.walls)
    center = complex((bounds.xmax + bounds.xmin) / 2, (bounds.ymin + bounds.ymax) / 2)

    def distance(x, y):
        return max(abs(x.real - y.real), abs(x.imag - y.imag))

    def neighbors(y):
        lvl, x = y
        x = library.unwrap(x)
        for n in library.neighbors(x):
            if n not in world.walls:
                yield (lvl, library.wrap(n))
        if x in world.portals:
            new = world.portals[x]
            if distance(new, center) < distance(x, center):
                # outer
                if lvl > 0:
                    yield (lvl - 1, library.wrap(new))
            else:
                yield (lvl + 1, library.wrap(new))

    return neighbors


def answer2(inp):
    world = make(inp)
    path = library.astar(
        start=(0, library.wrap(world.loc)),
        goal=lambda x: x == (0, library.wrap(world.end)),
        cost=lambda x, y: 1,
        neighbors=gen_neighbors2(world),
        heuristic=lambda x: x[0] + library.distance(library.unwrap(x[1]), world.end),
    )
    return len(path) - 1


if __name__ == "__main__":
    for inp, ans in tests:
        myans = answer1(inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
    ans1 = answer1(data)
    print("Answer1:", ans1)

    for inp, ans in tests2:
        myans = answer2(inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

    ans2 = answer2(data)
    print("Answer2:", ans2)
