package com.thishood.groups

import com.thishood.ContentAction

import grails.converters.JSON

import com.thishood.domain.*
import grails.plugins.springsecurity.Secured
import com.thishood.image.ImageSize;

@Secured(['isAuthenticated()'])
class UserGroupController {
	def springSecurityService
	def userGroupService
	def membershipService
	def groupNewsService
	def hoodService
	def groupService
	def marketService
	def persistableTemplateService

	static allowedMethods = [save: "POST", update: "POST", delete: "POST", setGroupImage: "POST"]

	def createGroup = {
		render view: 'group/create', model: new Group()
	}

	def createHood = {
		def hood = new Hood(
				city: 'Albany',
				state: 'CA',
				country: 'USA'
		)
		render view: 'hood/create', model: [hood: hood]
	}

	def saveGroup = {
		def response = [:]
		def user = springSecurityService.currentUser

		Group assembled = new Group(params)

		try {
			Group group = groupService.create(
					userId: user.id,
					name: assembled.name,
					description: assembled.description,
					about: assembled.about,
					news: assembled.news,
					//moderationType: assembled.moderationType,
					joinAccessLevel: assembled.joinAccessLevel,
					//privacyLevel: assembled.privacyLevel
			)
			if (!group.hasErrors()) {
				response = [success: "true"]
			} else {
				render group.errors as JSON
			}
		} catch (any) {
			log.error("Error occured on creation of group [${assembled}] by user [${user}]", any)
			response = [errors: "Unexpected error ${any?.message}"]
		}

		render response as JSON
	}

	def saveHood = {
		def response = [:]
		def user = springSecurityService.currentUser

		Hood assembled = new Hood(params)

		try {
			Hood hood = hoodService.create(
					userId: user.id,
					//name: assembled.name,
					//description: assembled.description,
					about: assembled.about,
					news: assembled.news,
					//moderationType: assembled.moderationType,
					//joinAccessLevel: assembled.joinAccessLevel, -- always is RESTRICTED
					//privacyLevel: assembled.privacyLevel
					//--hood specific fields
					street: assembled.street,
					number: assembled.number,
					city: assembled.city,
					state: assembled.state,
					country: assembled.country
			)
			if (!hood.hasErrors()) {
				response = [success: "true"]
			} else {
				render hood.errors as JSON
			}
		} catch (any) {
			log.error("Error occured on creation of hood [${assembled}] by user [${user}]", any)
			response = [errors: "Unexpected error ${any?.message}"]
		}

		render response as JSON
	}


	def editGroup = {
		def user = springSecurityService.currentUser
		def groupId = ConvertUtils.toLong(params.id)

		Membership membership = membershipService.findByUserAndGroup(user.id, groupId)

		if (!userGroupService.hasAction(groupId, user.id, ContentAction.Group.EDIT)) {
			flash.message = "${message(code: 'default.not.allowed.message')}"
			//todo make forward/redirect instead?
			render view: 'my', model: [groups: membershipService.findAllByUser(user.id)]
		}

		UserGroup userGroup = membership.userGroup
		GroupNews groupNews = groupNewsService.getLatestNews(userGroup.id)

		render view: 'group/edit', model: [group: userGroup, news: groupNews, dialog: params.dialog]
	}

	def editMarket = {
		def user = springSecurityService.currentUser
		def groupId = ConvertUtils.toLong(params.id)

		Membership membership = membershipService.findByUserAndGroup(user.id, groupId)

		if (!userGroupService.hasAction(groupId, user.id, ContentAction.Group.EDIT)) {
			flash.message = "${message(code: 'default.not.allowed.message')}"
			//todo make forward/redirect instead?
			render view: 'my', model: [groups: membershipService.findAllByUser(user.id)]
		}

		UserGroup userGroup = membership.userGroup
		GroupNews groupNews = groupNewsService.getLatestNews(userGroup.id)

		render view: 'market/edit', model: [group: userGroup, news: groupNews, dialog: params.dialog]
	}

	def editHood = {
		def user = springSecurityService.currentUser
		def groupId = ConvertUtils.toLong(params.id)

		Membership membership = membershipService.findByUserAndGroup(user.id, groupId)

		if (!userGroupService.hasAction(groupId, user.id, ContentAction.Group.EDIT)) {
			flash.message = "${message(code: 'default.not.allowed.message')}"
			//todo make forward/redirect instead?
			render view: 'my', model: [groups: membershipService.findAllByUser(user.id)]
		}

		UserGroup userGroup = membership.userGroup
		GroupNews groupNews = groupNewsService.getLatestNews(userGroup.id)

		render view: 'hood/edit', model: [hood: userGroup, news: groupNews, dialog: params.dialog]
	}

