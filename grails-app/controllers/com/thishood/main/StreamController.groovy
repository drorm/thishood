package com.thishood.main

import com.thishood.ContentAction
import com.thishood.domain.ConvertUtils
import com.thishood.domain.Thread
import com.thishood.domain.ThreadReply
import grails.converters.JSON
import grails.plugins.springsecurity.Secured
import java.text.ParsePosition
import java.text.SimpleDateFormat
import com.thishood.image.ImageSize

@Secured(["isAuthenticated()"])
class StreamController {

	def springSecurityService
	def userService
	def userGroupService
	def threadService
	def threadReplyService
	def pingService
	def membershipService

	static allowedMethods = [createThread: "POST", deleteThread: "POST", createReply: "POST", deleteReply: "POST"]

	def path = servletContext.getContextPath()

	/**
	 * Gets request 'group' and returns {@link com.thishood.domain.Membership}
	 * @throws RuntimeException when 'group' is not correct or user doesn't member of this group
	 */
	private getMembership = {
		def groupId = ConvertUtils.toLong(params.groupId)
		if (!groupId) throw new RuntimeException("Argument 'groupId' is not correct")
		def user = springSecurityService.currentUser
		def membership = membershipService.findByUserAndGroup(user.id, groupId)
		if (!membership) throw new RuntimeException("User ${user.id} is not a member of group ${groupId}")

		membership
	}

	private getUserGroups = {
		def userGroups

		def groupId = ConvertUtils.toLong(params.groupId)

		if (groupId) {
			userGroups = [userGroupService.findByUserAndGroup(springSecurityService.currentUser.id, groupId)]
		}
		if (!userGroups?.size()) {
			//for case when groupId is not specified
			userGroups = userGroupService.findAllByUser(springSecurityService.currentUser.id)
		}
		userGroups
	}

	private getThreadInstance = {
		def thread

		def threadId = ConvertUtils.toLong(params.threadId)

		def userGroups = getUserGroups()
		if (userGroups?.size()) {
			thread = threadService.getById(userGroups, threadId)
		}
		thread
	}

	private parseDate(String date) {
		if (date) {
			date = date.substring(0, date.length() - 1) + "GMT-00:00"
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz").parse(date, new ParsePosition(0))
		}
		return null;
	}

	private addThread(resultThreads, replies, Thread thread) {
		if (thread) {
			def fromUser = thread.fromUser
			def userGroup = thread.userGroup
			def actions = threadService.getActions(thread.id, springSecurityService.currentUser.id)
			resultThreads.add([
					threadId: thread.id,
					dateCreated: thread.dateCreated,
					dateUpdated: thread.dateUpdated,
					message: thread.message,
					userId: fromUser.id,
					userName: fromUser.displayName,
					groupId: userGroup.id,
					groupName: userGroup.name,
					repliesCount: thread.repliesCount,
					replies: replies,
					userImage: userService.getPhotoUrl(fromUser.id, ImageSize.TINY),
					groupImage: userGroupService.getImageUrl(userGroup.id, ImageSize.THUMB),
					actions: actions.collectAll{it.name()}
			])
		}
	}

	private addReply(resultReplies, threadId, ThreadReply reply) {
		if (reply) {
			def fromUser = reply.fromUser
			def actions = threadReplyService.getActions(reply.id, springSecurityService.currentUser.id)
			resultReplies.add([
					threadId: threadId,
					replyId: reply.id,
					dateCreated: reply.dateCreated,
					dateUpdated: reply.dateUpdated,
					reply: reply.reply,
					userId: fromUser.id,
					userName: fromUser.displayName,
					userImage: userService.getPhotoUrl(fromUser.id, ImageSize.THUMB),
					actions: actions.collectAll {it.name()}
			])
		}
	}

	private addReplies(resultReplies, Thread thread) {
		def threadId = thread.id
		//todo vitaliy@03.03.11 move to service -- repeatable reads
		def replies = ThreadReply.findAllByThread(thread, [sort: "dateUpdated", order: "desc", max: 2])
		for (ir in 1..0) {
			addReply(resultReplies, threadId, replies[ir])
		}
	}

	private addReplies(resultReplies, Thread thread, Date fromDate) {
		def threadId = thread.id
		//todo vitaliy@03.03.11 move to service -- repeatable reads
		def replies = ThreadReply.findAllByThreadAndDateUpdatedGreaterThanEquals(thread, fromDate, [sort: "dateUpdated", order: "desc"])
		int repliesSize = replies.size()
		if (repliesSize > 0) {
			for (ir in (replies.size() - 1)..0) {
				addReply(resultReplies, threadId, replies[ir])
			}
		}
	}

