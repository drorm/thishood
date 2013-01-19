package com.thishood

import grails.util.Environment
import grails.plugins.springsecurity.Secured

@Secured(["permitAll"])
class GoController {

    def home = {
		if(Environment.isDevelopmentMode()) {
			flash.message="For development environment redirection to /home is NOT performed (due absolute link)"
			render view: '/anonymous-info'
		} else {
			redirect url:'/home'
		}
	}
}
