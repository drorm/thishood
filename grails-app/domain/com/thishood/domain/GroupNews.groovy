package com.thishood.domain

class GroupNews implements Serializable {
	UserGroup userGroup
	String title
	String content
	Date dateCreated

	static mapping = {
		content type: 'text'
	}
    static constraints = {
		title(nullable: true, blank: false)
		content(nullable: false, blank: false)
		dateCreated(nullable: false, blank: false)
    }
	
	String toString() {
		"[${id}] ${content}"
	}
}
