package com.thishood.error

import grails.util.Environment
import org.codehaus.groovy.grails.web.errors.GrailsWrappedRuntimeException

class ErrorController {

	def index = {
		def exception = request.exception
		if (exception instanceof GrailsWrappedRuntimeException) {
			log.error "exception $exception.className, line $exception.lineNumber has throw $exception.cause"
			def cause = exception?.cause

			if (Environment.current == Environment.DEVELOPMENT) {
				render view: "full-info"
			} else {
				render view: "nice-info"
			}
		} else {
			//render(text: "Exception in ${exception?.className}", contentType: "text/plain", encoding: "UTF-8")
			render view: "/error"
		}
	}
}
