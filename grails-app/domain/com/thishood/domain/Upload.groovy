package com.thishood.domain

class Upload {

	User user
	String uid
	String name
	String ext
	String contentType
	Long length
	Date dateCreated

	static constraints = {
		user nullable: false
		uid nullable: false, blank: false
		name nullable: false, blank: false
		ext nullable: true, blank: true
		contentType nullable: true, blank: true
		length min: 0L
	}

	static transients = ["filename", "niceLength", "url"]

	String toString() {
		uid
	}

	def grailsApplication

	def getUrl() {
		grailsApplication.config.grails.serverURL + "/uploads/view/" + uid
	}

	/**
	 * marker for Foxtrot only
	 */
	static final String NOT_NEEDED = "-not-needed-"
}