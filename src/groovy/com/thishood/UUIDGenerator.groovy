package com.thishood

class UUIDGenerator {
	static String next() {
		UUID.randomUUID().toString().replaceAll('-', '')
	}
}
