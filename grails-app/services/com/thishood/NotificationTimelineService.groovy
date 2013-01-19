package com.thishood

import com.thishood.domain.GroupNotificationTimeline
import com.thishood.domain.Membership
import com.thishood.domain.User
import com.thishood.domain.NotificationTimeline

/**
 * @see com.thishood.GroupNotificationTimelineService
 * @see com.thishood.UserNotificationTimelineService
 */

class NotificationTimelineService {

	static transactional = true

	def serviceMethod() {

	}

	void delete(Long notificationTimelineId, Long userId) {
		User user = User.getOrFail(userId)
		NotificationTimeline notificationTimeline = NotificationTimeline.getOrFail(notificationTimelineId)

		//todo add check if user has permission to delete
		//todo add delete of all cascades

		log.info("User [${user}] deletes notificationTimeline [${notificationTimeline}]")

		notificationTimeline.delete(failOnError: true)
	}

	void deleteByMembership(Long membershipId) {
		Membership membership = Membership.getOrFail(membershipId)

		GroupNotificationTimeline groupNotificationTimeline = GroupNotificationTimeline.findByMembership(membership)
		if (groupNotificationTimeline) {
			log.warn("Deleting groupNotification by membership [${membershipId}]")
			groupNotificationTimeline.delete(flush: true)
		}
	}
}
