package com.thishood.domain


class UserNotificationTimeline extends NotificationTimeline {
	UserNotification userNotification

	static constraints = {
		userNotification nullable: false
	}
	static mapping = {
		discriminator "user"
	}
}
