#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

#define MAX_SHAPES 20
#define MAX_CELLS 50
#define MAX_ORIENTATIONS 8
#define MAX_REGIONS 1000
#define MAX_WIDTH 100
#define MAX_HEIGHT 100
#define MAX_NODES 1000000
#define MAX_COLUMNS 10100  // shape instances + grid cells

// ============== Data Structures ==============

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
    int width, height;
    int shape_counts[MAX_SHAPES];
    int shape_count;
} Region;

// DLX Node
typedef struct Node {
    struct Node *left, *right, *up, *down;
    struct Node *column;
    int row_id;
} Node;

// Column header
typedef struct {
    Node node;
    int size;
    int col_id;
    bool primary;
} Column;

// ============== Global State ==============

Shape shapes[MAX_SHAPES];
int num_shapes = 0;
ShapeOrientations all_orientations[MAX_SHAPES];
Region regions[MAX_REGIONS];
int num_regions = 0;

// DLX structures (reused per region)
static Node node_pool[MAX_NODES];
static int node_count;
static Column columns[MAX_COLUMNS];
static int num_columns;
static Node header;  // Root header

// ============== Shape Parsing (from p12.c) ==============

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

// ============== DLX Operations ==============

Node *alloc_node(void) {
    if (node_count >= MAX_NODES) {
        fprintf(stderr, "Node pool exhausted\n");
        exit(1);
    }
    return &node_pool[node_count++];
}

void cover(Node *col_node) {
    Column *col = (Column *)col_node;

    // Remove column from header list
    col_node->right->left = col_node->left;
    col_node->left->right = col_node->right;

    // Remove all rows containing this column
    for (Node *row = col_node->down; row != col_node; row = row->down) {
        for (Node *node = row->right; node != row; node = node->right) {
            node->down->up = node->up;
            node->up->down = node->down;
            ((Column *)node->column)->size--;
        }
    }
}

void uncover(Node *col_node) {
    Column *col = (Column *)col_node;

    // Restore all rows (reverse order)
    for (Node *row = col_node->up; row != col_node; row = row->up) {
        for (Node *node = row->left; node != row; node = node->left) {
            ((Column *)node->column)->size++;
            node->down->up = node;
            node->up->down = node;
        }
    }

    // Restore column to header list
    col_node->right->left = col_node;
    col_node->left->right = col_node;
}

// Choose column with minimum size (S heuristic)
Node *choose_column(void) {
    Node *best = NULL;
    int min_size = MAX_NODES;

    for (Node *col = header.right; col != &header; col = col->right) {
        Column *c = (Column *)col;
        if (c->primary && c->size < min_size) {
            min_size = c->size;
            best = col;
            if (min_size == 0) break;  // Can't do better
        }
    }
    return best;
}

bool search(void) {
    // Check if all primary columns are covered
    Node *col = choose_column();
    if (col == NULL) {
        return true;  // Solution found
    }

    Column *c = (Column *)col;
    if (c->size == 0) {
        return false;  // Dead end
    }

    cover(col);

    for (Node *row = col->down; row != col; row = row->down) {
        // Cover all other columns in this row
        for (Node *node = row->right; node != row; node = node->right) {
            cover(node->column);
        }

        if (search()) {
            return true;
        }

        // Uncover all other columns (reverse order)
        for (Node *node = row->left; node != row; node = node->left) {
            uncover(node->column);
        }
    }

    uncover(col);
    return false;
}

// ============== Matrix Building ==============

void init_dlx(void) {
    node_count = 0;
    num_columns = 0;

    // Initialize header
    header.left = &header;
    header.right = &header;
    header.up = &header;
    header.down = &header;
    header.column = NULL;
}

int add_column(bool primary) {
    int col_id = num_columns++;
    Column *col = &columns[col_id];

    col->node.column = (Node *)col;
    col->node.up = &col->node;
    col->node.down = &col->node;
    col->size = 0;
    col->col_id = col_id;
    col->primary = primary;

    if (primary) {
        // Link to header list
        col->node.left = header.left;
        col->node.right = &header;
        header.left->right = &col->node;
        header.left = &col->node;
    } else {
        // Secondary columns not in header list
        col->node.left = &col->node;
        col->node.right = &col->node;
    }

    return col_id;
}

