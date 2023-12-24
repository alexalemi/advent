# Advent of Code 2023 - Day 24

from typing import NamedTuple
from collections.abc import Generator
import itertools
import numpy as np

with open("../input/24.txt") as f:
    data_string = f.read()

test_string = """19, 13, 30 @ -2,  1, -2
18, 19, 22 @ -1, -1, -2
20, 25, 34 @ -2, -2, -4
12, 31, 28 @ -1, -2, -1
20, 19, 15 @  1, -5, -3"""

type Bounds = tuple[int, int]
type Vector = tuple[int, int, int]

class Ball(NamedTuple):
    p: Vector
    v: Vector

def process(s: str) -> Generator[Ball, None, None]:
    for line in s.splitlines():
        left, right, *rest = line.split(" @ ")
        yield Ball(
                np.array(tuple(map(int, left.split(",")))), 
                np.array(tuple(map(int, right.split(",")))))

def move(ball: Ball, t: int) -> Vector:
    return ball.p + t * ball.v

test_data = tuple(process(test_string))
data = tuple(process(data_string))

def inside(bounds: Bounds, p: Vector) -> bool:
    lo, hi = bounds
    x, y, _ = p
    return (lo <= int(x) <= hi) and (lo <= int(y) <= hi)

def collision(bounds: Bounds,
              ball0: Ball,
              ball1: Ball):
    px0, py0, _ = ball0.p
    vx0, vy0, _ = ball0.v
    px1, py1, _ = ball1.p
    vx1, vy1, _ = ball1.v
    if (denom := vx1 * vy0 - vx0 * vy1):
        t0 = (py1 * vx1 + px0 * vy1 - py0 * vx1 - px1 * vy1)/denom
        t1 = (py1 * vx0 + px0 * vy0 - py0 * vx0 - px1 * vy0)/denom
        return (t0 > 0) and (t1 > 0) and inside(bounds, move(ball0, t0)) and inside(bounds, move(ball1, t1))
    return False


def part1(bounds: Bounds, data: tuple[Ball, ...]):
    return sum(1 for ball0, ball1 in itertools.combinations(data, 2) if collision(bounds, ball0, ball1))

assert 2 == part1((7, 27), test_data)
ans1 = part1((200000000000000, 400000000000000), data)
assert ans1 == 14799


### Alternating minimization


def solver(data):
    xs = np.array([ball.p for ball in data]).astype('float128')
    vs = np.array([ball.v for ball in data]).astype('float128')

    xx = xs.mean(0)
    xx = np.random.rand(3)
    vv = vs.mean(0)
    vv = np.random.rand(3)
    N = len(data)
    ts = np.ones(N).astype('float128')
    ts = np.random.rand(N)
    rho = 1.0

    def project_ts(xx, vv, ts, rho=rho):
        return (rho * ts + (xs * vv).sum(1) + (vs * xx).sum(1) - (xs * vs).sum(1) - (xx * vv).sum())/(rho + ((vs-vv)**2).sum(1))

    def project_xx_vv(xx, vv, ts, rho=rho):
        return ( (rho * xx + ( xs - ts[:,None] * ( vv - vs)).sum(0))/(rho + N),
                 (rho * vv + ( ts[:,None] * ((xs - xx) + ts[:,None] * vs)).sum(0))/(rho + (ts**2).sum()) )

    eps = 1.0
    ans = xx.sum()
    i= 0

    for j in range(10): 
        for k in range(1000): 
            i += 1 
            ts = project_ts(xx, vv, ts, rho=rho)
            (xx, vv) = project_xx_vv(xx, vv, ts, rho=rho)
        print(f"iter = {i}, guess = {xx.sum()}")
    return xx.sum()

    alpha = 0.01
    x_ts = ts[:]
    y_ts = ts[:]
    z_ts = ts[:]
    x_xx = xx[:]
    y_xx = xx[:]
    z_xx = xx[:]
    x_vv = vv[:]
    y_vv = vv[:]
    z_vv = vv[:]

    i = 0

    while eps > 1e-15:
        i += 1

        # y'        PD (ts)
        yp_xx =  z_xx + alpha * x_xx
        yp_vv =  z_vv + alpha * x_vv
        yp_ts  = project_ts(yp_xx, yp_vv, z_ts + alpha * x_ts, rho=rho)
        yp_xx = np.round(yp_xx)
        # yp_vv = np.round(yp_vv)
        # yp_ts = np.round(yp_ts)

        # z'        PC (xx, vv)
        zp_ts = yp_ts - alpha * x_ts
        (zp_xx, zp_vv) = project_xx_vv(yp_xx - alpha * x_xx, yp_vv - alpha * x_vv, zp_ts, rho=rho)

        # x
        x_xx += zp_xx - yp_xx
        x_vv += zp_vv - yp_vv
        x_ts += zp_ts - yp_ts

        (y_xx, y_vv, y_ts) = (yp_xx, yp_vv, yp_ts)
        (z_xx, z_vv, z_ts) = (zp_xx, zp_vv, zp_ts)

        eps = np.abs(zp_xx - yp_xx).sum() + np.abs(zp_vv - yp_vv).sum() + np.abs(zp_ts - yp_ts).sum() 
        print(f"iter = {i}, guess = {y_xx.sum()} guess2= {z_xx.sum()}, eps = {eps:.3g}")

    return int(np.round(xx.sum()))


# assert 47 == solver(test_data)
ans2 = solver(data)
print("Answer 2:", ans2)
# assert ans2 == 1007148211789625

if __name__ == "__main__":
    print("Answer 1:", ans1)
    print("Answer 2:", ans2)


## False directions Part 2

import jax
import jax.flatten_util
import jax.numpy as jnp

def jaxify(xs):
    return Ball(jnp.array([x.p for x in xs]).astype('float64'),
                jnp.array([x.v for x in xs]).astype('float64'))

jtest_data = jaxify(test_data)
jdata = jaxify(data)

ball0 = Ball(jdata.p.mean(0), jdata.v.mean(0))
ts = jnp.ones(len(data))
flat_params, unravel_tree = jax.flatten_util.ravel_pytree((ball0, ts))

def make_energy(balls: tuple[Ball, ...]):
    n = len(balls)
    @jax.jit
    def energy(flat_params):
        ball, ts = unravel_tree(flat_params)
        balls_poses = jax.vmap(move)(balls, ts)
        ball_poses = jax.vmap(move, (None, 0))(ball, ts)
        return jnp.mean((1.0 * balls_poses - 1.0 * ball_poses)**2)
    return energy

from jax.scipy.optimize import minimize
import optax

def newtons_method(f):
    def iter(x0):
        hess = jax.hessian(f)(x0)
        grad = jax.grad(f)(x0)
        return x0 - inv(hess).dot(grad)
        return solve(hess, hess.dot(x0) - grad)
    return iter

energy = make_energy(jdata)
opt = optax.adabelief(1e-2)
x = flat_params[:]
state = opt.init(x)