	def updateGroup = {
		def response = [:]

		def user = springSecurityService.currentUser

		def assembled = new Group(params)

		try {
			//todo vitaliy@02.04.11 - strange behavior -- assembled instance has null for id, version, need to investigate
			Group group = groupService.update(
					userId: user.id,
					//
					id: params.id as Long,
					version: params.version as Long,
					//name is ignoring -- it not for update by user
					description: assembled.description,
					about: assembled.about,
					news: assembled.news,
					//status ignoring
					//moderationType: assembled.moderationType,
					joinAccessLevel: assembled.joinAccessLevel,
					//privacyLevel: assembled.privacyLevel
			)
			if (!group.hasErrors()) {
				response = [success: "true"]
			} else {
				render group.errors as JSON
			}
		} catch (any) {
			log.error("Error occured on updating group [${assembled}] by user [${user}]", any)
			response = [errors: "Unexpected error ${any?.message}"]
		}

		render response as JSON
	}

	def updateMarket = {
		def response = [:]

		def user = springSecurityService.currentUser

		def assembled = new Market(params)

		try {
			//todo vitaliy@02.04.11 - strange behavior -- assembled instance has null for id, version, need to investigate
			def group = marketService.update(
					userId: user.id,
					//
					id: params.id as Long,
					version: params.version as Long,
					//name is ignoring -- it not for update by user
					description: assembled.description,
					about: assembled.about,
					news: assembled.news,
					//status ignoring
					//moderationType: assembled.moderationType,
					//joinAccessLevel: assembled.joinAccessLevel,
					//privacyLevel: assembled.privacyLevel
			)
			if (!group.hasErrors()) {
				response = [success: "true"]
			} else {
				render group.errors as JSON
			}
		} catch (any) {
			log.error("Error occured on updating group [${assembled}] by user [${user}]", any)
			response = [errors: "Unexpected error ${any?.message}"]
		}

		render response as JSON
	}

	def updateHood = {
		def response = [:]

		def user = springSecurityService.currentUser

		def assembled = new Group(params)

		try {
			//todo vitaliy@02.04.11 - strange behavior -- assembled instance has null for id, version, need to investigate
			Hood hood = hoodService.update(
					userId: user.id,
					//
					id: params.id as Long,
					version: params.version as Long,
					//name is ignoring -- it not for update by user
					description: assembled.description,
					about: assembled.about,
					news: assembled.news,
					//status ignoring
					//moderationType: assembled.moderationType,
					//joinAccessLevel: ignoring
					//privacyLevel: assembled.privacyLevel
			)
			if (!hood.hasErrors()) {
				response = [success: "true"]
			} else {
				render hood.errors as JSON
			}
		} catch (any) {
			log.error("Error occured on updating block [${assembled}] by user [${user}]", any)
			response = [errors: "Unexpected error ${any?.message}"]
		}

		render response as JSON
	}

	/**
	 * Don't distinguish type because on UI form is not need to be rendered
	 */
	def delete = {
		def user = springSecurityService.currentUser
		def groupId = ConvertUtils.toLong(params.group)
		try {
			userGroupService.deleteGroup(groupId, user.id)
			def success = [success: "${message(code: 'default.deleted.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"]
			render success as JSON
		} catch (any) {
			log.error("User [${user}] can't delete group [${groupId}]", any)
			def errors = [errors: any]
			if (any instanceof org.springframework.dao.DataIntegrityViolationException)
				errors = [errors: ["${message(code: 'default.not.deleted.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"]]
			render errors as JSON
		}
	}

	def setGroupImage = {
		def result = [:]
		def user = springSecurityService.currentUser

		def groupId = params.groupId as Long
		def uploadId = params.uploadId as Long

		try {
			userGroupService.setImage(user.id, groupId, uploadId)
			result = [url: userGroupService.getImageUrl(groupId, ImageSize.SMALL)]
		} catch (any) {
			log.error("Can't set image for group [${groupId}] by user [${user}]",any)
			result = [error: any.message]
		}

		render result as JSON
	}

