#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

#define MAX_SHAPES 20
#define MAX_CELLS 50
#define MAX_SHAPE_CELLS 8  // Actual shapes have 5-7 cells
#define MAX_ORIENTATIONS 8
#define MAX_REGIONS 1000
#define MAX_PLACEMENTS 10000
#define MAX_WIDTH 100
#define MAX_HEIGHT 100
#define MAX_ITERATIONS 3000000

typedef struct {
    int r, c;
} Coord;

typedef struct {
    Coord cells[MAX_CELLS];
    int count;
} Shape;

typedef struct {
    Shape orientations[MAX_ORIENTATIONS];
    int count;
} ShapeOrientations;

typedef struct {
    Coord cells[MAX_SHAPE_CELLS];
    int cell_count;
    int row, col;
} Placement;

typedef struct {
    int width, height;
    int shape_counts[MAX_SHAPES];
    int shape_count;
} Region;

Shape shapes[MAX_SHAPES];
int num_shapes = 0;
ShapeOrientations all_orientations[MAX_SHAPES];
Region regions[MAX_REGIONS];
int num_regions = 0;

void normalize_shape(Shape *s) {
    if (s->count == 0) return;
    int min_r = s->cells[0].r, min_c = s->cells[0].c;
    for (int i = 1; i < s->count; i++) {
        if (s->cells[i].r < min_r) min_r = s->cells[i].r;
        if (s->cells[i].c < min_c) min_c = s->cells[i].c;
    }
    for (int i = 0; i < s->count; i++) {
        s->cells[i].r -= min_r;
        s->cells[i].c -= min_c;
    }
}

void rotate_90(Shape *src, Shape *dst) {
    dst->count = src->count;
    for (int i = 0; i < src->count; i++) {
        dst->cells[i].r = src->cells[i].c;
        dst->cells[i].c = -src->cells[i].r;
    }
    normalize_shape(dst);
}

void flip_horizontal(Shape *src, Shape *dst) {
    dst->count = src->count;
    for (int i = 0; i < src->count; i++) {
        dst->cells[i].r = src->cells[i].r;
        dst->cells[i].c = -src->cells[i].c;
    }
    normalize_shape(dst);
}

int compare_coords(const void *a, const void *b) {
    const Coord *ca = (const Coord *)a;
    const Coord *cb = (const Coord *)b;
    if (ca->r != cb->r) return ca->r - cb->r;
    return ca->c - cb->c;
}

void sort_shape(Shape *s) {
    qsort(s->cells, s->count, sizeof(Coord), compare_coords);
}

bool shapes_equal(Shape *a, Shape *b) {
    if (a->count != b->count) return false;
    for (int i = 0; i < a->count; i++) {
        if (a->cells[i].r != b->cells[i].r || a->cells[i].c != b->cells[i].c) {
            return false;
        }
    }
    return true;
}

void get_all_orientations(int shape_idx) {
    ShapeOrientations *so = &all_orientations[shape_idx];
    so->count = 0;

    Shape current;
    memcpy(&current, &shapes[shape_idx], sizeof(Shape));
    normalize_shape(&current);

    for (int rot = 0; rot < 4; rot++) {
        Shape normalized, flipped;
        memcpy(&normalized, &current, sizeof(Shape));
        sort_shape(&normalized);

        bool exists = false;
        for (int i = 0; i < so->count; i++) {
            if (shapes_equal(&normalized, &so->orientations[i])) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            memcpy(&so->orientations[so->count++], &normalized, sizeof(Shape));
        }

        flip_horizontal(&current, &flipped);
        sort_shape(&flipped);
        exists = false;
        for (int i = 0; i < so->count; i++) {
            if (shapes_equal(&flipped, &so->orientations[i])) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            memcpy(&so->orientations[so->count++], &flipped, sizeof(Shape));
        }

        Shape rotated;
        rotate_90(&current, &rotated);
        memcpy(&current, &rotated, sizeof(Shape));
    }
}

