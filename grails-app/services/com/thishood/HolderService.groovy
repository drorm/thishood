package com.thishood

import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext

/**
 * @see AbstractHolderableService
 */
class HolderService implements ApplicationContextAware {

	static transactional = false

	private ApplicationContext applicationContext

	void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext
	}

	def getUserService() {
		applicationContext.getBean("userService")
	}

	def getUserGroupService() {
		applicationContext.getBean("userGroupService")
	}

	def getHoodService() {
		applicationContext.getBean("hoodService")
	}

	def getGroupService() {
		applicationContext.getBean("groupService")
	}

	def getGroupNewsService() {
		applicationContext.getBean("groupNewsService")
	}

	def getMembershipService() {
		applicationContext.getBean("membershipService")
	}

	def getThreadService() {
		applicationContext.getBean("threadService")
	}

	def getThreadReplyService() {
		applicationContext.getBean("threadReplyService")
	}

	def getNotificationTimelineService() {
		applicationContext.getBean("notificationTimelineService")
	}

	def getMembershipVerificationService() {
		applicationContext.getBean("membershipVerificationService")
	}

	def getChatService() {
		applicationContext.getBean("chatService")
	}

	def getChatMemberService() {
		applicationContext.getBean("chatMemberService")
	}

	def getChatMessageService() {
		applicationContext.getBean("chatMessageService")
	}
}
