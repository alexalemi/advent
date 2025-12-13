// Day 12: Christmas Tree Farm - Handles slack (empty cells)

type Coord = [number, number];
type Shape = Set<string>;
type Orientation = Coord[];
type Region = [number, number, number[]];

function coordKey(r: number, c: number): string {
  return `${r},${c}`;
}

function parseCoord(key: string): Coord {
  const [r, c] = key.split(",").map(Number);
  return [r, c];
}

async function parseInput(filename: string): Promise<{
  shapes: Map<number, Shape>;
  regions: Region[];
}> {
  const file = Bun.file(filename);
  const content = await file.text();

  const lines = content.trim().split("\n");
  const shapes = new Map<number, Shape>();
  let i = 0;

  while (i < lines.length) {
    const line = lines[i].trim();
    if (!line) {
      i++;
      continue;
    }
    if (line.includes(":") && !line.includes("x")) {
      const idx = parseInt(line.split(":")[0]);
      const shapeLines: string[] = [];
      i++;
      while (i < lines.length && lines[i].trim() && !lines[i].includes("x")) {
        shapeLines.push(lines[i]);
        i++;
      }
      const coords = new Set<string>();
      for (let r = 0; r < shapeLines.length; r++) {
        for (let c = 0; c < shapeLines[r].length; c++) {
          if (shapeLines[r][c] === "#") {
            coords.add(coordKey(r, c));
          }
        }
      }
      shapes.set(idx, coords);
    } else if (line.includes("x")) {
      break;
    } else {
      i++;
    }
  }

  const regions: Region[] = [];
  for (const line of lines) {
    if (line.includes("x") && line.includes(":")) {
      const [dimPart, countsPart] = line.split(":");
      const [w, h] = dimPart.trim().split("x").map(Number);
      const counts = countsPart.trim().split(/\s+/).map(Number);
      regions.push([w, h, counts]);
    }
  }

  return { shapes, regions };
}

function getAllOrientations(shape: Shape): Orientation[] {
  function normalize(coords: Coord[]): string {
    if (coords.length === 0) return "";
    const minR = Math.min(...coords.map(([r]) => r));
    const minC = Math.min(...coords.map(([, c]) => c));
    const normalized = coords.map(([r, c]): Coord => [r - minR, c - minC]);
    normalized.sort((a, b) => a[0] - b[0] || a[1] - b[1]);
    return JSON.stringify(normalized);
  }

  function rotate90(coords: Coord[]): Coord[] {
    return coords.map(([r, c]) => [c, -r]);
  }

  function flipHorizontal(coords: Coord[]): Coord[] {
    return coords.map(([r, c]) => [r, -c]);
  }

  const orientations = new Set<string>();
  let current: Coord[] = [...shape].map(parseCoord);

  for (let rot = 0; rot < 4; rot++) {
    orientations.add(normalize(current));
    orientations.add(normalize(flipHorizontal(current)));
    current = rotate90(current);
  }

  return [...orientations].map((s) => JSON.parse(s) as Orientation);
}

