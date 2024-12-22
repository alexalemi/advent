"""Tests for the intcode computer."""

import intcode
import itertools
import unittest
from utils import data19


def runner(string, *inps):
    return intcode.Computer(string).run(*inps)


class TestComputer(unittest.TestCase):
    def test_day2tests(self):
        cases = [
            ("1,9,10,3,2,3,11,0,99,30,40,50", 3500),
            ("1,0,0,0,99", 2),
            ("2,3,0,3,99", 2),
            ("2,4,4,5,99,0", 2),
            ("1,1,1,4,99,5,6,0,99", 30),
        ]

        def run(program_string):
            prog = intcode.getcodes(program_string)
            computer = intcode.Computer(prog)
            computer.run()
            return computer.codes[0]

        for prog, ans in cases:
            self.assertEqual(run(prog), ans)

    def _run2(self, noun, verb):
        data = data19(2)
        prog = intcode.getcodes(data)
        prog[1] = noun
        prog[2] = verb
        computer = intcode.Computer(prog)
        computer.run()
        return computer.codes[0]

    def test_day2(self):
        self.assertEqual(self._run2(12, 2), 5110675)

    def test_day2_part2(self):
        self.assertEqual(self._run2(48, 47), 19690720)

    def test_day5_part1(self):
        codes = intcode.getcodes(data19(5))
        out = runner(codes, 1)
        self.assertTrue(all([x == 0 for x in out[:-1]]))
        self.assertEqual(out[-1], 13933662)

    def test_day5_part2(self):
        codes = intcode.getcodes(data19(5))
        out = runner(codes, 5)
        self.assertEqual(out, [2369720])

    def test_day5tests(self):
        inps = [1, 2342, 7, 8, 9, -10, 0, -1, 500]

        def bigex(x):
            if x < 8:
                return 999
            elif x == 8:
                return 1000
            else:
                return 1001

        testfuncs = {
            (lambda x: 1 * (x == 8)): [
                "3,9,8,9,10,9,4,9,99,-1,8",
                "3,3,1108,-1,8,3,4,3,99",
            ],
            (lambda x: 1 * (x < 8)): [
                "3,9,7,9,10,9,4,9,99,-1,8",
                "3,3,1107,-1,8,3,4,3,99",
            ],
            (lambda x: 1 * (x != 0)): [
                "3,12,6,12,15,1,13,14,13,4,13,99,-1,0,1,9",
                "3,3,1105,-1,9,1101,0,0,12,4,12,99,1",
            ],
            bigex: [
                "3,21,1008,21,8,20,1005,20,22,107,8,21,20,1006,20,31,1106,0,36,98,0,0,1002,21,125,20,4,20,1105,1,46,104,999,1105,1,46,1101,1000,1,20,4,20,1105,1,46,98,99",
            ],
        }

        for i, (func, progs) in enumerate(testfuncs.items()):
            for j, prog in enumerate(progs):
                codes = intcode.getcodes(prog)
                for inp in inps:
                    self.assertEqual(runner(codes[:], inp), [func(inp)])

    def _day5_part1(self, inp):
        prog, inps = inp
        prog = intcode.getcodes(prog)
        inps = intcode.getcodes(inps)
        out = 0
        for i, inp in enumerate(inps):
            outs = runner(prog[:], inp, out)
            out = outs[0]
        return out

    def test_day5_tests(self):
        tests = [
            (("3,15,3,16,1002,16,10,16,1,16,15,15,4,15,99,0,0", "4,3,2,1,0"), 43210),
            (
                (
                    "3,23,3,24,1002,24,10,24,1002,23,-1,23,101,5,23,23,1,24,23,23,4,23,99,0,0",
                    "0,1,2,3,4",
                ),
                54321,
            ),
            (
                (
                    "3,31,3,32,1002,32,10,32,1001,31,-2,31,1007,31,0,33,1002,33,7,33,1,33,31,31,1,32,31,31,4,31,99,0,0,0",
                    "1,0,4,3,2",
                ),
                65210,
            ),
        ]
        for case, ans in tests:
            self.assertEqual(self._day5_part1(case), ans)

    def test_day5_part1(self):
        data = data19(7)
        self.assertEqual(
            max(
                [
                    self._day5_part1((data, ",".join(map(str, x))))
                    for x in itertools.permutations(range(5))
                ]
            ),
            99376,
        )

    def _day5_part2(self, inp):
        prog, inps = inp
        prog = intcode.getcodes(prog)
        inps = intcode.getcodes(inps)

        computers = [intcode.Computer(prog[:], [i]) for i in inps]
        current = 0
        out = [0]
        while not all(c.finished for c in computers):
            out = computers[current].run(*out)
            current = (current + 1) % 5
        return computers[-1].outputs[-1]

    def test_day5_tests2(self):
        tests = [
            (
                (
                    "3,26,1001,26,-4,26,3,27,1002,27,2,27,1,27,26,27,4,27,1001,28,-1,28,1005,28,6,99,0,0,5",
                    "9,8,7,6,5",
                ),
                139629729,
            ),
            (
                (
                    "3,52,1001,52,-5,52,3,53,1,52,56,54,1007,54,5,55,1005,55,26,1001,54,-5,54,1105,1,12,1,53,54,53,1008,54,0,55,1001,55,1,55,2,53,55,53,4,53,1001,56,-1,56,1005,56,6,99,0,0,0,0,10",
                    "9,7,8,5,6",
                ),
                18216,
            ),
        ]
        for case, ans in tests:
            self.assertEqual(self._day5_part2(case), ans)

    def test_day5_part2(self):
        data = data19(7)
        self.assertEqual(
            max(
                [
                    self._day5_part2((data, ",".join(map(str, x))))
                    for x in itertools.permutations([5, 6, 7, 8, 9])
                ]
            ),
            8754464,
        )

    def test_day9_tests(self):
        tests = [
            (
                "109,1,204,-1,1001,100,1,100,1008,100,16,101,1006,101,0,99",
                [
                    109,
                    1,
                    204,
                    -1,
                    1001,
                    100,
                    1,
                    100,
                    1008,
                    100,
                    16,
                    101,
                    1006,
                    101,
                    0,
                    99,
                ],
            ),
            ("1102,34915192,34915192,7,4,7,99,0", [1219070632396864]),
            ("104,1125899906842624,99", [1125899906842624]),
        ]
        for prog, ans in tests:
            self.assertEqual(runner(prog), ans)

    def test_day9_part1(self):
        data = data19(9)
        self.assertEqual(runner(data, 1), [3241900951])

    def test_day9_part2(self):
        data = data19(9)
        self.assertEqual(runner(data, 2), [83089])
