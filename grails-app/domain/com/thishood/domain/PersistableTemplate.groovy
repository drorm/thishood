package com.thishood.domain

class PersistableTemplate {
	UserGroup userGroup
	String content

	Date dateCreated

	static mapping = {
		content type: 'text'
	}
    static constraints = {
		userGroup nullable: false
		content nullable: false
    }
}
