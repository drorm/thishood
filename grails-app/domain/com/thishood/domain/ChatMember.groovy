package com.thishood.domain

/**
 * @see ChatMessage
 */
class ChatMember {
	Chat chat
	User user
	Date dateCreated
	Date lastUpdated

	Date lastRead

    static constraints = {
		chat nullable: false
		user nullable: false
    }
}
