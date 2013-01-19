package com.thishood.domain

class GroupNotificationTimeline extends NotificationTimeline {
 	Membership membership

	static constraints = {
		membership nullable: false
	}

	static mapping = {
		discriminator "group"
	}
}