void parse_input(const char *filename) {
    FILE *f = fopen(filename, "r");
    if (!f) {
        fprintf(stderr, "Cannot open %s\n", filename);
        exit(1);
    }

    char line[1024];
    char shape_lines[20][256];
    int shape_line_count = 0;
    int current_shape_idx = -1;
    bool in_shape = false;

    while (fgets(line, sizeof(line), f)) {
        int len = strlen(line);
        while (len > 0 && (line[len-1] == '\n' || line[len-1] == '\r')) {
            line[--len] = '\0';
        }

        if (strchr(line, 'x') && strchr(line, ':')) {
            if (in_shape && shape_line_count > 0) {
                shapes[current_shape_idx].count = 0;
                for (int r = 0; r < shape_line_count; r++) {
                    for (int c = 0; shape_lines[r][c]; c++) {
                        if (shape_lines[r][c] == '#') {
                            int idx = shapes[current_shape_idx].count++;
                            shapes[current_shape_idx].cells[idx].r = r;
                            shapes[current_shape_idx].cells[idx].c = c;
                        }
                    }
                }
                if (current_shape_idx >= num_shapes) num_shapes = current_shape_idx + 1;
            }
            in_shape = false;

            int w, h;
            char *colon = strchr(line, ':');
            *colon = '\0';
            sscanf(line, "%dx%d", &w, &h);

            Region *reg = &regions[num_regions];
            reg->width = w;
            reg->height = h;
            reg->shape_count = 0;

            char *ptr = colon + 1;
            int val;
            while (sscanf(ptr, "%d", &val) == 1) {
                reg->shape_counts[reg->shape_count++] = val;
                while (*ptr == ' ') ptr++;
                while (*ptr && *ptr != ' ') ptr++;
            }
            num_regions++;
        }
        else if (strchr(line, ':') && !strchr(line, 'x')) {
            if (in_shape && shape_line_count > 0) {
                shapes[current_shape_idx].count = 0;
                for (int r = 0; r < shape_line_count; r++) {
                    for (int c = 0; shape_lines[r][c]; c++) {
                        if (shape_lines[r][c] == '#') {
                            int idx = shapes[current_shape_idx].count++;
                            shapes[current_shape_idx].cells[idx].r = r;
                            shapes[current_shape_idx].cells[idx].c = c;
                        }
                    }
                }
                if (current_shape_idx >= num_shapes) num_shapes = current_shape_idx + 1;
            }

            sscanf(line, "%d:", &current_shape_idx);
            in_shape = true;
            shape_line_count = 0;
        }
        else if (in_shape && len > 0) {
            strcpy(shape_lines[shape_line_count++], line);
        }
    }

    if (in_shape && shape_line_count > 0) {
        shapes[current_shape_idx].count = 0;
        for (int r = 0; r < shape_line_count; r++) {
            for (int c = 0; shape_lines[r][c]; c++) {
                if (shape_lines[r][c] == '#') {
                    int idx = shapes[current_shape_idx].count++;
                    shapes[current_shape_idx].cells[idx].r = r;
                    shapes[current_shape_idx].cells[idx].c = c;
                }
            }
        }
        if (current_shape_idx >= num_shapes) num_shapes = current_shape_idx + 1;
    }

    fclose(f);
}

static int grid[MAX_HEIGHT][MAX_WIDTH];
static int remaining[MAX_SHAPES];
static int total_remaining;
static int slack_remaining;
static int iterations;
static Placement all_placements[MAX_SHAPES][MAX_PLACEMENTS];
static int placement_counts[MAX_SHAPES];
static int first_empty_hint;  // Linear index hint for first empty cell

// Index: for each cell, which placements cover it
#define MAX_COVERING 500
static int covering[MAX_HEIGHT][MAX_WIDTH][MAX_SHAPES][MAX_COVERING];
static int covering_counts[MAX_HEIGHT][MAX_WIDTH][MAX_SHAPES];

bool can_place(Placement *p) {
    for (int i = 0; i < p->cell_count; i++) {
        if (grid[p->row + p->cells[i].r][p->col + p->cells[i].c] != 0) return false;
    }
    return true;
}

void place(Placement *p) {
    for (int i = 0; i < p->cell_count; i++) {
        grid[p->row + p->cells[i].r][p->col + p->cells[i].c] = 1;
    }
}

void unplace(Placement *p) {
    for (int i = 0; i < p->cell_count; i++) {
        grid[p->row + p->cells[i].r][p->col + p->cells[i].c] = 0;
    }
}

bool find_first_empty(int width, int height, int *out_r, int *out_c) {
    int start_r = first_empty_hint / width;
    int start_c = first_empty_hint % width;

    for (int r = start_r; r < height; r++) {
        for (int c = (r == start_r ? start_c : 0); c < width; c++) {
            if (grid[r][c] == 0) {
                *out_r = r;
                *out_c = c;
                first_empty_hint = r * width + c;
                return true;
            }
        }
    }
    return false;
}

int backtrack(int width, int height, int *shapes_needed, int shapes_needed_count) {
    iterations++;
    if (iterations > MAX_ITERATIONS) return -1;

    if (total_remaining == 0) return 1;

    int target_r, target_c;
    if (!find_first_empty(width, height, &target_r, &target_c)) {
        return (total_remaining == 0) ? 1 : 0;
    }

    int saved_hint = first_empty_hint;

    // Use precomputed index of placements covering this cell
    for (int si = 0; si < shapes_needed_count; si++) {
        int shape_idx = shapes_needed[si];
        if (remaining[shape_idx] == 0) continue;

        int num_covering = covering_counts[target_r][target_c][shape_idx];
        for (int ci = 0; ci < num_covering; ci++) {
            int pi = covering[target_r][target_c][shape_idx][ci];
            Placement *p = &all_placements[shape_idx][pi];

            if (can_place(p)) {
                place(p);
                remaining[shape_idx]--;
                total_remaining--;

                int result = backtrack(width, height, shapes_needed, shapes_needed_count);
                if (result == 1) return 1;
                if (result == -1) {
                    remaining[shape_idx]++;
                    total_remaining++;
                    unplace(p);
                    first_empty_hint = saved_hint;
                    return -1;
                }

                remaining[shape_idx]++;
                total_remaining++;
                unplace(p);
                first_empty_hint = saved_hint;
            }
        }
    }

    if (slack_remaining > 0) {
        grid[target_r][target_c] = 2;
        slack_remaining--;

        int result = backtrack(width, height, shapes_needed, shapes_needed_count);
        if (result == 1) return 1;
        if (result == -1) {
            slack_remaining++;
            grid[target_r][target_c] = 0;
            first_empty_hint = saved_hint;
            return -1;
        }

        slack_remaining++;
        grid[target_r][target_c] = 0;
        first_empty_hint = saved_hint;
    }

    return 0;
}

