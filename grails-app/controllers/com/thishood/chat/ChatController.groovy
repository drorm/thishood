package com.thishood.chat

import grails.plugins.springsecurity.Secured
import grails.converters.JSON
import com.thishood.domain.*
import com.thishood.DataAccessDeniedException
import com.thishood.image.ImageSize;

@Secured(['isAuthenticated()'])
class ChatController {
	def springSecurityService
	def privateChatService
	def chatMessageService
	def chatMemberService
	def chatService
	def userService


	def index = {
		def user = springSecurityService.currentUser

		def holders = []
		
		def chats = chatService.findAllUserChats(user.id)
		
		chats.each { Chat chat ->
			def unreadMessages = chatMessageService.findAllUnreadMessages(user.id, chat.id)
			def lastMessage = unreadMessages?unreadMessages[0]:chatMessageService.getLastMessage(chat.id)
			holders.add([
				chat: chat,
				lastMessage: lastMessage,
				unreadMessages: unreadMessages
			])
		}

		[holders: holders, user:user]
	}


	def getMessagesCount = {
		def user = springSecurityService.currentUser

		def count
		try {
			count = chatMessageService.getNumberOfInboxMessages(user.id)
		} catch (any) {
			log.error("Unable to get amount of messages for user [${user}]", any)
		}

		render count
	}

	def createChat = {
		def recipient = User.get(params.recipientId)
		//def community = UserGroup.get(params.communityId)

		if (!recipient) {
			[error: 'Recipient and community should be specified']
		} else {
			[recipient: recipient]
		}
	}


	def saveChat = {
		def result = [:]

		def user = springSecurityService.currentUser

/*
		if (params.chatId) {
			chatMessageService.replyToSinglePrivateMessage(
				chatId: params.chatId,
				authorId: user.id,
				recipientId: params.recipientId,
				topic: params.topic,
				message: params.message
			)
		}

*/
		try{
			chatService.createIndividualChat(
					authorId: user.id,
					recipientId: params.recipientId,
					topic: params.topic,
					message: params.message
			)
			result.success = true
		}catch(any) {
			log.error("Erro on send message by user [${user}]",any)
			result.error = 'Failed to send message'
		}

		render result as JSON
	}

	def chatMessages = {
		def result = [:]
		def user = springSecurityService.currentUser

		Long chatId = params.chatId as Long

		try {
			def chat = Chat.get(chatId)
			if (!chat) throw new IllegalArgumentException("Chat doesn't exist")

			def now = new Date()
			def member = chatMemberService.findByUserAndChat(user.id, chatId)
			if (!member) throw new DataAccessDeniedException("User [${user}] is not a member of chat [${chatId}]")
			def lastReadAt = member.lastRead
			
			return [chat: chat, lastReadAt: lastReadAt, user: user, userPhotoUrl: userService.getPhotoUrl(user.id, ImageSize.THUMB)]
		} catch (any) {
			log.error("Can't get chat messages of [${chatId}] for user [${user}]", any)
			flash.error = "Error on open messages: ${any.message}"
			render view: '/loggedin-info'
		}

	}
	
	def getMessages = {
		def result = [:]
		def user = springSecurityService.currentUser

		Long chatId = params.chatId as Long

		try {
			def chat = Chat.get(chatId)

			def now = new Date()
			def member = chatMemberService.findByUserAndChat(user.id, chatId)
			if (!member) throw new DataAccessDeniedException("User [${user}] is not a member of chat [${chat}]")
			def lastReadAt = member.lastRead.time

			def messages = chatMessageService.getChatMessagesAndSetRead(user.id, chat.id, now)

			def resultMessages = []
			messages.each {ChatMessage message ->
				resultMessages.add([
						authorId: message.author.user.id,
						authorName: message.author.user.displayName.encodeAsHTML(),
						authorPhotoUrl: userService.getPhotoUrl(message.author.user.id, ImageSize.THUMB),
						dateCreated: message.dateCreated.time,
						dateUpdated: message.lastUpdated.time,
						content: message.content.encodeAsNewLineHTML(),
				])
			}

			result = [messages: resultMessages, lastReadAt: lastReadAt]
		} catch (any) {
			log.error("Can't get chat messages of [${chatId}] for user [${user}]", any)
			result.error = any.message
		}

		render result as JSON
	}

	def saveMessage= {
		def result = [:]

		User user = springSecurityService.currentUser
		Long chatId = params.chatId as Long
		String message = params.message

		try {
			ChatMessage newMsg = chatMessageService.addMessage(
					chatId: chatId,
					authorId: user.id,
					message: message
			)
			result.message = newMsg
			result.success = true
		} catch (any) {
			log.error("Can't save message by user [${user}] due ", any)
			result.error = "Can't save message"
		}

		render result as JSON
	}

	def inbox = {
		def user = springSecurityService.currentUser

		def messages = chatMessageService.getInboxMessages(user.id)

		[messages: messages]
	}

	def setTopic = {
		def chatId = params.chatId as Long
		def topic = params.value
		
		try {
			chatService.setTopic(chatId, topic)
		} catch (any) {
			log.error("Can't change topic due ", any)
		}

		render topic
	}

}
