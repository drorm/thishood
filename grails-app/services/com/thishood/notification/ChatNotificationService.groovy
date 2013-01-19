package com.thishood.notification

import com.thishood.domain.ChatMessage
import com.thishood.domain.Chat
import com.thishood.domain.ChatMember
import com.thishood.AbstractHolderableService
import java.util.Map.Entry

/**
* Major purpose of this service is to hold notifications and send them in on bunch
 * This is useful because user may add few messages continiously -- use them to emulate new line separation
 */
class ChatNotificationService extends AbstractHolderableService{

    static transactional = true

	def grailsApplication
	def queueMailerService
	def userMessages = [:]

    void notifyOfNewMessage(Long chatMessageId) {
		ChatMessage message = ChatMessage.getOrFail(chatMessageId)
		Chat chat = message.chat
		List<ChatMember> members = chatMemberService.findAllMembers(chat.id)

		//we can't use TO/CC -- so need to send personal emails
		for(ChatMember member:members) {
			if (member.id == message.author.id) {
				//don't send to author
				continue
			}

			if (!userMessages.containsKey(member.id)) {
				userMessages[member.id] = new ArrayList<Long>()
			}
			userMessages[member.id].add(message.id)
		}
    }

	void sendNotifications() {
		for(Map.Entry<Long, List<Long>> entry : userMessages) {
			Long memberId = entry.key
			List<Long> messageIds = entry.value
			if (messageIds.size()>0) {
				entry.value = new ArrayList<Long>()

				ChatMember member = ChatMember.getOrFail(memberId)
				Chat chat = member.chat

				List<ChatMessage> messages = []

				for (Long messageId: messageIds) {
					ChatMessage message = ChatMessage.get(messageId)
					if (message == null) {
						log.warn("Couldn't get message [${messageId}]")
					} else {
						messages.add(message)
					}
				}

				if (messages) {
					log.debug("Sending to ${member.user} personal messages")

					def authors = messages.groupBy {message->message.author.user}

					def subject = messages[0].author.user.displayName
					int authorCount = authors.keySet().size()
					if (authorCount == 1) {
						subject += " sent you a message:"
					} else if (authorCount == 2) {
						subject +=  " and somebody else sent you a message:"
					} else {
						subject += " and " + authorCount + " others sent you a message:"
					}
					subject += " " + chat.topic + ", ThisHood"

					queueMailerService.sendEmail(
							to: member.user.emailWithName,
							subject: subject,
							model: [
									config: grailsApplication.config,
									chat: chat,
									member: member,
									messages: messages
							],
							view: "/template/email/chat/newMessage")
				}
			}
		}
	}
}
