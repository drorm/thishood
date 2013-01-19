package com.thishood

import com.thishood.domain.*

import com.thishood.image.ImageSize

/**
 * Service which has common operations for all classes which extends from {@link com.thishood.domain.UserGroup}
 * @see HoodService
 * @see GroupService
 */
class UserGroupService extends AbstractHolderableService {
	static transactional = true

	def grailsApplication
	def queueMailerService
	def uploadService

	UserGroup findByUserAndGroup(Long userId, Long userGroupId) {
		def userGroups = findAllByUser(userId)

		userGroups.find { userGroup ->
			userGroup.id == userGroupId
		}
	}

	List<UserGroup> findAllByUser(Long userId) {
		UserGroup.executeQuery("select m.userGroup from Membership m where m.user.id = :userId", [userId: userId])
	}
	
	void createDefaultUserGroups() {
		def groupNames = [grailsApplication.config.thishood.default.usergroup, "Test"]

		groupNames.each {
			def defaultUserGroup = UserGroup.findByName(it) ?:
				new Group(
						name: it,
						description: "The default group",
						about: "This is the default automatically created group. All new users are members of this group.",
						parent: null,
						dateCreated: new Date(),
						lastUpdated: new Date(),
						imageUploadId: null,
						moderationType: GroupModerationType.NONE,
						joinAccessLevel: GroupJoinAccessLevel.OPEN,
						privacyLevel: GroupPrivacyLevel.OPEN,
						status: GroupStatus.ACTIVE
				).save(failOnError: true)
		}
	}

	UserGroup getById(Long groupId) {
		def userGroup = UserGroup.get(groupId);

		if (!userGroup) throw new RuntimeException("Can't find userGroup with id ${groupId}")

		userGroup
	}

	UserGroup create(params) {
		UserGroup userGroup = new UserGroup()
		userGroup.properties = params
		return userGroup
	}

	/**
	 * Creates a group (user group) and sets creator permission as 'admin'
	 */
	UserGroup createOwn(params) {
		User user = User.getOrFail(params.userId)

		String news = params.remove('news')

		UserGroup userGroup = new UserGroup(params)
		userGroup.status = GroupStatus.PENDING

		if (userGroup.validate()) {
			userGroup = userGroup.save(failOnError: true, flush: true)

			membershipService.startMembership(user.id, userGroup.id, [MembershipAuthority.ADMIN]);


			if (news) {
				GroupNews groupNews = new GroupNews()
				groupNews.content = news
				groupNews.dateCreated = new Date()
				groupNews.userGroup = userGroup
				groupNews.save()
			}

			List<User> users = userService.findAllActiveByRole(UserRole.ROLE_ADMIN)

			queueMailerService.sendEmail(
					to: users.collectAll({it.emailWithName }),
					subject: "New group created (approval required), ThisHood",
					model: [
							config: grailsApplication.config,
							creator: user,
							group: userGroup,

					],
					view: "/template/email/group/newGroupCreated")
		}

		userGroup
	}

	/**
	 * Commons operations occured on create of UserGroup
	 */
	def onCreate(UserGroup userGroup, User user, params) {
		membershipService.startMembership(user.id, userGroup.id, [MembershipAuthority.ADMIN]);

		if (params.news) {
			def news = params.news
			GroupNews groupNews = new GroupNews()
			groupNews.content = news
			groupNews.dateCreated = new Date()
			groupNews.userGroup = userGroup
			groupNews.save()
		}

		// admins have to approve
		List<User> users = userService.findAllActiveByRole(UserRole.ROLE_ADMIN)

		queueMailerService.sendEmail(
				to: users.collectAll({it.emailWithName }),
				subject: "New group created (approval required), ThisHood",
				model: [
						config: grailsApplication.config,
						creator: user,
						group: userGroup,

				],
				view: "/template/email/group/newGroupCreated")

	}


