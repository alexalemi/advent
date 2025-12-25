#!/usr/bin/env python3
"""Day 12: Christmas Tree Farm - Handles slack (empty cells)"""

import sys
sys.setrecursionlimit(100000)


def parse_input(filename):
    with open(filename) as f:
        content = f.read()

    lines = content.strip().split('\n')
    shapes = {}
    i = 0
    while i < len(lines):
        line = lines[i].strip()
        if not line:
            i += 1
            continue
        if ':' in line and 'x' not in line:
            idx = int(line.split(':')[0])
            shape_lines = []
            i += 1
            while i < len(lines) and lines[i].strip() and 'x' not in lines[i]:
                shape_lines.append(lines[i])
                i += 1
            coords = set()
            for r, sline in enumerate(shape_lines):
                for c, ch in enumerate(sline):
                    if ch == '#':
                        coords.add((r, c))
            shapes[idx] = coords
        elif 'x' in line:
            break
        else:
            i += 1

    regions = []
    for line in lines:
        if 'x' in line and ':' in line:
            dim_part, counts_part = line.split(':')
            w, h = map(int, dim_part.strip().split('x'))
            counts = list(map(int, counts_part.strip().split()))
            regions.append((w, h, counts))

    return shapes, regions


def get_all_orientations(shape):
    """Get all unique orientations, normalized to (0,0)."""
    def normalize(coords):
        if not coords:
            return frozenset()
        min_r = min(r for r, c in coords)
        min_c = min(c for r, c in coords)
        return frozenset((r - min_r, c - min_c) for r, c in coords)

    def rotate_90(coords):
        return {(c, -r) for r, c in coords}

    def flip_horizontal(coords):
        return {(r, -c) for r, c in coords}

    orientations = set()
    current = set(shape)
    for _ in range(4):
        orientations.add(normalize(current))
        orientations.add(normalize(flip_horizontal(current)))
        current = rotate_90(current)

    return [tuple(sorted(o)) for o in orientations]


def solve_region(width, height, shape_counts, all_orientations):
    """Solve allowing some cells to remain empty (slack)."""
    shapes_needed = []
    for shape_idx, count in enumerate(shape_counts):
        if count > 0:
            shapes_needed.append((shape_idx, count))

    if not shapes_needed:
        return True

    # Area check
    shape_sizes = {idx: len(all_orientations[idx][0]) for idx, _ in shapes_needed}
    total_cells = sum(shape_sizes[idx] * count for idx, count in shapes_needed)
    region_area = width * height
    if total_cells > region_area:
        return False

    slack = region_area - total_cells  # Number of cells that must remain empty

    # Precompute all valid placements
    placements = {}
    for shape_idx, _ in shapes_needed:
        placements[shape_idx] = []
        for orientation in all_orientations[shape_idx]:
            max_r = max(r for r, c in orientation)
            max_c = max(c for r, c in orientation)
            for row in range(height - max_r):
                for col in range(width - max_c):
                    placements[shape_idx].append((orientation, row, col))

    # Grid: 0=empty, 1=filled by shape, 2=marked as slack
    grid = [[0] * width for _ in range(height)]
    remaining = {idx: count for idx, count in shapes_needed}
    slack_remaining = slack

    iterations = 0
    max_iterations = 3000000

    def can_place(cells, ar, ac):
        for dr, dc in cells:
            if grid[ar + dr][ac + dc] != 0:
                return False
        return True

    def place(cells, ar, ac):
        for dr, dc in cells:
            grid[ar + dr][ac + dc] = 1

    def unplace(cells, ar, ac):
        for dr, dc in cells:
            grid[ar + dr][ac + dc] = 0

    def find_first_empty():
        for r in range(height):
            for c in range(width):
                if grid[r][c] == 0:
                    return (r, c)
        return None

    def backtrack():
        nonlocal iterations, slack_remaining
        iterations += 1
        if iterations > max_iterations:
            return None

        if all(c == 0 for c in remaining.values()):
            return True

        empty = find_first_empty()
        if empty is None:
            return all(c == 0 for c in remaining.values())

        target_r, target_c = empty

        # Try each shape type
        for shape_idx in remaining:
            if remaining[shape_idx] == 0:
                continue

            for orientation, row, col in placements[shape_idx]:
                # Check if this placement covers the target cell
                covers_target = False
                for dr, dc in orientation:
                    if row + dr == target_r and col + dc == target_c:
                        covers_target = True
                        break

                if not covers_target:
                    continue

                if can_place(orientation, row, col):
                    place(orientation, row, col)
                    remaining[shape_idx] -= 1

                    result = backtrack()
                    if result is True:
                        return True
                    if result is None:
                        remaining[shape_idx] += 1
                        unplace(orientation, row, col)
                        return None

                    remaining[shape_idx] += 1
                    unplace(orientation, row, col)

        # If no shape can cover this cell, try marking it as slack
        if slack_remaining > 0:
            grid[target_r][target_c] = 2  # Mark as slack
            slack_remaining -= 1

            result = backtrack()
            if result is True:
                return True
            if result is None:
                slack_remaining += 1
                grid[target_r][target_c] = 0
                return None

            slack_remaining += 1
            grid[target_r][target_c] = 0

        return False

    return backtrack()


def main():
    shapes, regions = parse_input('../input/12.txt')
    print(f"Parsed {len(shapes)} shapes and {len(regions)} regions", flush=True)

    all_orientations = {}
    for idx, shape in shapes.items():
        orients = get_all_orientations(shape)
        all_orientations[idx] = orients
        print(f"Shape {idx}: {len(shape)} cells, {len(orients)} orientations", flush=True)

    count = 0
    unknown = 0

    for i, (width, height, shape_counts) in enumerate(regions):
        result = solve_region(width, height, shape_counts, all_orientations)
        if result is True:
            count += 1
        elif result is None:
            unknown += 1

        if (i + 1) % 50 == 0:
            print(f"Processed {i + 1}/{len(regions)}: {count} yes, {unknown} unknown, {i+1-count-unknown} no", flush=True)

    print(f"\nResults: {count} yes, {unknown} unknown, {len(regions)-count-unknown} no", flush=True)
    print(f"Part 1: {count}", flush=True)
    return count


if __name__ == '__main__':
    main()
