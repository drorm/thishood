package com.thishood

import com.thishood.domain.*

/**
 * @see HoodService
 * @see UserGroupService
 */
class GroupService extends AbstractHolderableService {

	static transactional = true

	def grailsApplication
	def queueMailerService

	/**
	 * @see HoodService#create(Object)
	 */
	Group create(params) {
		User user = User.getOrFail(params.userId)

		Group group = new Group(
				name: params.name,
				description: params.description,
				about: params.about,
				status: GroupStatus.PENDING,
				moderationType: GroupModerationType.NONE,
				joinAccessLevel: params.joinAccessLevel,
				privacyLevel: GroupPrivacyLevel.OPEN
		)
		if (group.validate()) {
			group = group.save(failOnError: true, flush: true)

			userGroupService.onCreate(group, user, params)
		}

		group
	}

	/**
	 * @see HoodService#delete(long, long)
	 */
	void delete(Long groupId, Long userId) {
		Group group = Group.getOrFail(groupId)
		User user = User.getOrFail(userId)

		userGroupService.onDelete(group, user)
	}

	/**
	 * @see HoodService#update(Object)
	 * @param params
	 * @return
	 */
	Group update(params) {
		Group group = Group.getOrFail(params.id)
		User user = User.getOrFail(params.userId)

		if (!userGroupService.hasAction(group.id, user.id, ContentAction.Group.EDIT)) throw new DataAccessDeniedException("User [${user}] is not allowed to update [${group}]")

		group.version = params.version
		group.description = params.description
		group.about = params.about
		group.moderationType = GroupModerationType.NONE
		group.joinAccessLevel = params.joinAccessLevel
		group.privacyLevel = GroupPrivacyLevel.OPEN

		if (group.validate()) {
			group = group.save(flush:true, failOnError: true)

			userGroupService.onUpdate(group, user, params)
		}

		group
	}
}