	def joinGroup = {
		def resp = [success: 'true']
		def user = springSecurityService.currentUser
		def groupId = ConvertUtils.toLong(params.groupId)
		def description = params.description
		try {
			userGroupService.joinGroup(groupId, user.id, description)
			def group = userGroupService.getById(groupId)
			resp.group = group
		} catch (any) {
			log.error("User [${user}] can't join/request group [${groupId}]", any)
			resp = [success: 'false']
		}

		render resp as JSON
	}

	def joinMarket = {
		def market = UserGroup.findByName("Market");
		if (!market) {
			throw new RuntimeException("Market not found.")
		}
		userGroupService.joinGroup(market.id, springSecurityService.currentUser.id)
		render "OK"
	}

	def joinApprove = {
		def user = springSecurityService.currentUser
		def code = params.code
		try {
			userGroupService.joinApprove(user.id, code)
			flash.message = 'Group approved'
		} catch (any) {
			log.error("Unknown error on approving to join by user [${user}]", any)
			flash.error = "Error occured on group approval: ${any}"
		}
		render view: "/loggedin-info"
	}

	def joinReject = {
		def user = springSecurityService.currentUser
		def code = params.code
		try {
			userGroupService.joinReject(user.id, code)
			flash.message = 'Join to group is rejected'
		} catch (any) {
			log.error("Unknown error on rejecting to join by user [${user}]", any)
			flash.error = "Error occured on group reject: ${any}"
		}
		render view: "/loggedin-info"
	}

	def leaveGroup = {
		def errors = [success: 'true']
		membershipService.endMembership(springSecurityService.currentUser.id, ConvertUtils.toLong(params.id));
		render errors as JSON
	}

	def my = {
		def memberships = membershipService.findAllAccessibleByUser(springSecurityService.currentUser.id)

		render view: 'my', model: [memberships: memberships]
	}

	def join = {
		def groups = userGroupService.findUserGroupsNotSubscribed(springSecurityService.currentUser.id)
		
		render view: 'join', model: [groups: groups, entity: 'Group']
	}
	
	def joinBlock = {
		def groups = userGroupService.findHoodsNotSubscribed(springSecurityService.currentUser.id)

		render view: 'joinBlock', model: [groups: groups, entity: 'Block']
	}

	def inviteDialog = {
		def userGroupId = params.userGroup as Long

		UserGroup userGroup = userGroupService.getById(userGroupId)
		PersistableTemplate persistableTemplate = persistableTemplateService.findByUserGroup(userGroupId)

		render view: 'inviteDialog', model: [userGroup: userGroup, persistableTemplate: persistableTemplate]
	}

	def sendInvitations = {
		def result = [:]

		User user = springSecurityService.currentUser

		def userGroupId = params.userGroup as Long
		def emails = params.emails_list
		def content = params.content

		try {
			hoodService.inviteToHood(user.id, userGroupId, emails, content)
			result = [success: true]
		} catch (any) {
			log.error("Unable to send invitations by user [${user}] to group [${userGroupId}]", any)
			result = [errors: any.message]
		}

		render result as JSON
	}

	def nonAdmins = {
		def user = springSecurityService.currentUser
		def groupId = ConvertUtils.toLong(params.id)

		Membership membership = membershipService.findByUserAndGroup(user.id, groupId)

		if (!userGroupService.hasAction(groupId, user.id, ContentAction.Group.SET_ADMIN)) {
			flash.message = "${message(code: 'default.not.allowed.message')}"
			render model: []
		}
		
		def memberships = membershipService.findAllByGroup(groupId)
		def availableMemberships = []
		
		memberships.each {
			if (it.user.id != user.id && !it.hasAuthority(MembershipAuthority.ADMIN)) {
				availableMemberships.add it
			}
		}
		
		render view: 'nonAdmins', model: [memberships: availableMemberships, groupId: groupId]
	}
	
	def setAdmin = {
		def result = []
		def user = springSecurityService.currentUser
		def groupId = ConvertUtils.toLong(params.groupId)
		def newAdminMembershipId = ConvertUtils.toLong(params.newAdminMembershipId)
		def removeAdmin = params.removeAdmin == 'yes' 
		
		if (!userGroupService.hasAction(groupId, user.id, ContentAction.Group.SET_ADMIN)) {
			result.error = "${message(code: 'default.not.allowed.message')}"
			render result as JSON
		}
		
		Membership membership = membershipService.findByUserAndGroup(user.id, groupId)
		
		membershipService.changeAdministration(membership.id, newAdminMembershipId, removeAdmin)
		
		render result as JSON
	}
}
