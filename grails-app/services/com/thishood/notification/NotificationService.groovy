package com.thishood.notification

import com.thishood.domain.GroupNotification
import com.thishood.domain.NotificationFrequency
import com.thishood.domain.User
import com.thishood.domain.UserNotification
import com.thishood.domain.GroupNews

class NotificationService {

	static transactional = true

	/**
	 * By specified user return command object with notification settings
	 * @param userId user ID
	 * @return UserNotificationCommand
	 */
	UserNotification findUserNotificationByUser(def userId) {
		User user = User.getOrFail(userId)

		UserNotification userNotification = UserNotification.findByUser(user)

		userNotification ?: getDefaultUserNotification()
	}

	/**
	 * Persisting to database user notification settings
	 *
	 * @param command
	 * @return
	 * @see com.thishood.userSettings.UserNotificationCommand
	 */
	UserNotification saveUserNotification(params) {
		User user = User.getOrFail(params.userId)

		UserNotification userNotification = UserNotification.findByUser(user)

		if (!userNotification) {
			log.warn("Creating new userNotification for user [${user.id}]")
			userNotification = getDefaultUserNotification()
			userNotification.user = user
		} else {
			//embedded
			userNotification.groupNotification = params.groupNotification?:new GroupNotification()
			userNotification.frequency = params.frequency
		}

		userNotification.save(failOnError: true, flush: true)
	}

	/**
	 * Create command and fill it with all transport/purpose/frequency data
	 * Needed because these data will be overriden from database
	 */
	UserNotification getDefaultUserNotification() {
		new UserNotification(
				frequency: NotificationFrequency.HOURLY,
				groupNotification: new GroupNotification(
						myCommentReply: true,
						postCreated: true,
						commentCreated: true
				)
		)
	}
}