function solveRegion(
  width: number,
  height: number,
  shapeCounts: number[],
  allOrientations: Map<number, Orientation[]>
): boolean | null {
  const shapesNeeded: [number, number][] = [];
  for (let shapeIdx = 0; shapeIdx < shapeCounts.length; shapeIdx++) {
    if (shapeCounts[shapeIdx] > 0) {
      shapesNeeded.push([shapeIdx, shapeCounts[shapeIdx]]);
    }
  }

  if (shapesNeeded.length === 0) {
    return true;
  }

  // Area check
  const shapeSizes = new Map<number, number>();
  for (const [idx] of shapesNeeded) {
    shapeSizes.set(idx, allOrientations.get(idx)![0].length);
  }

  let totalCells = 0;
  for (const [idx, count] of shapesNeeded) {
    totalCells += shapeSizes.get(idx)! * count;
  }

  const regionArea = width * height;
  if (totalCells > regionArea) {
    return false;
  }

  const slack = regionArea - totalCells;

  // Precompute all valid placements
  const placements = new Map<number, [Orientation, number, number][]>();
  for (const [shapeIdx] of shapesNeeded) {
    placements.set(shapeIdx, []);
    for (const orientation of allOrientations.get(shapeIdx)!) {
      const maxR = Math.max(...orientation.map(([r]) => r));
      const maxC = Math.max(...orientation.map(([, c]) => c));
      for (let row = 0; row <= height - maxR - 1; row++) {
        for (let col = 0; col <= width - maxC - 1; col++) {
          placements.get(shapeIdx)!.push([orientation, row, col]);
        }
      }
    }
  }

  // Grid: 0=empty, 1=filled by shape, 2=marked as slack
  const grid: number[][] = Array.from({ length: height }, () =>
    Array(width).fill(0)
  );
  const remaining = new Map<number, number>(shapesNeeded);
  let slackRemaining = slack;

  let iterations = 0;
  const maxIterations = 3000000;

  function canPlace(cells: Orientation, ar: number, ac: number): boolean {
    for (const [dr, dc] of cells) {
      if (grid[ar + dr][ac + dc] !== 0) {
        return false;
      }
    }
    return true;
  }

  function place(cells: Orientation, ar: number, ac: number): void {
    for (const [dr, dc] of cells) {
      grid[ar + dr][ac + dc] = 1;
    }
  }

  function unplace(cells: Orientation, ar: number, ac: number): void {
    for (const [dr, dc] of cells) {
      grid[ar + dr][ac + dc] = 0;
    }
  }

  function findFirstEmpty(): Coord | null {
    for (let r = 0; r < height; r++) {
      for (let c = 0; c < width; c++) {
        if (grid[r][c] === 0) {
          return [r, c];
        }
      }
    }
    return null;
  }

  function backtrack(): boolean | null {
    iterations++;
    if (iterations > maxIterations) {
      return null;
    }

    if ([...remaining.values()].every((c) => c === 0)) {
      return true;
    }

    const empty = findFirstEmpty();
    if (empty === null) {
      return [...remaining.values()].every((c) => c === 0);
    }

    const [targetR, targetC] = empty;

    // Try each shape type
    for (const [shapeIdx] of remaining) {
      if (remaining.get(shapeIdx)! === 0) {
        continue;
      }

      for (const [orientation, row, col] of placements.get(shapeIdx)!) {
        // Check if this placement covers the target cell
        let coversTarget = false;
        for (const [dr, dc] of orientation) {
          if (row + dr === targetR && col + dc === targetC) {
            coversTarget = true;
            break;
          }
        }

        if (!coversTarget) {
          continue;
        }

        if (canPlace(orientation, row, col)) {
          place(orientation, row, col);
          remaining.set(shapeIdx, remaining.get(shapeIdx)! - 1);

          const result = backtrack();
          if (result === true) {
            return true;
          }
          if (result === null) {
            remaining.set(shapeIdx, remaining.get(shapeIdx)! + 1);
            unplace(orientation, row, col);
            return null;
          }

          remaining.set(shapeIdx, remaining.get(shapeIdx)! + 1);
          unplace(orientation, row, col);
        }
      }
    }

    // If no shape can cover this cell, try marking it as slack
    if (slackRemaining > 0) {
      grid[targetR][targetC] = 2;
      slackRemaining--;

      const result = backtrack();
      if (result === true) {
        return true;
      }
      if (result === null) {
        slackRemaining++;
        grid[targetR][targetC] = 0;
        return null;
      }

      slackRemaining++;
      grid[targetR][targetC] = 0;
    }

    return false;
  }

  return backtrack();
}

async function main() {
  const { shapes, regions } = await parseInput("../input/12.txt");
  console.log(`Parsed ${shapes.size} shapes and ${regions.length} regions`);

  const allOrientations = new Map<number, Orientation[]>();
  for (const [idx, shape] of shapes) {
    const orients = getAllOrientations(shape);
    allOrientations.set(idx, orients);
    console.log(`Shape ${idx}: ${shape.size} cells, ${orients.length} orientations`);
  }

  let count = 0;
  let unknown = 0;

  for (let i = 0; i < regions.length; i++) {
    const [width, height, shapeCounts] = regions[i];
    const result = solveRegion(width, height, shapeCounts, allOrientations);
    if (result === true) {
      count++;
    } else if (result === null) {
      unknown++;
    }

    if ((i + 1) % 50 === 0) {
      console.log(
        `Processed ${i + 1}/${regions.length}: ${count} yes, ${unknown} unknown, ${i + 1 - count - unknown} no`
      );
    }
  }

  console.log(
    `\nResults: ${count} yes, ${unknown} unknown, ${regions.length - count - unknown} no`
  );
  console.log(`Part 1: ${count}`);
  return count;
}

main();
