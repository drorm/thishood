package com.thishood.metric

import com.thishood.AbstractHolderableService
import com.thishood.domain.CommunityMetric
import com.thishood.domain.CommunityMetricType
import com.thishood.domain.UserGroup

class CommunityMetricService extends AbstractHolderableService {

	static transactional = true


	List<CommunityMetric> findLast(Long communityId,CommunityMetricType type) {
		UserGroup community = UserGroup.getOrFail(communityId)

		CommunityMetric.findAll("from CommunityMetric cm where cm.community=:community and cm.type=:type order by cm.dateCreated desc", [community: community, type:type], [max: 7])
	}

	void calculateAll(Date date = new Date()) {
		List<UserGroup> communities = userGroupService.findAllActive()
		communities.each {community ->
			gatherCommunity(community.id)
		}
	}

	void gatherCommunity(Long communityId, Date date = new Date()) {
		UserGroup community = UserGroup.getOrFail(communityId)

		calculateMembers(community.id)
		calculatePosts(community.id)
		calculateComments(community.id)
		calculatePopularity(community.id)

	}

	void calculateMembers(Long communityId, Date date = new Date()) {
		UserGroup community = UserGroup.getOrFail(communityId)

		int value = membershipService.countActiveMembers(community.id)

		CommunityMetric metric = new CommunityMetric(
				type: CommunityMetricType.MEMBERS,
				community: community,
				value: value
		)

		metric.save(failOnError: true, flush: true)
	}

	void calculatePosts(Long communityId, Date date = new Date()) {
		UserGroup community = UserGroup.getOrFail(communityId)

		int value = threadService.countPostsPerDay(community.id, date)

		CommunityMetric metric = new CommunityMetric(
				type: CommunityMetricType.POSTS,
				community: community,
				value: value
		)

		metric.save(failOnError: true, flush: true)
	}

	void calculateComments(Long communityId, Date date = new Date()) {
		UserGroup community = UserGroup.getOrFail(communityId)

		int value = threadReplyService.countCommentsPerDay(community.id, date)

		CommunityMetric metric = new CommunityMetric(
				type: CommunityMetricType.COMMENTS,
				community: community,
				value: value
		)

		metric.save(failOnError: true, flush: true)
	}

	void calculatePopularity(Long communityId, Date date = new Date()) {
		UserGroup community = UserGroup.getOrFail(communityId)

		Date d = date.clone()
		double sum = 0
		for (int k = 10; k > 1; k--) {
			int communityPosts = threadService.countPostsPerDay(communityId, d)
			int totalPosts = threadService.countPostsPerDay(d)
			int communityComments = threadReplyService.countCommentsPerDay(communityId, d)
			int totalComments = threadReplyService.countCommentsPerDay(d)
			sum += Math.log(k) * ((totalPosts == 0 ? 0 : communityPosts / totalPosts * 10) + (totalComments == 0 ? 0 : communityComments / totalComments))
			d = d - 1
		}
		int members = membershipService.countActiveMembers(community.id)

		Integer total = Math.round(sum * members)
		CommunityMetric metric = new CommunityMetric(
				type: CommunityMetricType.POPULARITY,
				community: community,
				value: total
		)

		metric.save(failOnError: true, flush: true)

		community.statPopularityIndex = total
		community.save(failOnError: true, flush: true)

	}
}
