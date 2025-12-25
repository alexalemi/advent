#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <sys/stat.h>

#define MAX_SHAPES 20
#define MAX_CELLS 50
#define MAX_SHAPE_CELLS 8
#define MAX_ORIENTATIONS 8
#define MAX_REGIONS 1000
#define MAX_PLACEMENTS 5000
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

// Grid encoding
#define GRID_SHAPE(cell) (((cell) & 0xFF) - 1)
#define GRID_PIECE(cell) ((cell) >> 8)
#define MAKE_GRID_VAL(piece_id, shape_id) (((piece_id) << 8) | ((shape_id) + 1))
#define SLACK_SHAPE_ID 99

typedef struct {
    int grid[MAX_HEIGHT][MAX_WIDTH];
    int remaining[MAX_SHAPES];
    int total_remaining;
    int slack_remaining;
    int iterations;
    Placement all_placements[MAX_SHAPES][MAX_PLACEMENTS];
    int placement_counts[MAX_SHAPES];
    int first_empty_hint;
    int covering[MAX_HEIGHT][MAX_WIDTH][MAX_SHAPES][100];
    int covering_counts[MAX_HEIGHT][MAX_WIDTH][MAX_SHAPES];
    int next_piece_id;
} ThreadState;

#define MAX_COVERING 100
static ThreadState ts;

bool can_place(Placement *p) {
    for (int i = 0; i < p->cell_count; i++) {
        if (ts.grid[p->row + p->cells[i].r][p->col + p->cells[i].c] != 0) return false;
    }
    return true;
}

void place(Placement *p, int shape_id) {
    int piece_id = ts.next_piece_id++;
    for (int i = 0; i < p->cell_count; i++) {
        ts.grid[p->row + p->cells[i].r][p->col + p->cells[i].c] = MAKE_GRID_VAL(piece_id, shape_id);
    }
}

void unplace(Placement *p) {
    for (int i = 0; i < p->cell_count; i++) {
        ts.grid[p->row + p->cells[i].r][p->col + p->cells[i].c] = 0;
    }
}

// Nord color palette
static const unsigned char shape_colors[][3] = {
    {191, 97, 106},   // Shape 0: Nord Red
    {208, 135, 112},  // Shape 1: Nord Orange
    {235, 203, 139},  // Shape 2: Nord Yellow
    {163, 190, 140},  // Shape 3: Nord Green
    {136, 192, 208},  // Shape 4: Nord Cyan
    {180, 142, 173},  // Shape 5: Nord Purple
};
static const unsigned char empty_color[3] = {236, 239, 244};
static const unsigned char border_color[3] = {46, 52, 64};

#define CELL_SIZE 20

// Compute actual bounding box of placed pieces (excluding slack/empty)
void get_actual_bounds(int width, int height, int *out_min_r, int *out_max_r, int *out_min_c, int *out_max_c) {
    int min_r = height, max_r = -1, min_c = width, max_c = -1;
    for (int r = 0; r < height; r++) {
        for (int c = 0; c < width; c++) {
            int cell = ts.grid[r][c];
            if (cell != 0 && GRID_SHAPE(cell) != SLACK_SHAPE_ID) {
                if (r < min_r) min_r = r;
                if (r > max_r) max_r = r;
                if (c < min_c) min_c = c;
                if (c > max_c) max_c = c;
            }
        }
    }
    *out_min_r = min_r;
    *out_max_r = max_r;
    *out_min_c = min_c;
    *out_max_c = max_c;
}

