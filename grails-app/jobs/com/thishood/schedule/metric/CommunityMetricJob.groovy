package com.thishood.schedule.metric

class CommunityMetricJob {
	def sessionRequired = false
	def concurrent = false

	def communityMetricService

	static triggers = {
		cron name: "Metric-Community", cronExpression: "0 0 0 * * ?"
	}

	def group = "metricGroup"

	def execute() {
		log.debug "starting " + this.class.simpleName

		communityMetricService.calculateAll(new Date()-1)
	}

}
