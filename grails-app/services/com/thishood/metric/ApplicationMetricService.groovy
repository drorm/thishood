package com.thishood.metric

import com.thishood.AbstractHolderableService
import com.thishood.domain.*

/**
 * Takes snapshot (count) of data and persists for further visualization or analytics
 */
class ApplicationMetricService extends AbstractHolderableService {

	static transactional = true

	// keep here count instead of service/domain because they are pretty simple
	// should be moved out to method in case if some complexity or logic will be added
	def actions = [
			(ApplicationMetricType.USERS): {User.count()},
			(ApplicationMetricType.COMMUNITIES): {UserGroup.count()},
			(ApplicationMetricType.COMMUNITY_GROUPS): {Group.count()},
			(ApplicationMetricType.COMMUNITY_HOODS): {Hood.count()},
			(ApplicationMetricType.MEMBERSHIPS): {Membership.count()},
			(ApplicationMetricType.POSTS): {Thread.count()},
			(ApplicationMetricType.COMMENTS): {ThreadReply.count()},
			(ApplicationMetricType.CHATS): {Chat.count()},
			(ApplicationMetricType.CHAT_MESSAGES): {ChatMessage.count()},
			(ApplicationMetricType.RECOMMENDATIONS): {UserReference.count()},
			(ApplicationMetricType.UPLOADS): {Upload.count()}
	]

	void gatherAll() {
		actions.each {ApplicationMetricType type, Closure closure ->
			int value = closure.call()
			ApplicationMetric metric = new ApplicationMetric(type: type, value: value)
			metric.save(failOnError: true, flush: true)
		}
	}

}
