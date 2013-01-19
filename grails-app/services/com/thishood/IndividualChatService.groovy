package com.thishood

import java.util.List;
import com.thishood.domain.*

/**
* @see ChatMessageService
 */
class IndividualChatService extends AbstractHolderableService{

    static transactional = true

	IndividualChat create(params) {
		User creator = User.getOrFail(params.creatorId)
		String topic = params.topic

		IndividualChat chat = new IndividualChat(
				creator: creator,
				topic: topic
		)

		if (!chat.validate()) {
			throw new DomainValidationException(chat)
		}

		chat.save(failOnError: true, flush: true)
	}

}