	UserGroup save(params) {
		UserGroup userGroup = new UserGroup(params)

		//todo add additional business logic like informing administrator, etc

		userGroup.saveSafely(flush: true)
	}

	def findUserGroupsByUser(def userId) {
		def user = User.get(userId)
		if (!user) {
			throw new RuntimeException("Can't find user by id ${userId}")
		}

	}

	void setImage(Long userId, Long groupId, Long imageUploadId) {
		User user = User.getOrFail(userId)
		UserGroup group = UserGroup.getOrFail(groupId)

		Membership membership = membershipService.findByUserAndGroup(userId, groupId)

		if (!membership) throw new IllegalArgumentException("User [${user}] doesn't belong to group [${group}]")
		if (!hasAction(group.id, user.id, ContentAction.Group.EDIT)) throw new DataAccessDeniedException("User [${user}] is not allowed to change image for [${group}]")

		Upload upload = Upload.findByIdAndUser(imageUploadId, user);

		if (!upload) throw new IllegalArgumentException("Can't find upload for user [${user}]")

		group.imageUploadId = imageUploadId
		group.save(failOnError: true)

	}

	String getImageUrl(Long groupId, ImageSize size) {
		UserGroup group = UserGroup.getOrFail(groupId)
		Upload upload = Upload.get(group.imageUploadId)
		if (upload) {
			return uploadService.getImageUrl(upload.id, size)
		} else {
			//group has no image, use the generic one
			return grailsApplication.config.grails.serverURL + "/identicon/view/" + (group.id + group.name).encodeAsMD5()
		}
	}

	UserGroup findByName(String name) {
		UserGroup.findByName(name)
	}

	List<UserGroup> findGroupsNotSubscribed(Long userId) {
		User user = User.getOrFail(userId)
		
		UserGroup.findAll("from UserGroup where id not in (select userGroup from Membership m where m.user = :user) and id not in (select group from MembershipVerification mv where mv.user = :user and mv.status=:membershipVerificationStatus) and status = :status", [user: user, status: GroupStatus.ACTIVE, membershipVerificationStatus: MembershipVerificationStatus.PENDING])
	}
	
	List<UserGroup> findUserGroupsNotSubscribed(Long userId) {
		User user = User.getOrFail(userId)
		
		UserGroup.findAll("from UserGroup where id not in (select userGroup from Membership m where m.user = :user) and id not in (select group from MembershipVerification mv where mv.user = :user and mv.status=:membershipVerificationStatus) and status = :status and discriminator = 'GROUP'", [user: user, status: GroupStatus.ACTIVE, membershipVerificationStatus: MembershipVerificationStatus.PENDING])
	}
	
	List<UserGroup> findHoodsNotSubscribed(Long userId) {
		User user = User.getOrFail(userId)

		UserGroup.findAll("from UserGroup where id not in (select userGroup from Membership m where m.user = :user) and id not in (select group from MembershipVerification mv where mv.user = :user and mv.status=:membershipVerificationStatus) and status = :status and discriminator = 'HOOD'", [user: user, status: GroupStatus.ACTIVE, membershipVerificationStatus: MembershipVerificationStatus.PENDING])
	}
	
	List<UserGroup> findAllActive() {
		UserGroup.findAllByStatus(GroupStatus.ACTIVE.name())
	}

	List<UserGroup> findAllPending() {
		UserGroup.findAll("from UserGroup ug where ug.status = :status order by ug.name", [status :GroupStatus.PENDING])
	}

