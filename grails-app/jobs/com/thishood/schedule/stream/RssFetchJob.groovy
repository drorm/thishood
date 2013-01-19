package com.thishood.schedule.stream

class RssFetchJob {
	def sessionRequired = false
	def concurrent = false
	def foreignContentService

	static triggers = {
		cron name: "RssFetch", cronExpression: "0 0/5 * * * ?"
	}

	def group = "rss"

	def execute() {
		log.debug "starting " + this.class.simpleName

		foreignContentService.fetch(com.thishood.ForeignContentService.RSS_ALBANY)
	}
}