void save_solution_ppm_bounds(int min_r, int max_r, int min_c, int max_c, const char *filename) {
    int actual_w = max_c - min_c + 1;
    int actual_h = max_r - min_r + 1;
    int img_w = actual_w * CELL_SIZE;
    int img_h = actual_h * CELL_SIZE;

    FILE *f = fopen(filename, "wb");
    if (!f) return;

    fprintf(f, "P6\n%d %d\n255\n", img_w, img_h);

    for (int py = 0; py < img_h; py++) {
        for (int px = 0; px < img_w; px++) {
            int local_cell_r = py / CELL_SIZE;
            int local_cell_c = px / CELL_SIZE;
            int cell_r = min_r + local_cell_r;
            int cell_c = min_c + local_cell_c;
            int local_y = py % CELL_SIZE;
            int local_x = px % CELL_SIZE;

            int cell = ts.grid[cell_r][cell_c];
            const unsigned char *color;

            int shape_id = (cell == 0) ? -1 : GRID_SHAPE(cell);
            bool is_shape = (cell != 0 && shape_id != SLACK_SHAPE_ID);
            bool is_outline = false;

            int left_cell = (cell_c > min_c) ? ts.grid[cell_r][cell_c - 1] : 0;
            int top_cell = (cell_r > min_r) ? ts.grid[cell_r - 1][cell_c] : 0;
            int left_shape = (left_cell == 0) ? -1 : GRID_SHAPE(left_cell);
            int top_shape = (top_cell == 0) ? -1 : GRID_SHAPE(top_cell);
            bool left_is_shape = (left_cell != 0 && left_shape != SLACK_SHAPE_ID);
            bool top_is_shape = (top_cell != 0 && top_shape != SLACK_SHAPE_ID);

            if (local_x == 0 && (is_shape || left_is_shape) && cell != left_cell) {
                is_outline = true;
            }
            if (local_y == 0 && (is_shape || top_is_shape) && cell != top_cell) {
                is_outline = true;
            }
            if (local_x == CELL_SIZE - 1 && local_cell_c == actual_w - 1 && is_shape) {
                is_outline = true;
            }
            if (local_y == CELL_SIZE - 1 && local_cell_r == actual_h - 1 && is_shape) {
                is_outline = true;
            }

            if (is_outline) {
                color = border_color;
            } else if (!is_shape) {
                color = empty_color;
            } else if (shape_id < 6) {
                color = shape_colors[shape_id];
            } else {
                color = empty_color;
            }

            fputc(color[0], f);
            fputc(color[1], f);
            fputc(color[2], f);
        }
    }

    fclose(f);
}

bool find_first_empty(int width, int height, int *out_r, int *out_c) {
    int start_r = ts.first_empty_hint / width;
    int start_c = ts.first_empty_hint % width;

    for (int r = start_r; r < height; r++) {
        for (int c = (r == start_r ? start_c : 0); c < width; c++) {
            if (ts.grid[r][c] == 0) {
                *out_r = r;
                *out_c = c;
                ts.first_empty_hint = r * width + c;
                return true;
            }
        }
    }
    return false;
}

int backtrack(int width, int height, int *shapes_needed, int shapes_needed_count) {
    ts.iterations++;
    if (ts.iterations > MAX_ITERATIONS) return -1;

    if (ts.total_remaining == 0) return 1;

    int target_r, target_c;
    if (!find_first_empty(width, height, &target_r, &target_c)) {
        return (ts.total_remaining == 0) ? 1 : 0;
    }

    int saved_hint = ts.first_empty_hint;

    for (int si = 0; si < shapes_needed_count; si++) {
        int shape_idx = shapes_needed[si];
        if (ts.remaining[shape_idx] == 0) continue;

        int num_covering = ts.covering_counts[target_r][target_c][shape_idx];
        for (int ci = 0; ci < num_covering; ci++) {
            int pi = ts.covering[target_r][target_c][shape_idx][ci];
            Placement *p = &ts.all_placements[shape_idx][pi];

            if (can_place(p)) {
                place(p, shape_idx);
                ts.remaining[shape_idx]--;
                ts.total_remaining--;

                int result = backtrack(width, height, shapes_needed, shapes_needed_count);
                if (result == 1) return 1;
                if (result == -1) {
                    ts.remaining[shape_idx]++;
                    ts.total_remaining++;
                    unplace(p);
                    ts.first_empty_hint = saved_hint;
                    return -1;
                }

                ts.remaining[shape_idx]++;
                ts.total_remaining++;
                unplace(p);
                ts.first_empty_hint = saved_hint;
            }
        }
    }

    // Allow slack if available
    if (ts.slack_remaining > 0) {
        int slack_piece_id = ts.next_piece_id++;
        ts.grid[target_r][target_c] = MAKE_GRID_VAL(slack_piece_id, SLACK_SHAPE_ID);
        ts.slack_remaining--;

        int result = backtrack(width, height, shapes_needed, shapes_needed_count);
        if (result == 1) return 1;
        if (result == -1) {
            ts.slack_remaining++;
            ts.grid[target_r][target_c] = 0;
            ts.first_empty_hint = saved_hint;
            return -1;
        }

        ts.slack_remaining++;
        ts.grid[target_r][target_c] = 0;
        ts.first_empty_hint = saved_hint;
    }

    return 0;
}

