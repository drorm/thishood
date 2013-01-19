package com.thishood.domain

/**
 * Common user notification settings
 *
 * Keep all 'purposes' as Boolean fields to reduce SQL queries produced and keep it all in one place
 * This object is in 'one-to-one' relationship with {@link User} to decouple it
 *
 * @see NotificationTimeline
 * @see GroupNotification
 */
class UserNotification {
	User user

	NotificationFrequency frequency

	GroupNotification groupNotification

	static constraints = {
		user(nullable: false)
		frequency(nullable: false)
		groupNotification(nullable: false)
	}

	static embedded = ['groupNotification']
}
