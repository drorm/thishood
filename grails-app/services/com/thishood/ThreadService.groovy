package com.thishood

import com.thishood.domain.*
import org.apache.commons.lang.StringUtils

class ThreadService {

	static transactional = true

	def springSecurityService
	def membershipService
	def userGroupService
	def threadReplyService

	Thread create(params) {
		User author = User.getOrFail(params.fromUserId)
		UserGroup userGroup = UserGroup.getOrFail(params.userGroupId)

		if (!membershipService.findByUserAndGroup(author.id, userGroup.id)) throw new IllegalArgumentException("User [${author}] doesn't has membership in group [${userGroup}]")

		def now = new Date()

		Thread thread = new Thread(
				fromUser: author,
				userGroup: userGroup,
				message: StringUtils.trim(params.message),
				repliesCount: 0,
				dateCreated:  now,
				dateUpdated: now

		)
		if (thread.validate()) {
			thread = thread.save(failOnError: true, flush: true)

			// with current design we have only hasAction which takes ID, so check will be performed only for persisted entity
			if (!hasAction(thread.id, thread.fromUser.id, ContentAction.Post.CREATE)) throw new IllegalArgumentException("User [${author}] is not allowed to create posts in group [${userGroup}]")
		}
		thread
	}

	Thread getById(List<UserGroup> userGroups, Long threadId) {
		def cr = Thread.createCriteria()
		def thread = cr.get {
			and {
				eq("id", threadId)
				or {
					userGroups.each {
						eq("userGroup", it)
					}
				}
			}
		}
		thread
	}

	/**
	 * Adds thread replies to list of threads
	 * @param threads
	 */
	void bindReplies(List<Thread> threads) {
		for (Thread thread: threads) {
			//adding dynamic field
			thread.metaClass.replies = threadReplyService.findAllByThread(thread.id)
		}
	}

	List<Thread> findThreadsCreatedNotByMemberInPeriod(Long membershipId, Date startDate, Date finishDate) {
		Membership membership = Membership.getOrFail(membershipId)

		Thread.findAll("from Thread t where t.fromUser != :user and t.dateCreated between :startDate and :finishDate and t.userGroup=:userGroup", [
				user: membership.user,
				userGroup: membership.userGroup,
				startDate: startDate,
				finishDate: finishDate
		])


	}

	List<Thread> findByIdAndGroupsAndPeriod(Long threadId, List<UserGroup> userGroups, Date fromDate, Date toDate) {
		def cr = Thread.createCriteria()

		def threads = cr {
			and {
				if (threadId) {
					eq("id", threadId)
				}
				if (fromDate) {
					ge("dateUpdated", fromDate)
				} else if (toDate) {
					lt("dateUpdated", toDate)
				}
				or {
					userGroups.each {
						eq("userGroup", it)
					}
				}
			}
			if (fromDate) {
				order("dateUpdated", "asc")
			} else {
				order("dateUpdated", "desc")
			}
			maxResults(20)
		}
		threads
	}

	Integer countUpdatedFromDate(Long threadId, List<UserGroup> userGroups, Date fromDate) {
		def cr = Thread.createCriteria()
		def count = cr.get {
			projections {
				rowCount()
			}
			and {
				if (threadId) {
					eq("id", threadId)
				}
				if (fromDate) {
					gt("dateUpdated", fromDate)
				}
				or {
					userGroups.each {
						eq("userGroup", it)
					}
				}
			}
		}
		count
	}

	int countPostsPerDay(Date date) {
		def (Date lowerDate, Date upperDate) = DateUtil.getLowerAndUpperByDate(date)

		Thread.countByDateCreatedBetween(lowerDate, upperDate)
	}

	int countPostsPerDay(Long communityId, Date date) {
		UserGroup community = UserGroup.getOrFail(communityId)

		def (Date lowerDate, Date upperDate) = DateUtil.getLowerAndUpperByDate(date)

		Thread.countByUserGroupAndDateCreatedBetween(community, lowerDate, upperDate)
	}


	void deleteThread(Long threadId, Long userId) {
		Thread thread = Thread.getOrFail(threadId)
		User user = User.getOrFail(userId)

		if (!hasAction(threadId, userId, ContentAction.Post.DELETE)) throw new IllegalArgumentException("User [${user}] is not authorized to delete post [${thread}]")

		threadReplyService.deleteAllByThread(thread.id)
		thread.delete(failOnError: true)
	}

	void disableComments(Long threadId, Long userId) {
		Thread thread = Thread.getOrFail(threadId)
		User user = User.getOrFail(userId)

		if (!hasAction(threadId, userId, ContentAction.Post.DISABLE_COMMENTS)) throw new IllegalArgumentException("User [${user}] can't disable comments for post [${thread}]")

		thread.disableComments = true

		thread.save(failOnError: true, flush: true)
	}

	void enableComments(Long threadId, Long userId) {
		Thread thread = Thread.getOrFail(threadId)
		User user = User.getOrFail(userId)

		if (!hasAction(threadId, userId, ContentAction.Post.ENABLE_COMMENTS)) throw new IllegalArgumentException("User [${user}] can't enable comments for post [${thread}]")

		thread.disableComments = false

		thread.save(failOnError: true, flush: true)
	}

	void deleteAllThreadsByGroup(Long groupId, Long userId) {
		UserGroup group = UserGroup.getOrFail(groupId)
		User user = User.getOrFail(userId)

		//if (!hasAction(threadId, userId, ContentAction.Post.DELETE)) throw new IllegalArgumentException("User [${user}] is not authorized to delete post [${thread}]")

		threadReplyService.deleteAllByGroup(group.id)
		Thread.findAll("from Thread t where t.userGroup=:group", [group: group]).each {Thread post ->
			post.delete(flush: true)
		}

	}

	Boolean hasAction(Long threadId, Long userId, ContentAction.Post action) {
		def actions = getActions(threadId, userId)

		actions.contains(action)
	}

	def getActions(Long threadId, Long userId) {
		Thread thread = Thread.getOrFail(threadId)
		User user = User.getOrFail(userId)

		def actions = []
		//
		def activeGroup = thread.userGroup.status == GroupStatus.ACTIVE
		def membership = membershipService.findByUserAndGroup(userId, thread.userGroup.id)
		def isOwner = thread.fromUser.id == user.id
		def isRoleAdmin = user.hasRole(UserRole.ROLE_ADMIN)
		def isAuthorityAdmin = membership?.hasAuthority(MembershipAuthority.ADMIN)

		if (activeGroup && membership) actions.add(ContentAction.Post.CREATE)
		if (activeGroup && membership && isOwner) actions.add(ContentAction.Post.EDIT) // this has to be improved: only update once and < 5 mins of post creation
		if (activeGroup && membership && (isOwner || isRoleAdmin || isAuthorityAdmin)) actions.add(ContentAction.Post.DELETE)
		if (activeGroup && membership && !thread.disableComments) actions.add(ContentAction.Post.COMMENT)
		// TH-265
		//if (activeGroup && membership && isOwner && thread.disableComments) actions.add(ContentAction.Post.ENABLE_COMMENTS)
		//if (activeGroup && membership && isOwner && !thread.disableComments) actions.add(ContentAction.Post.DISABLE_COMMENTS)
		if (activeGroup && membership && !isOwner) actions.add(ContentAction.Post.SEND_MESSAGE)

		actions
	}

}
