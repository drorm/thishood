package com.thishood.domain

class Prospect {

	String email
	String city
	String state
	Date dateCreated

	static constraints = {
		email(blank: false, email: true, unique: true)
		city(blank: false)
		state(blank: false)
	}

}