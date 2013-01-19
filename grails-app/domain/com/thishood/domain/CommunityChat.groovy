package com.thishood.domain

/**
 * @see IndividualChat
 */
class CommunityChat extends Chat {
	UserGroup community

	CommunityChat() {
		setDiscriminatorType(ChatDiscriminatorType.COMMUNITY)
	}

    static constraints = {
		community nullable: true // due mapping hierarchy-per-table
    }

	static mapping = {
		discriminator ChatDiscriminatorType.COMMUNITY.name()
	}


}
