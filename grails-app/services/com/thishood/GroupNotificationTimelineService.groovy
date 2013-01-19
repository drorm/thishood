package com.thishood

import com.thishood.domain.User

import com.thishood.domain.Membership
import com.thishood.domain.GroupNotificationTimeline

/**
* @see UserNotificationTimelineService
 */
class GroupNotificationTimelineService {

    static transactional = true

	def notificationTimelineService

	void deleteByMembership(Long membershipId, Long userId) {
		User user = User.getOrFail(userId)
		Membership membership = Membership.getOrFail(membershipId)

		GroupNotificationTimeline groupNotificationTimeline = GroupNotificationTimeline.find("from GroupNotificationTimeline e where e.membership = :membership", [membership: membership])

		if (groupNotificationTimeline) delete(groupNotificationTimeline.id, user.id)
	}

    void delete(Long groupNotificationTimelineId, Long userId) {
		User user = User.getOrFail(userId)
		GroupNotificationTimeline groupNotificationTimeline = GroupNotificationTimeline.getOrFail(groupNotificationTimelineId)

		//todo add check if user has permission to delete it

		log.info("User [${user}] deletes groupNotificationTimeline [${groupNotificationTimeline}]")

		notificationTimelineService.delete(groupNotificationTimeline.id, userId)
    }
}