void add_row(int *col_ids, int num_cols, int row_id) {
    Node *first = NULL;
    Node *prev = NULL;

    for (int i = 0; i < num_cols; i++) {
        Node *node = alloc_node();
        Column *col = &columns[col_ids[i]];

        node->column = (Node *)col;
        node->row_id = row_id;

        // Link vertically
        node->up = col->node.up;
        node->down = &col->node;
        col->node.up->down = node;
        col->node.up = node;
        col->size++;

        // Link horizontally
        if (first == NULL) {
            first = node;
            node->left = node;
            node->right = node;
        } else {
            node->left = prev;
            node->right = first;
            prev->right = node;
            first->left = node;
        }
        prev = node;
    }
}

// ============== Solver ==============

#define ROW_ID_SLACK -1  // Slack row marker

// Track how many of each shape type still need to be placed
static int shapes_remaining[MAX_SHAPES];
static int total_shapes_remaining;

// Modified search that tracks shape counts
bool search_with_counts(void) {
    // Choose column with minimum size (among primary columns)
    Node *col = choose_column();
    if (col == NULL) {
        // All primary columns covered - check if all shapes placed
        return (total_shapes_remaining == 0);
    }

    Column *c = (Column *)col;
    if (c->size == 0) {
        return false;  // Dead end - column can't be covered
    }

    cover(col);

    for (Node *row = col->down; row != col; row = row->down) {
        int shape_idx = row->row_id;

        // If this is a shape row, check if we still need this shape
        if (shape_idx >= 0 && shapes_remaining[shape_idx] <= 0) {
            continue;
        }

        // Update shape count if this is a shape row
        if (shape_idx >= 0) {
            shapes_remaining[shape_idx]--;
            total_shapes_remaining--;
        }

        // Cover all other columns in this row
        for (Node *node = row->right; node != row; node = node->right) {
            cover(node->column);
        }

        if (search_with_counts()) {
            return true;
        }

        // Uncover all other columns (reverse order)
        for (Node *node = row->left; node != row; node = node->left) {
            uncover(node->column);
        }

        // Restore shape count
        if (shape_idx >= 0) {
            shapes_remaining[shape_idx]++;
            total_shapes_remaining++;
        }
    }

    uncover(col);
    return false;
}

int solve_region(int width, int height, int *shape_counts, int shape_count_len) {
    // Check if any shapes needed
    int total_shapes = 0;
    for (int i = 0; i < shape_count_len; i++) {
        total_shapes += shape_counts[i];
    }
    if (total_shapes == 0) return 1;

    // Check area constraint
    int total_cells = 0;
    for (int i = 0; i < shape_count_len; i++) {
        if (shape_counts[i] > 0) {
            total_cells += all_orientations[i].orientations[0].count * shape_counts[i];
        }
    }
    if (total_cells > width * height) return 0;

    int slack = width * height - total_cells;

    // Initialize DLX
    init_dlx();

    // Add primary columns for grid cells (must all be covered)
    for (int i = 0; i < width * height; i++) {
        add_column(true);
    }

    // Initialize shape counts
    total_shapes_remaining = total_shapes;
    for (int i = 0; i < shape_count_len; i++) {
        shapes_remaining[i] = shape_counts[i];
    }

    // Add rows for each valid placement
    int row_cols[MAX_CELLS + 1];

    for (int shape_idx = 0; shape_idx < shape_count_len; shape_idx++) {
        if (shape_counts[shape_idx] == 0) continue;

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
                    int num_cols = 0;

                    // Cell columns
                    for (int ci = 0; ci < orient->count; ci++) {
                        int cr = row + orient->cells[ci].r;
                        int cc = col + orient->cells[ci].c;
                        row_cols[num_cols++] = cr * width + cc;
                    }

                    add_row(row_cols, num_cols, shape_idx);
                }
            }
        }
    }

    // Add slack rows (one per cell) - these cover single cells
    if (slack > 0) {
        for (int cell = 0; cell < width * height; cell++) {
            row_cols[0] = cell;
            add_row(row_cols, 1, ROW_ID_SLACK);
        }
    }

    return search_with_counts() ? 1 : 0;
}

// ============== Main ==============

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

    for (int i = 0; i < num_regions; i++) {
        Region *reg = &regions[i];
        int result = solve_region(reg->width, reg->height,
                                  reg->shape_counts, reg->shape_count);
        if (result == 1) {
            count++;
        }

        if ((i + 1) % 50 == 0) {
            printf("Processed %d/%d: %d yes, %d no\n",
                   i + 1, num_regions, count, i + 1 - count);
            fflush(stdout);
        }
    }

    printf("\nResults: %d yes, %d no\n", count, num_regions - count);
    printf("Part 1: %d\n", count);
    fflush(stdout);

    return 0;
}
