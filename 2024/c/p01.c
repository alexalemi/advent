#include "dynarray.h"
#include <assert.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

void populateArrays(IntArray *one, IntArray *two) {
  FILE *file = fopen("../input/01.txt", "r");
  if (file == NULL) {
    fprintf(stderr, "Error: Could not open file\n");
    exit(1);
  }

  int a;
  int b;

  while (fscanf(file, "%d %d", &a, &b) == 2) {
    array_push(one, a);
    array_push(two, b);
  }

  fclose(file);
}

int64_t part1(IntArray *one, IntArray *two) {
  array_sort(one);
  array_sort(two);

  int64_t total = 0;
  for (size_t i = 0; i < one->size; i++) {
    total += labs(array_get(one, i) - array_get(two, i));
  }
  return total;
}

// Figure out how many times a
// number appears in a sorted array
int64_t counts(int64_t num, const IntArray *two) {
  int64_t count = 0;
  int64_t *found =
      bsearch(&num, two->data, two->size, sizeof(int64_t), compare);
  if (found) {
    int64_t *pos = found;
    while (pos < two->data + two->size && *pos == num) {
      count++;
      pos++;
    }
    pos = found - 1;
    while (pos >= two->data && *pos == num) {
      count++;
      pos--;
    }
  }
  return count;
}

int64_t part2(IntArray *one, IntArray *two) {
  int64_t total = 0;
  for (size_t i = 0; i < one->size; i++) {
    const int64_t x = array_get(one, i);
    total += x * counts(x, two);
  }
  return total;
}

int main() {
  IntArray one = array_create(1000);
  IntArray two = array_create(1000);

  populateArrays(&one, &two);

  const int ans1 = part1(&one, &two);
  assert(ans1 == 1222801);
  printf("Answer 1: %d\n", ans1);

  const int ans2 = part2(&one, &two);
  assert(ans2 == 22545250);
  printf("Answer 2: %d\n", ans2);
}
