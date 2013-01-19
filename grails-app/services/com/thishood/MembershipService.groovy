package com.thishood


import com.thishood.domain.Membership
import com.thishood.domain.MembershipAuthority
import com.thishood.domain.User
import com.thishood.domain.UserGroup
import com.thishood.domain.GroupStatus

class MembershipService extends AbstractHolderableService {

	static transactional = true

	def springSecurityService
	def groupNotificationTimelineService

	List<Membership> findAllByUser(Long userId) {
		Membership.findAll("from Membership m where m.user.id = :userId order by m.userGroup.name", [userId: userId])
	}

	List<Membership> findAllActiveByUser(Long userId) {
		Membership.findAll("from Membership m where m.user.id = :userId and m.userGroup.status = :status order by m.userGroup.name", [userId: userId, status: GroupStatus.ACTIVE])
	}

	List<Membership> findAllAccessibleByUser(Long userId) {
		Membership.findAll("from Membership m where m.user.id = :userId and m.userGroup.status in (:statuses) order by m.userGroup.name", [userId: userId, statuses: [GroupStatus.ACTIVE, GroupStatus.ARCHIVED]])
	}

	List<Membership> findAllByGroup(Long groupId) {
		Membership.findAll("from Membership m where m.userGroup.id = :groupId", [groupId: groupId])
	}

	Membership findByUserAndGroup(Long userId, Long groupId) {
		def c = Membership.createCriteria()
		def membership = c.get {
			and {
				eq("user.id", userId)
				eq("userGroup.id", groupId)
			}
		}
		membership
	}

	int countActiveMembers(Long userGroupId) {
		UserGroup userGroup = UserGroup.getOrFail(userGroupId)

		Membership.countByUserGroup(userGroup)
	}


	boolean haveCommonMembership(Long userId1, Long userId2) {
		User user1 = User.getOrFail(userId1)
		User user2 = User.getOrFail(userId2)

		if (user1.id == user2.id) {
			return true
		}

		//todo move such check to SQL
		def memberships1 = Membership.findAllByUser(user1)
		def memberships2 = Membership.findAllByUser(user2)

		for (m1 in memberships1) {
			def groupId = m1.userGroup.id
			for (m2 in memberships2) {
				if (groupId == m2.userGroup.id) {
					return true
				}
			}
		}
		return false
	}

	/**
	 * Find N memberships from the same group
	 */
	List<Membership> findRandomMembershipsByGroup(Long groupId) {
		def userGroup = UserGroup.getOrFail(groupId);

		Membership.findAll("from Membership m where m.userGroup = :userGroup order by rand()", [userGroup: userGroup], [max: 10])
	}

	void endMembership(Long userId, Long userGroupId) {
		User user = User.getOrFail(userId)
		UserGroup userGroup = UserGroup.getOrFail(userGroupId)

		Membership membership = findByUserAndGroup(userId, userGroupId)

		if (membership) {
			log.info("removing user [${membership.user}] from group [${membership.userGroup}]");
			delete(membership.id, user.id)
		} else {
			throw new IllegalArgumentException("Can't remove user with id [${userId}] from group with id [${userGroupId}] - membership doesn't exist")
		}
	}

	void startMembership(Long userId, Long groupId, List<MembershipAuthority> authorities = null) {
		User user = User.getOrFail(userId)
		UserGroup userGroup = UserGroup.getOrFail(groupId)

		Membership.findByUserAndUserGroup(user, userGroup) ?:
			new Membership(
					user: user,
					userGroup: userGroup,
					authorities: authorities
			).save(failOnError: true)
	}

	def update(params) {
		Membership membership = Membership.getOrFail(params.id)

		membership.version = params.version

		//embedded object
		membership.notification = params.notification
		membership.frequency = membership.notification ? params.frequency : null

		membership.save(flush: true, failOnError: true)
	}

	void delete(Long membershipId, Long userId) {
		User user = User.getOrFail(userId)
		Membership membership = Membership.getOrFail(membershipId)

		//todo add check if 'user' has permission to delete this membership

		membership.authorities = []
		groupNotificationTimelineService.deleteByMembership(membership.id, user.id)

		log.info("User [${user}] deletes membership [${membership}]")

		membership.delete()
	}

	void deleteAllByGroup(Long groupId, Long userId) {
		UserGroup group = UserGroup.getOrFail(groupId)
		User user = User.getOrFail(userId)

		//todo add recheck if user is allowed to delete

		findAllByGroup(groupId).each {Membership membership ->
			delete(membership.id, user.id)
		}

		// this bulk delete fails in grails 1.3.6
		//Membership.executeUpdate("update Membership m set m.authorities=:authorities where m.userGroup = :group", [authorities:[], group: group])
		//Membership.executeUpdate("delete from Membership m where m.userGroup = :group", [group: group])
	}

	List<Membership> findAllByGroupAndAuthority(Long groupId, MembershipAuthority authority) {
		UserGroup group = UserGroup.getOrFail(groupId)
		Membership.findAll("from Membership m where m.userGroup = :group and :authority in elements(m.authorities)", [group: group, authority: authority.name()])
	}
	
	void changeAdministration(Long currentAdminMembershipId, Long newAdminMembershipId, Boolean replace) {
		Membership newMembership = Membership.getOrFail(newAdminMembershipId)
		if (!newMembership.hasAuthority(MembershipAuthority.ADMIN)) {
			newMembership.authorities.add MembershipAuthority.ADMIN
		} 
		
		if (replace) {
			Membership currentMembership = Membership.getOrFail(currentAdminMembershipId)
			currentMembership.authorities = currentMembership.authorities.findAll {it != MembershipAuthority.ADMIN}
		}
	}
}
