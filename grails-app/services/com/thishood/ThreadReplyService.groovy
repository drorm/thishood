package com.thishood

import com.thishood.domain.Thread
import com.thishood.domain.ThreadReply
import com.thishood.domain.User
import com.thishood.domain.UserGroup
import com.thishood.domain.Membership
import com.thishood.domain.GroupStatus
import com.thishood.domain.UserRole
import com.thishood.domain.MembershipAuthority;

class ThreadReplyService extends AbstractHolderableService{

    static transactional = true

    def springSecurityService

    def addReply(params) {
        //prepare params
        def now = new Date()
        params.dateCreated = params.dateUpdated = now
        if (params.fromUserId) params.fromUser = User.getOrFail(params.fromUserId)
        if (params.threadId) params.thread = Thread.getOrFail(params.threadId)

        //validation
        def threadReply = new ThreadReply(params)

        //we have to lock because concurrently added comments may cause inconsistency
		def thread = Thread.lock(threadReply.thread.id)
        thread.dateUpdated = now
        thread.repliesCount++

        if (!membershipService.findByUserAndGroup(params.fromUserId, thread.userGroup.id)) throw new IllegalArgumentException("User [${params.fromUserId}] doesn't has membership in group [${params.userGroupId}]")

        thread.save(failOnError: true)
        threadReply.save(failOnError: true)

    }

	void deleteReply(Long threadReplyId, Long userId) {
		ThreadReply threadReply = ThreadReply.getOrFail(threadReplyId)
		User user = User.getOrFail(userId)

		if (!hasAction(threadReplyId, userId, ContentAction.Comment.DELETE)) throw new IllegalArgumentException("User [${user}] is not authorized to delete comment [${threadReply}]")

		Thread thread = Thread.lock(threadReply.thread.id)
		thread.repliesCount--
		thread.save(failOnError: true)
		threadReply.delete(failOnError: true)
	}

    List<ThreadReply> findAllByThread(Long threadId) {
        Thread thread = Thread.getOrFail(threadId)

        ThreadReply.findAll("from ThreadReply tr where tr.thread = :thread order by tr.dateCreated desc", [thread: thread])
    }

	int countCommentsPerDay(Long communityId, Date date) {
		UserGroup community = UserGroup.getOrFail(communityId)

		def (Date lowerDate, Date upperDate) = DateUtil.getLowerAndUpperByDate(date)

		//ThreadReply.createCriteria("select count(tr) from ThreadReply tr where tr.thread.userGroup=:community and tr.dateCreated between :lowerDate and :upperDate",[community:community, lowerDate:lowerDate, upperDate:upperDate])

		def cr = ThreadReply.createCriteria()
		def count = cr.get {
			projections {
				rowCount()
			}
			and {
				thread {
					eq("userGroup", community)
				}
				between("dateCreated", lowerDate, upperDate)
			}
		}
		count


	}

	int countCommentsPerDay(Date date) {
		def (Date lowerDate, Date upperDate) = DateUtil.getLowerAndUpperByDate(date)

		ThreadReply.countByDateCreatedBetween(lowerDate, upperDate)
	}

    List<ThreadReply> findAllCreatedNotByMemberInPeriod(Long membershipId, Date startDate, Date finishDate) {
        Membership membership = Membership.getOrFail(membershipId)

        ThreadReply.findAll("from ThreadReply tr where tr.fromUser != :user and tr.dateCreated between :startDate and :finishDate and tr.thread.userGroup=:group", [
                user: membership.user,
                group: membership.userGroup,
                startDate: startDate,
                finishDate: finishDate
        ])
    }

	void deleteAllByThread(Long threadId) {
		Thread thread = Thread.getOrFail(threadId)

		ThreadReply.executeUpdate("delete from ThreadReply tr where tr.thread = :thread", [thread: thread])
	}

	void deleteAllByGroup(Long groupId) {
		UserGroup group = UserGroup.getOrFail(groupId)

		ThreadReply.findAll("from ThreadReply tr where tr.thread.userGroup = :group", [group: group]).each {ThreadReply comment ->
			comment.delete(flush:true)
		}
		//doesn't work in grails 1.3.6
		//ThreadReply.executeUpdate("delete from ThreadReply tr where tr.thread.userGroup = :group", [group: group])
	}

	Boolean hasAction(Long threadId, Long userId, ContentAction.Comment action) {
		def actions = getActions(threadId, userId)

		actions.contains(action)
	}

	def getActions(Long threadReplyId, Long userId) {
		ThreadReply threadReply = ThreadReply.getOrFail(threadReplyId)
		User user = User.getOrFail(userId)

		def actions = []
		//
		def activeGroup = threadReply.thread.userGroup.status == GroupStatus.ACTIVE
		def membership = membershipService.findByUserAndGroup(userId, threadReply.thread.userGroup.id)
		def isOwner = threadReply.fromUser.id == user.id
		def isRoleAdmin = user.hasRole(UserRole.ROLE_ADMIN)
		def isAuthorityAdmin = membership?.hasAuthority(MembershipAuthority.ADMIN)
		//
		def threadActions = threadService.getActions(threadReply.thread.id, userId)

		//delegating to thread -- later other logic can be added
		if (threadActions.contains(ContentAction.Post.COMMENT)) actions.add(ContentAction.Comment.CREATE)
		if (activeGroup && membership && isOwner) actions.add(ContentAction.Comment.EDIT) // this has to be improved: only update once and < 5 mins of post creation
		if (activeGroup && membership && (isOwner || isRoleAdmin || isAuthorityAdmin)) actions.add(ContentAction.Comment.DELETE)
		if (activeGroup && membership && !isOwner) actions.add(ContentAction.Comment.SEND_MESSAGE)

		actions
	}

}
