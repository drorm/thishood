package com.thishood.domain

/**
 * @see ChatMember
 * @see ChatMessage
 * @see CommunityChat
 * @see IndividualChat
 */
class Chat implements Serializable{
	/**
	 * Read-only field only for convinience
	 */
	private ChatDiscriminatorType discriminatorType

	String topic
	User creator
	Date dateCreated
	Date lastUpdated

	static constraints = {
		topic nullable: false, blank: false
		creator nullable: false
	}

	static mapping = {
		//table "chat"
		tablePerHierarchy true
		discriminator column: [name: "discriminator", length: 24]
	}

	static transients = ["discriminatorType"]

	// ----------------------------------------------------------------------------------

	protected void setDiscriminatorType(ChatDiscriminatorType discriminatorType) {
		this.discriminatorType = discriminatorType
	}

	ChatDiscriminatorType getDiscriminatorType() {
		this.discriminatorType
	}


}