int compare_by_placements(const void *a, const void *b) {
    int ia = *(const int *)a;
    int ib = *(const int *)b;
    return placement_counts[ia] - placement_counts[ib];
}

int solve_region(int width, int height, int *shape_counts, int shape_count_len) {
    int shapes_needed[MAX_SHAPES];
    int shapes_needed_count = 0;

    for (int i = 0; i < shape_count_len; i++) {
        if (shape_counts[i] > 0) {
            shapes_needed[shapes_needed_count++] = i;
        }
    }

    if (shapes_needed_count == 0) return 1;

    int total_cells = 0;
    for (int i = 0; i < shapes_needed_count; i++) {
        int idx = shapes_needed[i];
        total_cells += all_orientations[idx].orientations[0].count * shape_counts[idx];
    }

    int region_area = width * height;
    if (total_cells > region_area) return 0;

    int slack = region_area - total_cells;

    // Clear covering index for this region size
    for (int r = 0; r < height; r++) {
        for (int c = 0; c < width; c++) {
            for (int si = 0; si < shapes_needed_count; si++) {
                covering_counts[r][c][shapes_needed[si]] = 0;
            }
        }
    }

    // Build placements and covering index
    for (int i = 0; i < shapes_needed_count; i++) {
        int shape_idx = shapes_needed[i];
        placement_counts[shape_idx] = 0;

        ShapeOrientations *so = &all_orientations[shape_idx];
        for (int oi = 0; oi < so->count; oi++) {
            Shape *orient = &so->orientations[oi];
            int max_r = 0, max_c = 0;
            for (int ci = 0; ci < orient->count; ci++) {
                if (orient->cells[ci].r > max_r) max_r = orient->cells[ci].r;
                if (orient->cells[ci].c > max_c) max_c = orient->cells[ci].c;
            }

            for (int row = 0; row < height - max_r; row++) {
                for (int col = 0; col < width - max_c; col++) {
                    if (placement_counts[shape_idx] >= MAX_PLACEMENTS) break;
                    int pi = placement_counts[shape_idx]++;
                    Placement *p = &all_placements[shape_idx][pi];
                    p->cell_count = orient->count;
                    memcpy(p->cells, orient->cells, orient->count * sizeof(Coord));
                    p->row = row;
                    p->col = col;

                    // Add to covering index for each cell this placement covers
                    for (int ci = 0; ci < orient->count; ci++) {
                        int cr = row + orient->cells[ci].r;
                        int cc = col + orient->cells[ci].c;
                        int idx = covering_counts[cr][cc][shape_idx]++;
                        if (idx < MAX_COVERING) {
                            covering[cr][cc][shape_idx][idx] = pi;
                        }
                    }
                }
            }
        }
    }

    // Sort shapes by placement count (fewer placements first = fail-first)
    qsort(shapes_needed, shapes_needed_count, sizeof(int), compare_by_placements);

    memset(grid, 0, sizeof(grid));
    total_remaining = 0;
    for (int i = 0; i < shapes_needed_count; i++) {
        remaining[shapes_needed[i]] = shape_counts[shapes_needed[i]];
        total_remaining += shape_counts[shapes_needed[i]];
    }
    slack_remaining = slack;
    iterations = 0;
    first_empty_hint = 0;

    return backtrack(width, height, shapes_needed, shapes_needed_count);
}

int main(void) {
    parse_input("../input/12.txt");
    printf("Parsed %d shapes and %d regions\n", num_shapes, num_regions);
    fflush(stdout);

    for (int i = 0; i < num_shapes; i++) {
        get_all_orientations(i);
        printf("Shape %d: %d cells, %d orientations\n",
               i, shapes[i].count, all_orientations[i].count);
        fflush(stdout);
    }

    int count = 0;
    int unknown = 0;

    for (int i = 0; i < num_regions; i++) {
        Region *reg = &regions[i];
        int result = solve_region(reg->width, reg->height,
                                  reg->shape_counts, reg->shape_count);
        if (result == 1) {
            count++;
        } else if (result == -1) {
            unknown++;
        }

        if ((i + 1) % 50 == 0) {
            printf("Processed %d/%d: %d yes, %d unknown, %d no\n",
                   i + 1, num_regions, count, unknown, i + 1 - count - unknown);
            fflush(stdout);
        }
    }

    printf("\nResults: %d yes, %d unknown, %d no\n",
           count, unknown, num_regions - count - unknown);
    printf("Part 1: %d\n", count);
    fflush(stdout);

    return 0;
}