	void joinGroup(Long groupId, Long userId, String description = null) {
		UserGroup group = UserGroup.getOrFail(groupId)
		User user = User.getOrFail(userId)

		if (group.status != GroupStatus.ACTIVE) throw new DataAccessDeniedException("Can't join to group [${group}] because it's not ${GroupStatus.ACTIVE}")
		Membership membership = membershipService.findByUserAndGroup(userId, groupId)
		if (membership) throw new IllegalArgumentException("User [${user}] is already member of group [${group}]")

		switch (group.joinAccessLevel) {
			case GroupJoinAccessLevel.RESTRICTED:
				MembershipVerification membershipVerification = membershipVerificationService.requestJoin(userId, groupId, description)
				def users = membershipService.findAllByGroupAndAuthority(groupId, MembershipAuthority.ADMIN)
				queueMailerService.sendEmail(
						to: users.collectAll({it.user.emailWithName }),
						subject: "Join request to " + group.name + ", ThisHood",
						model: [
								config: grailsApplication.config,
								requestor: user,
								group: group,
								membershipVerification: membershipVerification

						],
						view: "/template/email/group/joinRequest")
				break
			case GroupJoinAccessLevel.OPEN:
				membershipService.startMembership(userId, groupId)
				break
			default:
				throw new IllegalArgumentException("Unknown behavior for [${group.joinAccessLevel}]")
		}

	}

	/**
	 * Dispatcher which routes to service by type of instance loaded
	 */
	void deleteGroup(Long groupId, Long userId) {
		UserGroup userGroup = UserGroup.getOrFail(groupId)
		User user = User.getOrFail(userId)

		switch (userGroup) {
			case Hood:
				hoodService.delete(groupId, userId)
				break
			case Group:
				groupService.delete(groupId, userId)
				break
			default:
				throw new IllegalArgumentException("Can't process [${userGroup}]")
		}
	}

	/**
	 * common operations for deletion
	 */
	void onDelete(UserGroup userGroup, User user) {
		if (!hasAction(userGroup.id, user.id, ContentAction.Group.DELETE)) throw new DataAccessDeniedException("User [${user}] is now allowed to delete group [${userGroup}]")

		membershipService.deleteAllByGroup(userGroup.id, user.id)
		threadService.deleteAllThreadsByGroup(userGroup.id, user.id)
		groupNewsService.deleteByGroup(userGroup.id)

		userGroup.delete(flush: true)

	}

	void onUpdate(UserGroup userGroup, User user, params) {
		if (params.news) {
			def oldNews = groupNewsService.getLatestNews(userGroup.id)
			if (params.news != oldNews?.content) {
				groupNewsService.addGroupNews(userGroup.id, params.news)
			}
		}

	}
	void createApprove(Long userId, Long groupId) {
		createConfirmStatus(userId, groupId, GroupStatus.ACTIVE)

		notifyOfGroupCreationDecision(groupId, "New group is approved, ThisHood", "/template/email/group/newGroupApproved")
	}

	void createReject(Long userId, Long groupId) {
		createConfirmStatus(userId, groupId, GroupStatus.REJECTED)

		notifyOfGroupCreationDecision(groupId, "New group is rejected, ThisHood", "/template/email/group/newGroupRejected")
		//potential bug -- email might be sent even if error raise
		deleteGroup(groupId, userId)
	}

	private void createConfirmStatus(Long userId, Long groupId, GroupStatus status) {
		User user = User.getOrFail(userId)
		UserGroup group = UserGroup.getOrFail(groupId)

		if (!user.hasRole(UserRole.ROLE_ADMIN)) throw new IllegalArgumentException("User [${user}] is not allowed to change status of group [${group}]")
		if (group.status != GroupStatus.PENDING) throw new IllegalArgumentException("Group [${group}] is not in [${GroupStatus.PENDING}] status")

		group.status = status

		group.save(failOnError: true, flush: true)
	}

	private def notifyOfGroupCreationDecision(Long groupId, String subject, String view) {
		UserGroup group = UserGroup.getOrFail(groupId)

		//sending email to all members, but there have to be 1 member only - a creator
		def memberships = membershipService.findAllByGroup(groupId)
		if (memberships.size() != 1) {
			throw new IllegalArgumentException("New group [${group}] has [${memberships.size()}] but expected 1")
		}
		User creator = memberships[0].user

		queueMailerService.sendEmail(
				to: creator.emailWithName,
				subject: subject,
				model: [
						config: grailsApplication.config,
						creator: creator,
						group: group,

				],
				view: view)
	}

