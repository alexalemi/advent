
type vals = array[0..3, int]

type State = tuple
  xs: vals
  ys: vals
  zs: vals
  vx: vals
  vy: vals
  vz: vals


func energy(state: State): int =
  var total = 0
  for i in 0..<4:
    total += (abs(state.xs[i]) + abs(state.ys[i]) + abs(state.zs[i])) * (abs(
        state.vx[i]) + abs(state.vy[i]) + abs(state.vz[i]))
  return total


func step(state: State): State =
  result = state
  for i in 0..3:
    for j in 0..<i:
      let xdir = cmp(state.xs[i], state.xs[j])
      result.vx[i] -= xdir
      result.vx[j] += xdir
      let ydir = cmp(state.ys[i], state.ys[j])
      result.vy[i] -= ydir
      result.vy[j] += ydir
      let zdir = cmp(state.zs[i], state.zs[j])
      result.vz[i] -= zdir
      result.vz[j] += zdir
  for i in 0..3:
    result.xs[i] += result.vx[i]
    result.ys[i] += result.vy[i]
    result.zs[i] += result.vz[i]


func steps(state: State, n: int): State =
  result = state
  for i in 0..<n:
    result = step(result)


func gcd(a: int, b: int): int =
  var a = a
  var b = b
  while b > 0:
    (a, b) = (b, a mod b)
  return a

func lcm(a: int, b: int): int = (a * b) div gcd(a, b)

func recur(state: State): int =
  var xcount = 2
  var x = step(state)
  while x.xs != state.xs:
    x = step(x)
    xcount += 1

  x = step(state)
  var ycount = 2
  while x.ys != state.ys:
    x = step(x)
    ycount += 1

  x = step(state)
  var zcount = 2
  while x.zs != state.zs:
    x = step(x)
    zcount += 1

  return lcm(xcount, lcm(ycount, zcount))


when isMainModule:
  let state: State = (xs: [-10, 1, -15, 3], ys: [-13, 2, -3, 7], zs: [7,1,13,-4], vx: [0,0,0,0], vy: [0,0,0,0], vz: [0,0,0,0])
  let future = steps(state, 1000)
  echo future
  echo "energy= ", energy(future)

  # let state1: State = (xs: [-1, 2, 4, 3], ys: [0, -10, -8, 5], zs: [2, -7, 8, -1], vx: [0, 0, 0, 0], vy: [0,0,0,0], vz: [0,0,0,0])
  # echo recur(state1)
  # let state2: State = (xs: [-8, 5, 2, 9], ys: [-10, 5, -7, -8], zs: [0, 10, 3, -3], vx: [0, 0, 0, 0], vy: [0,0,0,0], vz: [0,0,0,0])
  echo "answer2 = ", recur(state)


