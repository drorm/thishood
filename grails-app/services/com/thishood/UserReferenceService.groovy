package com.thishood

import com.thishood.domain.*
import org.apache.commons.lang.StringUtils

class UserReferenceService {

	static transactional = true

	def grailsApplication
	def membershipService
	def queueMailerService

	UserReference createReference(params) {
		User forUser = User.getOrFail(params.forUserId)
		User fromUser = User.getOrFail(params.fromUserId)
		String subject = StringUtils.trim(params.subject)
		String message = StringUtils.trim(params.message)
		Integer score = 1 // should be set/calculated/checked on server side to prevent cheating

		//
		if (forUser.id == fromUser.id || (!membershipService.haveCommonMembership(forUser.id, fromUser.id) && !currentUser.hasRole(UserRole.ROLE_ADMIN))) {
			throw new IllegalArgumentException("User [${fromUser.id}] is not authorized to create reference for user [${forUser.id}]")
		}

		if (!UserReferenceType.findByType(subject)) {
			throw new IllegalArgumentException("Invalid type.")
		}

		def reference = new UserReference(
			forUser: forUser,
			fromUser: fromUser,
			score: score,
			subject: subject,
			message: message,
			dateCreated: new Date()
		)
		//todo: validation not performed!!
		reference = reference.save(flush:true, failOnError: true)

		notifyUser(reference)

		return reference
	}

	void deleteReference(Long userId, Long referenceId) {
		UserReference reference = UserReference.getOrFail(referenceId)
		User user = User.getOrFail(userId)

		if (reference.fromUser.id != user.id && !user.hasRole(UserRole.ROLE_ADMIN)) {
			throw new IllegalArgumentException("User [${user}] is not authorized to delete reference [${reference.id}]")
		}
		reference.delete(failOnError: true)
	}

	private void notifyUser(UserReference recommendation) {
		User user = recommendation.forUser

		queueMailerService.sendEmail(
				to: user.emailWithName,
				subject: recommendation.fromUser.displayName +" recommended you in " + recommendation.subject + ", ThisHood",
				model: [
						config: grailsApplication.config,
						creator: user,
						recommendation: recommendation,

				],
				view: "/template/email/recommendation/newCreated")

	}

}
