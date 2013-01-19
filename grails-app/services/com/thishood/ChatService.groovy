package com.thishood

import com.thishood.domain.*
import org.apache.commons.lang.StringUtils

class ChatService {

    static transactional = true

	def individualChatService
	def chatMemberService
	def chatMessageService

	void createIndividualChat(params) {
		User sender = User.getOrFail(params.authorId);
		User recipient = User.getOrFail(params.recipientId)

		String topic = StringUtils.trim(params.topic)
		String message = StringUtils.trim(params.message)

		IndividualChat chat = individualChatService.create(
				creatorId: sender.id,
				topic: topic
		)

		createChat(chat, sender, recipient, message)
	}

	void createCommunityChat(params) {
		//todo implement
	}

	void createChat(Chat chat, User sender, User recipient, String message) {
		ChatMember senderChatMember = chatMemberService.addMember(
				chatId: chat.id,
				userId: sender.id
		)

		ChatMember recipientChatMember = chatMemberService.addMember(
				chatId: chat.id,
				userId: recipient.id
		)

		ChatMessage chatMessage = chatMessageService.addMessage(
				chatId: chat.id,
				authorId: sender.id,
				message: message
		)

	}


	List<Chat> findAllUserChats(Long userId) {
		User user = User.getOrFail(userId)

		List<ChatMember> memberships = chatMemberService.findAllMemberships(userId)

		//taking chats from participations and order by lastUpdated which changes on adding new message
		List<Chat> chats = memberships.collectAll {ChatMember chatMember -> chatMember.chat}.sort {Chat chat -> chat.lastUpdated}.reverse()

		return chats
	}


	Boolean hasAction(Long chatId, Long userId, ContentAction.Chat action) {
		def actions = getActions(chatId, userId)

		actions.contains(action)
	}

	def getActions(Long chatId, Long userId) {
		Chat chat = Chat.getOrFail(chatId)
		User user = User.getOrFail(userId)

		def actions = []
		//
		def member = chatMemberService.findByUserAndChat(user.id, chat.id)
		def isOwner = chat.creator.id == user.id

		if (member) actions.add(ContentAction.Chat.VIEW)
		if (member && isOwner) actions.add(ContentAction.Chat.UPDATE_TOPIC)

		actions
	}

	Chat setTopic (Long chatId, String newTopic) {
		Chat chat = Chat.getOrFail(chatId)
		chat.topic = newTopic
		chat.save()
	}

}