	def getThreads = {
		def resultThreads = []
		def userGroups = getUserGroups()

		if (userGroups?.size()) {
			def threadId = ConvertUtils.toLong(params.threadId)
			Date fromDate = parseDate(params.fromDate)
			Date toDate = parseDate(params.toDate)

			def threads = threadService.findByIdAndGroupsAndPeriod(threadId, userGroups, fromDate, toDate)
			int threadsSize = threads.size()

			if (threadsSize > 0) {
				def range = fromDate ? (threadsSize - 1)..0 : 0..(threadsSize - 1)
				for (ith in range) {
					def thread = threads[ith]
					def replies = []
					if (thread.repliesCount > 0) {
						if (fromDate) {
							// Get all replies after the date
							addReplies(replies, thread, fromDate)
						} else {
							// Get last 2 replies
							addReplies(replies, thread)
						}
					}
					addThread(resultThreads, replies, thread)
				}
				if (!toDate) {
					pingService.update(springSecurityService.currentUser.id, resultThreads[0].dateUpdated)
				}
			}
		}

		render resultThreads as JSON
	}

	def getThreadCount = {
		def count = 0
		def userGroups = getUserGroups()
		if (userGroups?.size()) {
			def threadId = ConvertUtils.toLong(params.threadId)
			Date fromDate = parseDate(params.fromDate)
			count = threadService.countUpdatedFromDate(threadId, userGroups, fromDate)
			pingService.update(springSecurityService.currentUser.id, fromDate)
		}

		render count
	}

	def getReplies = {
		def replies = []
		def thread = getThreadInstance()
		if (thread) {
			def replyInstances = ThreadReply.findAllByThread(thread, [sort: "dateUpdated", order: "asc"])
			replyInstances.each {
				addReply(replies, thread.id, it)
			}
		}
		render replies as JSON
	}

	def createThread = {
		def response = []

		try {
			def thread = threadService.create(
					fromUserId: springSecurityService.currentUser.id,
					userGroupId: ConvertUtils.toLong(params.groupId),
					message: params.message,
			)
			if (!thread.hasErrors()) {
				addThread(response, [], thread)
			} else {
				response = thread.errors
			}
		} catch (any) {
			log.error("Unable to create a thread ", any)
			//todo roman display nice error
			response = [errors:"Unknown error"]
		}
		render response as JSON
	}

	def deleteThread = {
		def response = [:]

		def threadId = ConvertUtils.toLong(params.threadId)
		def user = springSecurityService.currentUser

		try {
			threadService.deleteThread(threadId, user.id)
			response = [ok: true]
		} catch (any) {
			log.error("Can't delete post [${threadId}], user [${user}]", any)
			response = [errors: true]
		}
		render response as JSON
	}

	def disableComments = {
		def response = [:]

		def threadId = ConvertUtils.toLong(params.threadId)
		def user = springSecurityService.currentUser

		try {
			threadService.disableComments(threadId, user.id)
			response = [ok: true]
		} catch (any) {
			log.error("Can't disable comments of [${threadId}] for user [${user}]", any)
			response = [errors:true]
		}

		render response as JSON
	}

	def enableComments = {
		def response = [:]

		def threadId = ConvertUtils.toLong(params.threadId)
		def user = springSecurityService.currentUser

		try {
			threadService.enableComments(threadId, user.id)
			response = [ok:true]
		} catch (any) {
			log.error("Can't enable comments of [${threadId}] for user [${user}]", any)
			response = [errors:true]
		}

		render response as JSON
	}

	def createReply = {
		def response = []
		def user = springSecurityService.currentUser

		def threadId = params.threadId as Long

		Date fromDate = parseDate(params.fromDate)

		try {
			def threadReply = threadReplyService.addReply(
					fromUserId: user.id,
					threadId: threadId,
					reply: params.reply
			)
			if (!threadReply.hasErrors()) {
				def thread = threadReply.thread
				if (fromDate) {
					// Return all replies after the date
					addReplies(response, thread, fromDate)
				} else {
					// Return saved reply
					addReply(response, thread.id, threadReply)
				}
			} else {
				response = threadReply.errors
			}
		} catch (any) {
			log.error("Error occured on reply made by [${user}] of thread [${threadId}]",any)
			response = [errors: 'Error occured ' + any.message]
		}


		render response as JSON
	}

	def deleteReply = {
		def result = [:]

		def user = springSecurityService.currentUser
		def replyId = params.replyId as Long

		try {
			threadReplyService.deleteReply(replyId, user.id)
			result = [ok: true]
		} catch (any) {
			log.error("User [${user}] unable to delete reply [${replyId}]",any)
			result = [errors: "Error: ${any.message}"]
		}

		render result as JSON
	}

	def getUser = {
		def userId = params.userId as Long

		def user = userService.getUser(userId)

		def result = [
				first: user.first,
				middle: user.middle,
				last: user.last,
				image: userService.getPhotoUrl(userId, ImageSize.SMALL),
				aboutMe: user.aboutMe
		]
		render result as JSON
	}

	/*
	def getUsersOnline = {
		def usersResult = []
		def membership = getMembership()

		def userPings = pingService.findActiveUsers(membership.userGroup.id)

		userPings.each { userPing ->
			def user = userPing.user
			usersResult.add([
					userId: user.id,
					name: user.first + " " + user.last,
					image: userService.getImagePath(user.id)
			])
		}

		render usersResult as JSON
	}
    */
}
