package com.thishood

import com.thishood.domain.Chat;
import com.thishood.domain.ChatMember;
import com.thishood.domain.ChatMessage;
import com.thishood.domain.ChatMessageType;


import com.thishood.domain.User;

/**
* @see IndividualChatService
 */
class ChatMessageService extends AbstractHolderableService{
	static transactional = true
	
	def privateChatService
	def chatMemberService
	def chatNotificationService

	Integer getNumberOfInboxMessages (Long userId) {
		User user = User.getOrFail(userId)

		List<ChatMessage> chatMessages = getInboxMessages(userId)

		chatMessages.size()
	}


	ChatMessage addMessage(params) {
		Chat chat = Chat.getOrFail(params.chatId)
		User authorUser = User.getOrFail(params.authorId)
		ChatMember author = chatMemberService.findByUserAndChat(authorUser.id, chat.id)

		chat.lock()

		String content = params.message

		ChatMessage chatMessage = new ChatMessage(
				chat: chat,
				author: author,
				content: content,
				type: ChatMessageType.COMMON
		)

		if (!chatMessage.validate())  {
			throw new DomainValidationException(chatMessage)
		}

		chatMessage = chatMessage.save(failOnError:true, flush:true)
		chat.lastUpdated = new Date()

		markMessagesAsRead(authorUser.id, chat.id, new Date())

		chatNotificationService.notifyOfNewMessage(chatMessage.id)

		chatMessage
	}


	void replyToSinglePrivateMessage (params) {
		User sender = User.getOrFail(params.authorId);
		User recipient = User.getOrFail(params.recipientId)
		Chat chat = Chat.getOrFail(params.chatId)
		
		String topic = params.topic
		String message = params.message

		ChatMember senderChatMember = ChatMember.findByChatAndUser(chat,sender)
		
		ChatMessage chatMessage = addMessage(
				chatId: chat.id,
				authorId: senderChatMember.id,
				message: message
		)
	}
	
	List<ChatMessage> getInboxMessages(Long userId) {
		User user = User.getOrFail(userId)

		List<ChatMessage> messages = []

		// we can optimize?
		List<ChatMember> members = chatMemberService.findAllMemberships(userId)

		for (member in members) {
			List<ChatMessage> unreadMessages = ChatMessage.findAll("from ChatMessage cm where cm.chat=:chat and cm.lastUpdated>:lastRead", [chat:member.chat, lastRead: member.lastRead])
			messages.addAll(unreadMessages)
		}

		//didn't find any
		return messages
	}

	List<ChatMessage> findAllUnreadMessages(Long userId, Long chatId) {
		User user = User.getOrFail(userId)
		Chat chat = Chat.getOrFail(chatId)

		ChatMember member = chatMemberService.findByUserAndChat(user.id, chat.id)
		if (!member) throw new IllegalArgumentException("Can't find chat member by user [${user}] and chat [${chat}]")

		ChatMessage.findAll("from ChatMessage cm where cm.chat=:chat and cm.lastUpdated>:lastRead order by cm.lastUpdated ASC", [chat:chat, lastRead: member.lastRead])
	}

	ChatMessage getLastMessage(Long chatId) {
		Chat chat = Chat.getOrFail(chatId)

		ChatMessage last = ChatMessage.findByChat(chat, [max: 1, sort: "dateCreated", order: "desc"])

		return last
	}
	
	List<ChatMessage> getChatMessages(Long chatId) {
		Chat chat = Chat.getOrFail(chatId)

		List<ChatMessage> messages = ChatMessage.findAll("from ChatMessage cm where cm.chat=:chat order by cm.lastUpdated ASC", [chat:chat])

		messages
	}

	void markMessagesAsRead(Long userId, Long chatId, Date date) {
		User user = User.getOrFail(userId)
		Chat chat = Chat.getOrFail(chatId)

		if (!chatService.hasAction(chat.id,  user.id, ContentAction.Chat.VIEW)) throw new DataAccessDeniedException("User [${user}] is not a member of chat [${chat}]")

		ChatMember member = chatMemberService.findByUserAndChat(user.id, chat.id)
		if (!member) throw new IllegalArgumentException("Can't find chat member by user [${user}] and chat [${chat}]")

		member.lastRead = date

		member.save(flush:true, failOnError:true)
	}

	List<ChatMessage> getChatMessagesAndSetRead(Long userId, Long chatId, Date date = new  Date()) {
		User user = User.getOrFail(userId)
		Chat chat = Chat.getOrFail(chatId)

		if (!chatService.hasAction(chat.id,  user.id, ContentAction.Chat.VIEW)) throw new DataAccessDeniedException("User [${user}] is not a member of chat [${chat}]")

		List<ChatMessage> messages = getChatMessages(chat.id)

		markMessagesAsRead(user.id, chat.id, date)

		messages
	}

}
