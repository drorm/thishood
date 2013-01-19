package com.thishood.registration

import com.thishood.domain.Hood
import com.thishood.domain.Prospect
import com.thishood.domain.SignupInvitation
import com.thishood.domain.User
import grails.plugins.springsecurity.Secured
import groovyx.net.http.HTTPBuilder

class RegisterController {

	static defaultAction = 'index'

	def grailsApplication
	def springSecurityService
	def userService
	def queueMailerService
	def facebookGraphService
	def hoodService
	def signupService
	def recaptchaService


	@Secured(["permitAll()"])
	def index = {
		if (springSecurityService.isLoggedIn()) {
			flash.message = 'Looks like you are already signed up to ThisHood.'
			render view: '/loggedin-info'
			return
		}
		render view: "index", model: params
	}

	@Secured(["permitAll()"])
	def theform = {
		if (springSecurityService.isLoggedIn()) {
			flash.message = 'Looks like you already have a ThisHood account.'
			render view: '/loggedin-info'
			return
		}

		if (!params.state) {
			flash.error = "Please select a State."
			render view: "index", model: params
			return
		}
		if (!params.city) {
			flash.error = "Please enter City."
			render view: "index", model: params
			return
		}

		flash.error = null

		def myCity = params.city.toLowerCase()
		if (!recaptchaService.verifyAnswer(session, request.remoteAddr, params)) {
			flash.error="CAPTCHA failed"
			log.warn("Wrong recaptcha for [${request.remoteAddr}]")
			render view: "index", model: params
		} else if (params.state != "CA" || myCity != "albany") {
			render view: "addProspect", model: params
		} else {
			def command = new RegisterCommand()
			command.state = params.state
			command.city = params.city
			command.country = 'USA'
			renderForm(command)
		}
	}

	@Secured(["permitAll()"])
	def addProspect = {
		if (springSecurityService.isLoggedIn()) {
			flash.message = 'Looks like you already have a ThisHood account.'
			render view: '/loggedin-info'
			return
		}
	}

	@Secured(["permitAll()"])
	def saveProspect = {
		if (springSecurityService.isLoggedIn()) {
			flash.message = 'Looks like you already have a ThisHood account.'
			render view: '/loggedin-info'
			return
		}
		if (!params.state) {
			flash.error = "Please select state."
			render view: "index", model: params
			return
		}
		if (!params.city) {
			flash.error = "Please enter city."
			render view: "index", model: params
			return
		}
		if (!params.email) {
			render view: "addProspect", model: params
			return
		}
		new Prospect(
			email: params.email,
			city: params.city,
			state: params.state,
			dateCreated: new Date()
		).save(failOnError: true)
		render view: "prospectSaved"
	}

	@Secured(["permitAll()"])
	def prospectSaved = {
		if (springSecurityService.isLoggedIn()) {
			flash.message = 'Looks like you already have a ThisHood account.'
			render view: '/loggedin-info'
			return
		}
	}

	@Secured(["permitAll()"])
	def form = {
		if (springSecurityService.isLoggedIn()) {
			flash.message = 'Looks like you already have a ThisHood account.'
			render view: '/loggedin-info'
			return
		}

		def command = new RegisterCommand()
		if (params.token) {
			SignupInvitation signupInvitation = SignupInvitation.findByToken(params.token)
			command.email = signupInvitation?.email
		}
		renderForm(command)
	}

	@Secured(["isAnonymous()"])
	def register = { RegisterCommand command ->

		if (command.hasErrors()) {
			renderForm(command)

			return
		}

		def photo = null

		//save a photo of user from facebook account
		/** disabled due reload problem
		if (session.facebook) {
			//Fetch the image from Facebook and bring it locally
			def facebookInfo = JSON.parse(facebookGraphService.facebookProfile.toString())
			def facebookPhotoUrl = facebookGraphService.getProfilePhotoSrc(facebookInfo.id)

			photo = fetchResource(facebookPhotoUrl)
		}
		*/

		try {
			def user = signupService.newRegistration(command, photo)

			if (user.hasErrors()) {
				def err = ""
				user.errors.each {
					err += it
				}
				flash.error = "Registration failed:" + err
				renderForm(command)
				return
			}
		} catch (any) {
			log.error("Error occured on signup", any)

			flash.error = "Registration failed: ${any.message}"
			renderForm(command)
			return
		}

		render view: 'registrationComplete', model: [emailSent: true]
	}

	private def renderForm = { RegisterCommand command ->

		List<Hood> hoods = hoodService.findAllActive()
		Hood hood = null
		if (params.hood) {
			def hoodId = params.hood as Long
			hood = hoods.find {it.id == hoodId}
		}
		render view: 'form', model: [command: command, hoods: hoods, hood: hood, token: request.token ?: params.token]
	}


	@Secured(["isAnonymous()"])
	def verify = {
		String token = params.token

		try {
			User user = signupService.verifyRegistration(token)

			springSecurityService.reauthenticate(user.email)

			flash.message = message(code: 'spring.security.ui.register.complete')
			render(view: "welcome")

		} catch (any) {
			log.error("Error occured on signup verification for token [${token}]", any)
			flash.error = "Error occured on verification, probably token is not valid"
			render(view: "/anonymous-info")
		}
	}
	
	@Secured(["isAnonymous()"])
	def welcome = {
		
	}

	protected fetchResource(String url) {
		log.debug("Trying to fetch resource from ${url}")
		def content = new ByteArrayOutputStream(1024 * 10)
		def httpBuilder = new HTTPBuilder(url)
		try {
			httpBuilder.get(contentType: groovyx.net.http.ContentType.BINARY, headers: ['User-Agent': 'ThisHood-HTTP/1.0']) { resp, img ->
				content << img
			}
		} catch (e) {
			log.info("Unable to fetch image", e)
		}
		content.toByteArray()
	}
}

