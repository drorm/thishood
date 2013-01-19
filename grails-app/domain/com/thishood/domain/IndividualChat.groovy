package com.thishood.domain

/**
 * @see CommunityChat
 */
class IndividualChat extends Chat {

	IndividualChat() {
		setDiscriminatorType(ChatDiscriminatorType.INDIVIDUAL)
	}

	static constraints = {
	}

	static mapping = {
		discriminator ChatDiscriminatorType.INDIVIDUAL.name()
	}

}
