package com.thishood

class HelperService {

    static transactional = false

	def grailsApplication

	protected String generateLink(String controller, String action, linkParams) {
		def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()
		g.createLink(base: grailsApplication.config.grails.serverURL,
				controller: controller,
				action: action,
				params: linkParams)

	}

}
