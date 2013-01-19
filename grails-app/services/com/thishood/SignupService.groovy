package com.thishood

import com.thishood.domain.User
import com.thishood.registration.RegisterCommand
import com.thishood.domain.VerificationCode
import com.thishood.domain.SignupInvitation
import com.thishood.domain.Hood
import com.thishood.domain.SignupInvitationStatus

class SignupService {

    static transactional = true

	def grailsApplication
	def springSecurityService
	def userService
	def queueMailerService
	def userGroupService
	def hoodService
	def membershipService
	def helperService

    def newRegistration(RegisterCommand command, photo) {

		User user = userService.create(
				first: command.first,
				middle: command.middle,
				last: command.last,
				email: command.email,
				address: command.address,
				city: command.city,
				state: command.state,
				zip: command.zip,
				country: command.country,
				password: command.password
		)

		if (user.validate() && !user.hasErrors()) {
			user = user.save(failOnError:true, flush:true)

			if (photo) {
				//todo TH-337 Update persisting of user photo on registration (from facebook)
            	//userService.setPhoto(user.id, photo)
			}

			Hood hood = Hood.get(command.hoodId) // because it's optional!!

			SignupInvitation signupInvitation
			if (command.token) {
				signupInvitation = SignupInvitation.findByToken(command.token)
				if (!signupInvitation) throw new IllegalArgumentException("Can't find invitation by token [${command.token}]")
				if (signupInvitation.status != SignupInvitationStatus.INVITED) throw new IllegalArgumentException("Invitation is not valid anymore")
				signupInvitation.status=SignupInvitationStatus.REGISTERED
			} else {
				signupInvitation = SignupInvitation.findByEmail(command.email)
				if (signupInvitation) {
					//TH-356 allow to register without invitation link
					//throw new IllegalArgumentException("User with such email was invited. Use invitation link")
					signupInvitation.status = SignupInvitationStatus.REGISTERED
					if (signupInvitation.hood != hood) {
						log.error("Potential registration error by user [${user.email}]: set hood [${hood}] but was invited to [${signupInvitation.hood}]")
						signupInvitation.hood = hood
						//we use to perform automatic signup
						signupInvitation.invitedPersonally = false
					}
				} else {
					signupInvitation = new SignupInvitation(
							status:SignupInvitationStatus.REGISTERED,
							//personal: recipient.personal,
							email: user.email,
							hood: hood,
							invitedPersonally: false,
							dateCreated: new Date(),
							token: UUIDGenerator.next()
					)
				}
			}
			signupInvitation.save(failOnError: true, flush:true)


			String url = helperService.generateLink('register', 'verify', [token: signupInvitation.token])

			queueMailerService.sendEmail(
					to: command.email,
					subject: "Registration on ThisHood",
					model: [
							url: url,
							user: user
					],
					view: "/template/email/signup/newRegistration"
			)

		}
		user
    }

	def verifyRegistration(String token){
		if (!token) throw new IllegalArgumentException("Token is not specified")

		def signupInvitation = SignupInvitation.findByToken(token)

		if (!signupInvitation) throw new IllegalArgumentException("Invitation data doesn't exists")
		if (signupInvitation.status == SignupInvitationStatus.VERIFIED) throw new IllegalArgumentException("Verification already processed")
		if (signupInvitation.status != SignupInvitationStatus.REGISTERED) throw new IllegalArgumentException("Verification token is not valid")

		signupInvitation.lock()

		User user = userService.findByEmail(signupInvitation.email)

		if (!user) throw new IllegalArgumentException("Can't find user by specified token")

		user.lock()
		user.accountLocked = false
		user.enabled = true
		user.save(failOnError: true, flush: true)

		if (signupInvitation.hood) { // this is an optional field
			if (signupInvitation.invitedPersonally) {
				membershipService.startMembership(user.id, signupInvitation.hood.id)
			} else {
				userGroupService.joinGroup(signupInvitation.hood.id, user.id)
			}
		}
		signupInvitation.status = SignupInvitationStatus.VERIFIED
		signupInvitation.save(failOnError:true, flush:true)

		user
	}

}
