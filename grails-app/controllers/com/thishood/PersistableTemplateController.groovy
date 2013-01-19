package com.thishood

import grails.plugins.springsecurity.Secured

@Secured(["isAuthenticated()"])
class PersistableTemplateController {

	def persistableTemplateService

    def getDefaultContent = {
		//not needed, but who knows?
		def userGroupId = params.group as Long
		render text: persistableTemplateService.getDefaultTemplate()
	}
}
