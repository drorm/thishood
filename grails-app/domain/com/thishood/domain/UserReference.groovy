package com.thishood.domain

class UserReference {

	User forUser
	User fromUser
	Integer score
	String subject
	String message
	Date dateCreated

	static constraints = {
		forUser nullable: false
		fromUser nullable: false
		score nullable: false, range: -1..1
		subject nullable: false, blank: false
		message nullable: false, blank: false
		dateCreated nullable: false
	}

	static mapping = {
		message type: "text"
	}

	String toString() {
		message
	}

}