package com.thishood

/**
* @see HolderService
 */
abstract class AbstractHolderableService {
	def holderService

	protected def getMembershipService() {
		holderService.membershipService
	}

	protected def getUserService() {
		holderService.userService
	}

	protected def getUserGroupService() {
		holderService.userGroupService
	}

	protected def getHoodService() {
		holderService.hoodService
	}
	protected def getGroupService() {
		holderService.groupService
	}

	protected def getGroupNewsService() {
		holderService.groupNewsService
	}

	protected def getThreadService() {
		holderService.threadService
	}

	protected def getThreadReplyService() {
		holderService.threadReplyService
	}

	protected def getNotificationTimelineService() {
		holderService.notificationTimelineService
	}

	protected def getMembershipVerificationService() {
		holderService.membershipVerificationService
	}

	protected def getChatService() {
		holderService.chatService
	}

	protected def getChatMemberService() {
		holderService.chatMemberService
	}

	protected def getChatMessageService() {
		holderService.chatMessageService
	}

}
