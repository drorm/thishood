package com.thishood

import com.thishood.domain.*
import grails.plugins.springsecurity.Secured

import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

import com.thishood.image.ImageSize

//permitting all because real check is made inside index method
class HomeController {

	def springSecurityService
	def userService
	def userGroupService
	def membershipService
	def groupNewsService
	def groupResourceService
	def hoodService
	def chatMessageService
	def threadService
	
	static allowedMethods = [saveResource: "POST", saveResourceForAllBlocks: "POST"]

	@Secured(['permitAll'])
	def index = {
		//render(view: "index_anonymous")

		redirect(action:"news")
	}

	@Secured(['isAuthenticated()'])
	def news = {
		def user = springSecurityService.currentUser
		def group = null
		def groupNews = null
		def thread = null
		def error = null
		def groupActions = null
		//shortcut
		def membership = null;
		if (params.thread) {
			def threadId = ConvertUtils.toLong(params.thread)
			thread = Thread.getOrFail(threadId)
			if (!thread.userGroup) throw new DataAccessDeniedException("You're trying to open the group that you don't belong to. Please choose another group from the left panel.")
			if (!(thread.userGroup.status in [GroupStatus.ACTIVE, GroupStatus.ARCHIVED])) throw new RuntimeException("Group [${thread.userGroup}] is not allowed to be open")
		} else if (params.group) {
			def userGroupId = params.group as Long
			group = userGroupService.findByUserAndGroup(user.id, userGroupId)

			if (!group) throw new DataAccessDeniedException("You're trying to open the group that you don't belong to. Please choose another group from the left panel.")
			if (!(group.status in [GroupStatus.ACTIVE, GroupStatus.ARCHIVED])) throw new RuntimeException("Group [${group}] is not allowed to be open")

			groupNews = groupNewsService.getLatestNews(group.id)
			groupActions = userGroupService.getActions(group.id, user.id)
			membership = membershipService.findByUserAndGroup(user.id, group.id)
		}

		def hoods = hoodService.findAllByUser(user.id)

		render(view: "index_loggedin", model: [
			user: user,
			isRoleAdmin: user.hasRole(UserRole.ROLE_ADMIN),
			isGroupAdmin: membership?.hasAuthority(MembershipAuthority.ADMIN),
			group: group ?: [],
			thread: thread,
			news: groupNews,
			groupActions: groupActions,
			membership: membership,
			withinHood: hoods.size() > 0,
			joinedMarket: userService.joinedMarket(user),
			inboxMessagesNumber: chatMessageService.getNumberOfInboxMessages(user.id)
		])
	}

	private getModel(allow) {
		if (!params.group) {
			false
		}
		def groupId = ConvertUtils.toLong(params.group)
		def user = springSecurityService.currentUser
		//todo when user can be null??? all these actions are secured
		def isRoleAdmin = user?.hasRole(UserRole.ROLE_ADMIN)
		def membership = null
		def group = null
		def isGroupAdmin = false
		if (isRoleAdmin) {
			group = UserGroup.getOrFail(groupId)
			membership = membershipService.findByUserAndGroup(user.id, groupId)
			isGroupAdmin = membership?.hasAuthority(MembershipAuthority.ADMIN)
		} else if (user) {
			membership = membershipService.findByUserAndGroup(user.id, groupId)
			if (!membership || !membership.hasAuthority(MembershipAuthority.ADMIN)) {
				false
			}
			group = membership.userGroup
			if (!(group.status in [GroupStatus.ACTIVE, GroupStatus.ARCHIVED])) {
				false
			}
			isGroupAdmin = membership.hasAuthority(MembershipAuthority.ADMIN)
			if (!allow?.notGroupAdmin && !isGroupAdmin) {
				false
			}
		}

		[
			user:user,
			isRoleAdmin:isRoleAdmin,
			isGroupAdmin:isGroupAdmin,
			group:group,
			news:groupNewsService.getLatestNews(group.id),
			groupActions:userGroupService.getActions(group.id, user.id),
			membership:membership
		]
	}

	private getAdminModel = {
		[
			user:springSecurityService.currentUser,
			isRoleAdmin:true
		]
	}

	private getResource(model) {
		def resource
		if (params.resourceId) {
			resource = GroupResource.findByIdAndUserGroup(ConvertUtils.toLong(params.resourceId), model.group)
		}
		if (!resource) {
			resource = new GroupResource()
			resource.userGroup = model.group
			resource.title = ""
			resource.description = ""
			resource.createdTime = new Date()
		}
		resource
	}

	private getAdminResource() {
		def resource
		if (params.resourceId) {
			resource = GroupResource.findByIdAndForAllBlocks(ConvertUtils.toLong(params.resourceId), true)
		}
		if (!resource) {
			resource = new GroupResource()
			resource.title = ""
			resource.description = ""
			resource.createdTime = new Date()
			resource.forAllBlocks = true
		}
		resource
	}

