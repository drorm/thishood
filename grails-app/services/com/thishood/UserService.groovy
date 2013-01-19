package com.thishood

import com.thishood.domain.*
import grails.util.GrailsConfig
import org.codehaus.groovy.grails.plugins.springsecurity.NullSaltSource
import com.thishood.domain.Upload
import com.thishood.image.ImageSize

class UserService {
	static transactional = true

	def grailsApplication
	def saltSource
	def springSecurityService
	def membershipService
	def notificationService
	def userGroupService
	def uploadService

	def createUserOnce(params) {
		User.findByEmail(params.email) ?: create(params)
	}

	def getUser(Long userId) {
		return User.get(userId)
	}

	List<User> findAllActiveByRole(UserRole role) {
		User.executeQuery("select ur.user from SecUserSecRole ur where ur.secRole.authority=:role and ur.user.enabled=true", [role: role.name()])
	}

	User findByEmail(String email) {
		if (!email) throw new IllegalArgumentException("Parameter [email] is not set")
		User.findByEmail(email)
	}

	User create(params) {
		Date now = new Date()

		def salt = generateSalt()
		def password = params.password

		User user = new User(
				first: params.first,
				middle: params.middle,
				last: params.last,
				email: params.email,
				address: params.address,
				city: params.city,
				state: params.state,
				zip: params.zip,
				country: params.country,
				salt: generateSalt(),
				password: springSecurityService.encodePassword(password, salt),
				dateCreated: now,
				lastUpdated: now,
				enabled: false,		//new user will have to confirm
				accountLocked: true, //new user will have to confirm
				tourTaken: false
		)

		if (user.validate()) {
			user = user.save(failOnError: true, flush: true)

			//create all dependencies

			// Add to default groups
			def defaultGroup = userGroupService.findByName(grailsApplication.config.thishood.default.usergroup)
			membershipService.startMembership(user.id, defaultGroup.id)
			def market = userGroupService.findByName(grailsApplication.config.thishood.market.usergroup)
			membershipService.startMembership(user.id, market.id)
			// remarked on Dror's request
			//def testGroup = userGroupService.findByName("Test")
			//membershipService.startMembership(user.id, testGroup.id)

			// set default notification
			notificationService.saveUserNotification(userId: user.id)
		}

		user
	}

	User updatePassword(String email, String newPassword, boolean reauthenticate = false) {
		User user = findByEmail(email)

		//regenerating salt on password change to make it more sucure
		user.salt = generateSalt()
		user.password = springSecurityService.encodePassword(newPassword, user.salt)
		User persisted = user.save(failOnError: true, flush: true)
		if (reauthenticate) {
			springSecurityService.reauthenticate(email)
		}
		persisted
	}

	void setPhoto(Long userId, Long photoUploadId) {
		User user = User.getOrFail(userId)

		Upload upload = Upload.findByIdAndUser(photoUploadId, user);
		if (!upload) throw new IllegalArgumentException("Can't find upload [${photoUploadId}] for user [${user}]")

		user.photoUploadId = photoUploadId
		user.save(failOnError: true)
	}

	String getPhotoUrl(Long userId, ImageSize size) {
		User user = User.getOrFail(userId)
		Upload upload = Upload.get(user.photoUploadId)
		if (upload) {
			return uploadService.getImageUrl(upload.id, size)
		} else {
			//user has no photo, use the generic one
			return grailsApplication.config.grails.serverURL + "/identicon/view/" + (user.id + user.emailWithName).encodeAsMD5()
		}
	}

	def String generateSalt() {
		saltSource instanceof NullSaltSource ? null : UUID.randomUUID().toString().replaceAll("-", "");
	}

	boolean joinedMarket(user) {
		def market = UserGroup.findByName(grailsApplication.config.thishood.market.usergroup)
		if (!market) throw new IllegalStateException("Can't find UserGroup ${grailsApplication.config.thishood.market.usergroup}")

		def membership = Membership.findByUserAndUserGroup(user, market)

		return membership ? true : false
	}

}
