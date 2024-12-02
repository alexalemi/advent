#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#define N 1000

void populateArrays(int *one, int *two) {
  FILE *file = fopen("../input/01.txt", "r");
  if (file == NULL) {
    fprintf(stderr, "Error: Could not open file\n");
    exit(1);
  }

  int a;
  int b;
  int i = 0;

  while (fscanf(file, "%d %d", &a, &b) == 2) {
    one[i] = a;
    two[i] = b;
    i++;
  }
  fclose(file);
}

int compare(const void *a, const void *b) { return (*(int *)a - *(int *)b); }

int part1(int *one, int *two) {
  qsort(one, N, sizeof(int), compare);
  qsort(two, N, sizeof(int), compare);

  int total = 0;
  for (size_t i = 0; i < N; i++) {
    total += abs(one[i] - two[i]);
  }
  return total;
}

// Figure out how many times a
// number appears in a sorted array
int counts(int num, const int *two) {
  int count = 0;
  int *found = bsearch(&num, two, N, sizeof(int), compare);
  if (found) {
    int *pos = found;
    while (pos < two + N && *pos == num) {
      count++;
      pos++;
    }
    pos = found - 1;
    while (pos >= two && *pos == num) {
      count++;
      pos--;
    }
  }
  return count;
}

int part2(int *one, int *two) {
  int total = 0;
  for (size_t i = 0; i < N; i++) {
    total += one[i] * counts(one[i], two);
  }
  return total;
}

int main() {
  int one[N];
  int two[N];

  populateArrays(one, two);

  const int ans1 = part1(one, two);
  printf("Answer 1: %d\n", ans1);

  const int ans2 = part2(one, two);
  printf("Answer 2: %d\n", ans2);
}
