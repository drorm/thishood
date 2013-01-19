package com.thishood.notification

import com.thishood.domain.*

class NotifyUserService {

	static transactional = true

	def queueMailerService
	def threadService
	def threadReplyService
	def membershipService
	def notificationService
	def grailsApplication
	def messageSource

	def notifyByFrequency(NotificationFrequency frequency) {
		def scroller = User.createCriteria().scroll {
			and {
				eq("enabled", true)
			}
			order("id")
		}

		try {
			boolean isNext = scroller.first()
			while (isNext) {
				//todo vitaliy@22.02.11 this is not clear -- by doc we just have to use 'get()'!!
				User user = scroller.get()[0]

				notifyUserByFrequency(frequency, user)

				isNext = scroller.next();
			}
		} finally {
			scroller.close()
		}

	}

	private def notifyUserByFrequency(NotificationFrequency frequency, User user) {
		UserNotification userNotification = UserNotification.findByUser(user)
		if (!userNotification) {
			userNotification = notificationService.saveUserNotification(userId: user.id)
		}
		List<Membership> memberships = membershipService.findAllByUser(user.id)

		memberships.each {Membership membership ->
			if (membership.frequency == frequency || (membership.frequency == null && userNotification.frequency == frequency)) {
				log.debug("Checking for user [${user}] with group [${membership.userGroup}] frequency [${frequency}]")
				GroupNotification groupNotification = membership.notification ?: userNotification.groupNotification

				GroupNotificationTimeline notificationTimeline = GroupNotificationTimeline.findByMembership(membership)

				def now = new Date()

				if (!notificationTimeline) {
					log.debug "for user [${user.id}] didn't find notificationTimeline, creating a new one"
					notificationTimeline = new GroupNotificationTimeline(
							membership: membership,
							lastProcessed: now,
							attempts: 0
					)
					notificationTimeline.save(failOnError: true)
				} else if (notificationTimeline instanceof List) {
					log.error "unexpected behavior: for user [${user.id}] found more than one notificationTimeline"
					throw new IllegalStateException()
				}

				log.debug "Processing notification ${frequency} for user [${user.id}]"
				if (notificationTimeline.lastProcessed.after(now)) {
					log.error "somehow notificationTimeline with id ${notificationTimeline.id} has processedDate after ${now}"
				} else {
					def posts = groupNotification.postCreated ? threadService.findThreadsCreatedNotByMemberInPeriod(membership.id, notificationTimeline.lastProcessed, now) : []
					threadService.bindReplies(posts)	// we will have to limit? a

					def comments = groupNotification.commentCreated ? threadReplyService.findAllCreatedNotByMemberInPeriod(membership.id, notificationTimeline.lastProcessed, now) : []

					def commentReplies = []//ThreadReply.queryAllNotByUserRepliesInPeriod(user.id, notificationTimeline.lastProcessed, now)

					try {
						Date from = notificationTimeline.lastProcessed
						notificationTimeline.lastProcessed = now
						if (posts || commentReplies || comments) {
							def postComments = ['all': comments]
							processModel(groupNotification, posts, postComments)
							queueMailerService.sendEmail(
									to: userNotification.user.emailWithName,
									subject: getEmailSubject(membership, frequency, from, now),
									model: [
											config: grailsApplication.config,
											membership: membership,
											frequency: messageSource.getMessage("notificationFrequency.enum.${frequency}",null,null),
											period: getFriendlyPeriod(frequency, from, now),
											groupNotification: groupNotification,
											posts: posts,
											postComments: postComments,
											commentReplies: commentReplies
									],
									view: "/template/email/notification/main")

							notificationTimeline.attempts = 0
						}
					} catch (any) {
						notificationTimeline.attempts = notificationTimeline.attempts + 1
						log.error("Unable to send email", any)
					} finally {
						notificationTimeline.save(failOnError: true, flush: true)
					}
				}
			}
		}

	}


	private def processModel(GroupNotification groupNotification, posts, postComments) {
		//by default all comments are by key 'all'
		def comments = postComments.all

		// we have to strip comments which will be already displayed in post
		if (groupNotification.commentCreated && posts) {
			Iterator<ThreadReply> iterator = comments.iterator();
			while (iterator.hasNext()) {
				ThreadReply comment = iterator.next();
				// should we use period check instead of by id?
				def found
				for (Thread post: posts) {
					if (post.id == comment.thread.id) {
						found = true
						break
					}
				}
				if (found) {
					//comment will be displayed in post and it comments section
					iterator.remove()
				}
			}
		}

		if (comments) {
			Iterator<ThreadReply> iterator = comments.iterator();
			while (iterator.hasNext()) {
				ThreadReply comment = iterator.next();
				if (!postComments.containsKey(comment.thread)) {
					postComments[comment.thread] = []
				}
				postComments[comment.thread] << comment
				iterator.remove()
			}
		}
		//on this stage there should be empty collection
		postComments.remove('all')
	}

	def frequencyMapping = [
			(NotificationFrequency.RIGHT_AWAY): [frequency: "Right away", period: {start, finish -> ""}],
			(NotificationFrequency.HOURLY): [frequency: "Hourly", period: {start, finish -> finish.format("d-MMM HH:00")}],
			(NotificationFrequency.THREE_PER_DAY): [frequency: "Three time a day", period: {start, finish -> finish.format("d-MMM HH:00")}],
			(NotificationFrequency.DAILY): [frequency: "Daily", period: {start, finish -> finish.format("d-MMM")}],
			(NotificationFrequency.ONCE_PER_2DAYS): [frequency: "Every other day", period: {start, finish -> finish.format("d-MMM")}],
			(NotificationFrequency.WEEKLY): [frequency: "Weekly", period: {start, finish -> finish.format("d-MMM")}]
	]

	private String getEmailSubject(Membership membership, NotificationFrequency frequency, Date from, Date till) {
		def mapping = frequencyMapping[frequency]

		if (!mapping) log.error("Can't find mapping for notification frequency ${frequency}")

		def frequencyText = mapping?.frequency
		def frequencyPeriod = mapping?.period.call(from, till)
		def result = "Updates"
		//if (frequencyText) result += frequencyText + " notification"
		result += " of " + membership.userGroup.name + " group"
		if (frequencyPeriod) result += ", " + frequencyPeriod
		result += " - ThisHood"
		result
	}

	private String getFriendlyPeriod(NotificationFrequency frequency, Date from, Date till) {
		def result = " "
		if (frequency != NotificationFrequency.RIGHT_AWAY) {
			def dateFrom = from.format("d-MMM")
			def timeFrom = from.format("HH:00")
			def dateTill = till.format("d-MMM")
			def timeTill = till.format("HH:00")

			if (dateFrom == dateTill) {
				result = ", ${dateFrom} ${timeFrom}-${timeTill}"
			} else {
				result = ", ${dateFrom} ${timeFrom} - ${dateTill} ${timeTill}"
			}
		}
		//frequency == NotificationFrequency.RIGHT_AWAY ? " " : ", " + from.format("d-MMM HH:00") + " - " + till.format("d-MMM HH:00")
		result
	}
}
