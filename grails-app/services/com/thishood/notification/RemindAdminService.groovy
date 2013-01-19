package com.thishood.notification

import com.thishood.domain.UserGroup
import com.thishood.domain.UserRole
import com.thishood.domain.GroupStatus
import com.thishood.domain.User

class RemindAdminService {

	static transactional = true

	def grailsApplication
	def userGroupService
	def userService
	def queueMailerService

	void pendingGroup() {
		List<UserGroup> userGroups = userGroupService.findAllPending()

		if (userGroups.isEmpty()) return

		List<User> users = userService.findAllActiveByRole(UserRole.ROLE_ADMIN)

		queueMailerService.sendEmail(
				to: users.collect {it.emailWithName},
				subject: "Groups with status " + GroupStatus.PENDING,
				model: [
						config: grailsApplication.config,
						userGroups: userGroups
				],
				view: "/template/email/remind/group/pending")


	}
}