int solve_region_with_slack(int width, int height, int *shape_counts, int shape_count_len, int allowed_slack) {
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
    int slack = region_area - total_cells;
    if (slack < 0 || slack > allowed_slack) return 0;

    // Clear covering index
    for (int r = 0; r < height; r++) {
        for (int c = 0; c < width; c++) {
            for (int si = 0; si < shapes_needed_count; si++) {
                ts.covering_counts[r][c][shapes_needed[si]] = 0;
            }
        }
    }

    // Build placements and covering index
    for (int i = 0; i < shapes_needed_count; i++) {
        int shape_idx = shapes_needed[i];
        ts.placement_counts[shape_idx] = 0;

        ShapeOrientations *so = &all_orientations[shape_idx];
        for (int oi = 0; oi < so->count; oi++) {
            Shape *orient = &so->orientations[oi];
            int max_r = 0, max_c = 0;
            for (int ci = 0; ci < orient->count; ci++) {
                if (orient->cells[ci].r > max_r) max_r = orient->cells[ci].r;
                if (orient->cells[ci].c > max_c) max_c = orient->cells[ci].c;
            }

            for (int row = 0; row <= height - max_r - 1; row++) {
                for (int col = 0; col <= width - max_c - 1; col++) {
                    if (ts.placement_counts[shape_idx] >= MAX_PLACEMENTS) break;
                    int pi = ts.placement_counts[shape_idx]++;
                    Placement *p = &ts.all_placements[shape_idx][pi];
                    p->cell_count = orient->count;
                    memcpy(p->cells, orient->cells, orient->count * sizeof(Coord));
                    p->row = row;
                    p->col = col;

                    for (int ci = 0; ci < orient->count; ci++) {
                        int cr = row + orient->cells[ci].r;
                        int cc = col + orient->cells[ci].c;
                        int idx = ts.covering_counts[cr][cc][shape_idx]++;
                        if (idx < MAX_COVERING) {
                            ts.covering[cr][cc][shape_idx][idx] = pi;
                        }
                    }
                }
            }
        }
    }

    // Sort shapes by placement count
    for (int i = 0; i < shapes_needed_count - 1; i++) {
        for (int j = i + 1; j < shapes_needed_count; j++) {
            if (ts.placement_counts[shapes_needed[j]] < ts.placement_counts[shapes_needed[i]]) {
                int tmp = shapes_needed[i];
                shapes_needed[i] = shapes_needed[j];
                shapes_needed[j] = tmp;
            }
        }
    }

    memset(ts.grid, 0, sizeof(ts.grid));
    ts.next_piece_id = 1;
    ts.total_remaining = 0;
    for (int i = 0; i < shapes_needed_count; i++) {
        ts.remaining[shapes_needed[i]] = shape_counts[shapes_needed[i]];
        ts.total_remaining += shape_counts[shapes_needed[i]];
    }
    ts.slack_remaining = slack;
    ts.iterations = 0;
    ts.first_empty_hint = 0;

    return backtrack(width, height, shapes_needed, shapes_needed_count);
}

