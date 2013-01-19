package com.thishood

import com.thishood.domain.Chat
import com.thishood.domain.ChatMember

import com.thishood.domain.User

/**
 * @see IndividualChatService
 * @see ChatMessageService
 */
class ChatMemberService extends AbstractHolderableService {

	static transactional = true

	ChatMember addMember(params) {
		Chat chat = Chat.getOrFail(params.chatId)
		User user = User.getOrFail(params.userId)

		ChatMember chatMember = new ChatMember(
				chat: chat,
				user: user,
				lastRead: chat.dateCreated
		)

		if (!chatMember.validate()) {
			throw new DomainValidationException(chatMember)
		}

		chatMember.save(failOnError: true, flush: true)
	}

	ChatMember findByUserAndChat(Long userId, Long chatId) {
		User user = User.getOrFail(userId)
		Chat chat = Chat.getOrFail(chatId)

		ChatMember.find("from ChatMember cm where cm.user=:user and cm.chat=:chat", [user:user, chat:chat])
	}

	List<ChatMember> findAllMembers(Long chatId) {
		Chat chat = Chat.getOrFail(chatId)

		ChatMember.findAll("from ChatMember cm where cm.chat=:chat", [chat:chat])
	}

	List<ChatMember> findAllMemberships(Long userId) {
		User user = User.getOrFail(userId)

		//where user is member in chats
		List<ChatMember> members = ChatMember.findAll("from ChatMember cm where cm.user=:user", [user:user])

		members
	}
}
