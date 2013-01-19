package com.thishood.domain

class UserReferenceType {

	String type

	static constraints = {
		type nullable: false, blank: false
	}

	String toString() {
		type
	}

}