int main(int argc, char **argv) {
    if (argc < 2) {
        printf("Usage: %s <region_idx> [max_slack]\n", argv[0]);
        printf("Finds minimum bounding box for a region's pieces.\n");
        printf("  max_slack: maximum extra cells allowed (default 0)\n");
        return 1;
    }

    int region_idx = atoi(argv[1]);
    int max_slack = (argc >= 3) ? atoi(argv[2]) : 0;

    parse_input("../input/12.txt");
    for (int i = 0; i < num_shapes; i++) {
        get_all_orientations(i);
    }

    if (region_idx < 0 || region_idx >= num_regions) {
        printf("Invalid region index. Valid range: 0-%d\n", num_regions - 1);
        return 1;
    }

    Region *reg = &regions[region_idx];

    // Calculate exact area needed
    int total_cells = 0;
    for (int i = 0; i < reg->shape_count; i++) {
        total_cells += all_orientations[i].orientations[0].count * reg->shape_counts[i];
    }

    int original_area = reg->width * reg->height;

    printf("Region %d:\n", region_idx);
    printf("  Original: %dx%d = %d cells\n", reg->width, reg->height, original_area);
    printf("  Pieces need: %d cells (minimum possible)\n", total_cells);
    printf("  Shape counts:");
    for (int i = 0; i < reg->shape_count; i++) {
        if (reg->shape_counts[i] > 0) {
            printf(" [%d]x%d", i, reg->shape_counts[i]);
        }
    }
    printf("\n\n");

    mkdir("imgs", 0755);

    // Search from smallest possible area upward until we find something
    printf("Searching for tightest fit...\n");
    for (int area = total_cells; area < original_area; area++) {
        // Try all width/height combinations for this area
        for (int w = 1; w <= area; w++) {
            if (area % w != 0) continue;
            int h = area / w;

            if (w > MAX_WIDTH || h > MAX_HEIGHT) continue;

            // Skip very narrow shapes (min dimension < 5 for large areas)
            if (area > 100 && (w < 5 || h < 5)) continue;

            int slack = area - total_cells;
            int result = solve_region_with_slack(w, h, reg->shape_counts, reg->shape_count, slack);
            if (result == 1) {
                // Compute actual tight bounding box
                int min_r, max_r, min_c, max_c;
                get_actual_bounds(w, h, &min_r, &max_r, &min_c, &max_c);
                int actual_w = max_c - min_c + 1;
                int actual_h = max_r - min_r + 1;
                int actual_area = actual_w * actual_h;

                printf("\nFound solution at %dx%d (slack %d)\n", w, h, slack);
                printf("  Actual tight bounds: %dx%d = %d cells\n", actual_w, actual_h, actual_area);
                printf("  Reduction: %d -> %d (%.1f%% of original)\n",
                       original_area, actual_area, 100.0 * actual_area / original_area);

                char filename[256];
                snprintf(filename, sizeof(filename), "imgs/shrink_%04d_%dx%d.ppm", region_idx, actual_w, actual_h);
                save_solution_ppm_bounds(min_r, max_r, min_c, max_c, filename);

                char cmd[512];
                snprintf(cmd, sizeof(cmd), "convert %s %.*s.png 2>/dev/null", filename, (int)(strlen(filename)-4), filename);
                if (system(cmd) == 0) {
                    remove(filename);
                    printf("  Saved: %.*s.png\n", (int)(strlen(filename)-4), filename);
                } else {
                    printf("  Saved: %s\n", filename);
                }
                return 0;
            } else if (result == -1) {
                // Timeout - skip this dimension
            }
        }
        // Progress indicator
        if ((area - total_cells) % 50 == 0 && area > total_cells) {
            printf("  Checked up to area %d (slack %d)...\n", area, area - total_cells);
        }
    }

    printf("No tighter fit found - original is already optimal or too constrained\n");
    return 0;
}
