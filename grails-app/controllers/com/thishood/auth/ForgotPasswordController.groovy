package com.thishood.auth

import com.thishood.domain.VerificationCode
import grails.plugins.springsecurity.Secured
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

@Secured(["isAnonymous()"])
class ForgotPasswordController {

    def grailsApplication
    def membershipService
    def queueMailerService
    def userService
    def springSecurityService

    static defaultAction = 'forgot'

    def forgot = {
        if (!request.post) {
            // show the form
            return
        }

        String email = params.email
        if (!email) {
            flash.error = 'Please enter your email'
            return
        }


        def user = userService.findByEmail(email)
        if (!user) {
            //todo vitaliy@10.03.11 add captcha to avoid continious email check by hackers
            flash.error = "No user was found with this email"
            return
        }

        //todo vitaliy@09.03.11 remove old record if already exists with such email
        def verificationCode = new VerificationCode(email: user.email)
                .save(failOnError: true)

        String url = generateLink('reset', [t: verificationCode.token])

        queueMailerService.sendEmail(
                to: user.email,
                subject: "Password Reset",
                model: [
                        url: url,
                        user: user
                ],
                view: "/template/email/forgotPassword"
        )

        [emailSent: true]
    }

    def reset = { ResetPasswordCommand command ->

        String token = params.t

        def verificationCode = token ? VerificationCode.findByToken(token) : null
        if (!verificationCode) {
            flash.error = message(code: 'spring.security.ui.resetPassword.badCode')
            //redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
            render(view: "/anonymous-info")
            return
        }

        if (!request.post) {
            return [token: token, command: new ResetPasswordCommand()]
        }

        command.email = verificationCode.email
        command.validate()

        if (command.hasErrors()) {
            return [token: token, command: command]
        }

        VerificationCode.withTransaction { status ->
            userService.updatePassword(command.email, command.password)
            verificationCode.delete()
        }

        springSecurityService.reauthenticate verificationCode.email

        flash.message = message(code: 'spring.security.ui.resetPassword.success')

        def conf = SpringSecurityUtils.securityConfig
        String postResetUrl = conf.ui.register.postResetUrl ?: conf.successHandler.defaultTargetUrl
        redirect uri: postResetUrl
    }

    protected String generateLink(String action, linkParams) {
        createLink(
                base: grailsApplication.config.grails.serverURL,
                controller: 'forgotPassword',
                action: action,
                params: linkParams)

    }

}

