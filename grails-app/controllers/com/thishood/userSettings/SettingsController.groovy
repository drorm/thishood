package com.thishood.userSettings

import com.thishood.domain.User
import com.thishood.domain.UserNotification
import grails.converters.JSON
import grails.plugins.springsecurity.Secured
import com.thishood.image.ImageSize

@Secured(["isAuthenticated()"])
class SettingsController {

	def springSecurityService
	def messageSource
	def saltSource
	def notificationService
	def userService
	def userGroupService

	static allowedMethods = [setUserPhoto: "POST"]
	
	def index = {}

	def setUserPhoto = {
		def result = [:]
		def user = springSecurityService.currentUser

		def uploadId = params.uploadId as Long

		try {
			userService.setPhoto(user.id, uploadId)
			result = [url: userService.getPhotoUrl(user.id, ImageSize.SMALL)]
		} catch (any) {
			log.error("Can't set photo for user [${user}]",any)
			result = [error: any.message]
		}

		render result as JSON
	}

	def infoView = {
	}

	def infoEdit = {
		def user = springSecurityService.currentUser

		render(view: "infoEdit", model: [user: user])
	}

	def infoUpdate = {
		def user = springSecurityService.currentUser
		user.properties = params
		if (!user.hasErrors() && user.save(flush: true)) {
			flash.message = "${message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), user.id])}"
				render(view: "infoView", model: [user: user])
		} else {
			render(view: "infoEdit", model: [user: user])
		}
	}
	
	def emailUpdate = {
		def user = springSecurityService.currentUser
		user.email = params.email
		if (!user.hasErrors() && user.save(flush: true)) {
			flash.message = "${message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), user.id])}"
			render(view: "infoView", model: [user: user])
		} else {
			render(view: "infoEdit", model: [user: user])
		}
	}

	def passwordEdit = {
		render view: "password"
	}

	def passwordUpdate = { UserPasswordCommand command ->
		def result = [:]// the JSON response

		User user = springSecurityService.currentUser
		//set explicitely for validator
		command.email = user.email
		command.validate()

		if (!command.hasErrors()) {
			try {
				userService.updatePassword(user.email, command.password, true)

				result.success = true;
				result.response = "Password has been changed"
			} catch (any) {
				log.error("Error occured on change password ${any}")

				result.success = false;
				result.response = "Unknown error occured during change password"
			}
		} else {
			result.success = false;
			result.response = command.errors.allErrors.collect({messageSource.getMessage(it, null)}).join("<br/>")
		}

		render result as JSON
	}

	def showNotifications = {
		def command = notificationService.findUserNotificationByUser(springSecurityService.currentUser.id)

		render view: 'notifications', model: [command: command]
	}

	def updateNotifications = {
		def user = springSecurityService.currentUser

		//assembling to have embedded object
		def assembled = new UserNotification(params)

		try {
			notificationService.saveUserNotification(
					userId: user.id,
					groupNotification: assembled.groupNotification,
					frequency: assembled.frequency
			)
			flash.message = "Your changes have been saved"
		} catch (Exception e) {
			flash.error = "Can't save notification settings"
			//todo vitaliy@21.02.11 process it
			log.error("Can't save user notification settings for user [${user.id}]", e)
		}
		// re-rendering what is persisted in database
		showNotifications()
	}
	
	def photo = {
		
	}
}