	private updateResource(model, resource) {
		resource.title = params.title
		resource.description = Jsoup.clean(params.description,
			Whitelist.relaxed()
			.addTags("iframe")
			.addAttributes(":all", "style")
			.addAttributes("iframe", "src", "width", "height", "frameborder", "allowfullscreen")
			.addEnforcedAttribute("a", "rel", "nofollow")
			.addProtocols("a", "href", "ftp", "http", "https")
			.addProtocols("img", "src", "ftp", "http", "https")
			.addProtocols("iframe", "src", "http", "https")
		)
		resource.setUploads(params.uploadId)
		resource.editedTime = new Date()
		resource.editedUserId = model.user.id
		resource
	}

	@Secured(['isAuthenticated()'])
	def editResource = {
		def model = getModel()
		if (!model) {
			redirect(action:"news")
		}
		model.editResource = getResource(model)
		render(view:"index_loggedin", model:model)
	}

	@Secured(["hasRole('ROLE_ADMIN')"])
	def editResourceForAllBlocks = {
		def model = getAdminModel()
		model.editResource = getAdminResource()
		render(view:"index_loggedin", model:model)
	}

	@Secured(['isAuthenticated()'])
	def saveResource = {
		def model = getModel()
		if (!model) {
			redirect(action:"news")
		}
		def resource = updateResource(model, getResource(model))
		if (resource.save()) {
			redirect(action:"viewResource", params:[group:model.group.id, title:resource.title])
		}
		model.editResource = resource
		render(view:"index_loggedin", model:model)
	}

	@Secured(["hasRole('ROLE_ADMIN')"])
	def saveResourceForAllBlocks = {
		def model = getAdminModel()
		def resource = updateResource(model, getAdminResource())
		if (resource.save()) {
			redirect(action:"viewResourceForAllBlocks", params:[title:resource.title])
		}
		model.editResource = resource
		render(view:"index_loggedin", model:model)
	}

	@Secured(['isAuthenticated()'])
	def deleteResource = {
		def model = getModel()
		if (!model) {
			redirect(action:"news")
		}
		def resource = getResource(model)
		resource.delete(failOnError: true)
		redirect(action:"viewResource", params:[group:model.group.id, title:resource.title])
	}

	@Secured(["hasRole('ROLE_ADMIN')"])
	def deleteResourceForAllBlocks = {
		def model = getAdminModel()
		def resource = getAdminResource()
		resource.delete(failOnError: true)
		redirect(action:"viewResourceForAllBlocks", params:[title:resource.title])
	}

	@Secured(['isAuthenticated()'])
	def viewResource = {
		def model = getModel([notGroupAdmin:true])
		if (!model) {
			redirect(action:"news")
		}
		def resources = groupResourceService.findAllByUserGroupAndTitle(model.group, params.title)
		if (!resources) {
			redirect(action:"news")
		}
		model.resources = resources
		render(view:"index_loggedin", model:model)
	}

	@Secured(["hasRole('ROLE_ADMIN')"])
	def viewResourceForAllBlocks = {
		def model = getAdminModel()
		def resources = GroupResource.findAllByTitleAndForAllBlocks(params.title, true, [sort:"createdTime", order:"asc"])
		if (!resources) {
			redirect(action:"news")
		}
		model.resources = resources
		render(view:"index_loggedin", model:model)
	}

	@Secured(['isAuthenticated()'])
	def viewUserProfile = {
		def currentUser = springSecurityService.currentUser
		def isRoleAdmin = currentUser.hasRole(UserRole.ROLE_ADMIN)

		def forUserId = params.userId as Long

		if (!isRoleAdmin && !membershipService.haveCommonMembership(forUserId, currentUser.id)) {
			redirect(action: "news")
		} else {
			def forUser = User.getOrFail(forUserId)

			def referenceTypes = UserReferenceType.getAll()
			def references = UserReference.findAllByForUser(forUser, [sort: "dateCreated", order: "desc"])

			render(view: "index_loggedin", model: [
				user: currentUser,
				isRoleAdmin: isRoleAdmin,
				forUser: forUser,
				referenceTypes: referenceTypes,
				references: getModelReferences(currentUser, references),
				canReference: currentUser.id != forUser.id
			])
		}
	}

	private def getModelReferences(User user, def references) {
		def result = []
		references.each {UserReference reference ->
			result.add([
					referenceId: reference.id,
					userId: reference.fromUser.id,
					displayName: reference.fromUser.displayName,
					userPhotoUrl: userService.getPhotoUrl(reference.fromUser.id, ImageSize.TINY),
					score: reference.score,
					subject: reference.subject,
					message: reference.message,
					dateCreated: reference.dateCreated,
					canDelete: reference.fromUser.id == user.id || user.hasRole(UserRole.ROLE_ADMIN) ? "true" : "false"
			])
		}
		result
	}
}
