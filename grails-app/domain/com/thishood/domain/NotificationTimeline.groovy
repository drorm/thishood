package com.thishood.domain

class NotificationTimeline {

	/**
	 * timestamp when notification was processed (or it was a try to process but due some reasons failed)
	 */
	Date lastProcessed

	/**
	 * Keep how many attempts (in other words fails) were performed
	 * This is useful to have because admin can be notified in case if N attempts performed (so, something is not ok in our digital kingdom)
	 */
	Long attempts = 0

	static constraints = {
		lastProcessed(nullable: false)
		attempts(nullable: false)
	}

	static mapping = {
		tablePerHierarchy true
		discriminator column: [name:"discriminator", length:24]
	}

}



