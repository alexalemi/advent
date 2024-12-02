#pragma once

#include <stdint.h>

// A Dynamic array library.

typedef struct {
	int64_t* data;
	size_t size;
	size_t capacity;
} IntArray;

// Initialize with default capacity
IntArray array_create(const size_t initialSize) {
	return (IntArray){
		.data = malloc(initialSize * sizeof(int64_t)),
		.size = 0,
		.capacity = initialSize
	};
}

// Push a new value on the array
void array_push(IntArray* arr, int64_t value) {
	if (arr->size >= arr->capacity) {
		arr->capacity *= 2;
		arr->data = realloc(arr->data, arr->capacity * sizeof(int64_t));
	}
	arr->data[arr->size++] = value;
}

// Free the array
void array_free(IntArray* arr){
	free(arr->data);
	arr->data = NULL;
	arr->size = arr->capacity = 0;
}

int compare(const void *a, const void *b) { return (*(int64_t *)a - *(int64_t *)b); }

// Sort the array
void array_sort(IntArray* arr) {
  qsort(arr->data, arr->size, sizeof(int64_t), compare);
}

int64_t array_get(const IntArray* arr, size_t index) {
	return arr->data[index];
}

void array_set(IntArray* arr, size_t index, int64_t value) {
	arr->data[index] = value;
}
