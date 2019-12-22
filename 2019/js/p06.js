import * as std from "std";

const data = std.open("../input/06.txt", "r").readAsString();

const tree = new Map(data.split("\n").map(x => x.split(")").reverse()));

let store = new Map();

function trace(node) {
  if (store.has(node)) { 
    return store.get(node);
  }
  const parent = tree.get(node);
  const ans = parent ? [parent].concat(trace(parent)) : []; 
  store.set(node, ans); 
  return ans;
}

print("Answer1: ", Array.from(tree.keys()).map(
      x => trace(x).length).reduce((x, y) => x + y))

let p1 = new Set(trace("YOU"));
let p2 = new Set(trace("SAN"));

function difference(setA, setB) {
    let _difference = new Set(setA)
    for (let elem of setB) {
        _difference.delete(elem)
    }
    return _difference
}

function union(setA, setB) {
    let _union = new Set(setA)
    for (let elem of setB) {
        _union.add(elem)
    }
    return _union
}

print("Answer2: ", 
    union(
      difference(p1, p2),
      difference(p2, p1)).size);