	void joinApprove(Long userId, String code) {
		joinConfirmStatus(userId, code, MembershipStatus.ACTIVE)
	}

	void joinReject(Long userId, String code) {
		joinConfirmStatus(userId, code, MembershipStatus.REJECTED)
	}

	private void joinConfirmStatus(Long userId, String code, MembershipStatus status) {
		User adminUser = User.getOrFail(userId)

		MembershipVerification membershipVerification = membershipVerificationService.findByCode(code)
		if (!membershipVerification) throw new IllegalArgumentException("Can't find by code [${code}]")

		UserGroup group = membershipVerification.group
		User requestorUser = membershipVerification.user

		if(membershipVerification.status == MembershipVerificationStatus.REJECTED) throw new IllegalArgumentException("User [${requestorUser}] was already rejected")
		if(membershipVerification.status == MembershipVerificationStatus.APPROVED) throw new IllegalArgumentException("User [${requestorUser}] was already approved")

		Membership adminMembership = membershipService.findByUserAndGroup(adminUser.id, group.id)
		if (!adminMembership || !adminMembership.hasAuthority(MembershipAuthority.ADMIN)) throw new DataAccessDeniedException("User [${adminUser}] is not [${MembershipAuthority.ADMIN}] for group [${group}]")

		switch (status) {
			case MembershipStatus.REJECTED:
				membershipVerificationService.changeStatus(membershipVerification.id, MembershipVerificationStatus.REJECTED)
				notifyOfJoinRequestDecision(requestorUser, group, adminUser, "Join to group " + group.name + " rejected, ThisHood", "/template/email/group/joinRejected")
				break
			case MembershipStatus.ACTIVE:
				membershipService.startMembership(membershipVerification.user.id, membershipVerification.group.id)
				membershipVerificationService.changeStatus(membershipVerification.id, MembershipVerificationStatus.APPROVED)
				notifyOfJoinRequestDecision(requestorUser, group, adminUser, "Join to group " + group.name + " approved, ThisHood", "/template/email/group/joinApproved")
				break
			default:
				throw new IllegalArgumentException("Status [${status}] is not processable")
		}
	}


	private def notifyOfJoinRequestDecision(User requestorUser, UserGroup group, User adminUser, String subject, String view) {
		queueMailerService.sendEmail(
				to: requestorUser.emailWithName,
				subject: subject,
				model: [
						config: grailsApplication.config,
						requestor: requestorUser,
						admin: adminUser,
						group: group,

				],
				view: view)
	}


	Boolean hasAction(Long groupId, Long userId, ContentAction.Group action) {
		def actions = getActions(groupId, userId)

		actions.contains(action)
	}

	def getActions(Long groupId, Long userId) {
		UserGroup group = UserGroup.getOrFail(groupId)
		User user = User.getOrFail(userId)

		def actions = []
		//
		def activeGroup = group.status == GroupStatus.ACTIVE
		def membership = membershipService.findByUserAndGroup(user.id, group.id)
		def isRoleAdmin = user.hasRole(UserRole.ROLE_ADMIN)
		def isAuthorityAdmin = membership?.hasAuthority(MembershipAuthority.ADMIN)

		if (activeGroup && membership) actions.add(ContentAction.Group.POST)
		if ((membership && isAuthorityAdmin) || isRoleAdmin) actions.add(ContentAction.Group.EDIT)
		if ((membership && isAuthorityAdmin) || isRoleAdmin) actions.add(ContentAction.Group.DELETE)
		if ((membership && isAuthorityAdmin) || isRoleAdmin) actions.add(ContentAction.Group.SET_ADMIN)

		actions
	}
	
}
