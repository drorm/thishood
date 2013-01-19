package com.thishood.domain

/**
 * @see Chat
 * @see ChatMessage
 */
class ChatMessage {
	Chat chat
	ChatMember author
	String content
	ChatMessageType type
	Date dateCreated
	Date lastUpdated

	static constraints = {
		author nullable: false
		content nullable: false, blank: false
		type nullable: false
	}

	static mapping = {
		content type: "text"
	}

}
