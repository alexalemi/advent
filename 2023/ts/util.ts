

function assert(statement) {
	if (!statement) {
		throw Exception("Failed Assertion!")
	}
}

export { assert };
