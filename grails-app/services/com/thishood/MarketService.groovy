package com.thishood

import com.thishood.domain.*

class MarketService extends AbstractHolderableService {

	static transactional = true

	def grailsApplication
	def queueMailerService

	def create() {
		def name = grailsApplication.config.thishood.market.usergroup
		if (!UserGroup.findByName(name)) {
			def market = new Market(
				name: name,
				description: "Free marketplace",
				about: "Place to buy or sell something",
				status: GroupStatus.ACTIVE,
				moderationType: GroupModerationType.NONE,
				joinAccessLevel: GroupJoinAccessLevel.OPEN,
				privacyLevel: GroupPrivacyLevel.OPEN
			)
			market.save(failOnError: true)
			def users = User.getAll()
			users.each {
				new Membership(
					user: it,
					userGroup: market
				).save(failOnError: true)
			}
		}
	}

	def update(params) {
		def group = Market.getOrFail(params.id)
		def user = User.getOrFail(params.userId)

		if (!userGroupService.hasAction(group.id, user.id, ContentAction.Group.EDIT)) throw new DataAccessDeniedException("User [${user}] is not allowed to update [${group}]")

		group.version = params.version
		group.description = params.description
		group.about = params.about
		group.moderationType = GroupModerationType.NONE
		group.joinAccessLevel = GroupJoinAccessLevel.OPEN
		group.privacyLevel = GroupPrivacyLevel.OPEN

		if (group.validate()) {
			group = group.save(flush:true, failOnError: true)

			userGroupService.onUpdate(group, user, params)
		}

		group
	}

}