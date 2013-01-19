package com.thishood

import org.codehaus.groovy.grails.commons.GrailsDomainClass

class DomainValidationException extends AppException{
	private def domain

	DomainValidationException(def domain) {
		this.domain = domain
	}

	def getDomain() {
		domain
	}
}
