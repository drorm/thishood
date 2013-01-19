package com.thishood.main

import com.thishood.domain.*
import grails.converters.JSON
import grails.plugins.springsecurity.Secured
import com.thishood.image.ImageSize

@Secured(["isAuthenticated()"])
class UserReferenceController {

	def springSecurityService
	def userReferenceService
	def userService

	static allowedMethods = [createReference: "POST", deleteReference: "POST"]

	def createReference = {
		def result = [:]

		def user = springSecurityService.currentUser

		try {
			def reference = userReferenceService.createReference(
					forUserId: params.forUserId as Long,
					fromUserId: user.id,
					subject: params.subject,
					message: params.message
			)

			def isRoleAdmin = user.hasRole(UserRole.ROLE_ADMIN)

			result = [
				referenceId: reference.id,
				userId: reference.fromUser.id,
				displayName: reference.fromUser.displayName.encodeAsHTML(),
				userPhotoUrl: userService.getPhotoUrl(reference.fromUser.id, ImageSize.TINY),
				score: reference.score,
				subject: reference.subject,
				message: reference.message,
				dateCreated: reference.dateCreated,
				canDelete: reference.fromUser.id == user.id || isRoleAdmin ? true : false
			]
		} catch (any) {
			log.error("Error ocurred when user [${user}] tried to created reference", any)
			result = [error: any.message]
		}

		render result as JSON
	}

	def deleteReference = {
		def result = [:]
		def user = springSecurityService.currentUser

		Long referenceId = params.referenceId as Long

		try {
			userReferenceService.deleteReference(user.id, referenceId)
			result = [ok:true]
		} catch (any) {
			log.error("User [${user}] can't delete reference", any)
			result = [error: any.message]
		}
		render result as JSON
	}

}
