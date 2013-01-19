package com.thishood.schedule.metric

class ApplicationMetricJob {
	def sessionRequired = false
	def concurrent = false

	def applicationMetricService

	static triggers = {
		cron name: "Metric-Application", cronExpression: "0 0 0 * * ?"
	}

	def group = "metricGroup"

	def execute() {
		log.debug "starting " + this.class.simpleName

		applicationMetricService.gatherAll()
	}

}
