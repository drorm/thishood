package com.thishood

import grails.plugins.springsecurity.Secured

@Secured(["isAuthenticated()"])
class ForeignContentController {
	def foreignContentService

	/**
	 * While we are working with Albany blocks it's no sense to set param with rss URL -- hardcode it in action
	 */
	def rss = {
		request.setAttribute(ThisHoodConstant.ATTRIBUTE_ENABLE_CACHING,true)
		cache store:true, shared: true, validFor: 300, auth: false

		def (date, items) = foreignContentService.get(ForeignContentService.RSS_ALBANY)

		lastModified(date)

		render(view: "rss", model: [items: items])
	}
}
