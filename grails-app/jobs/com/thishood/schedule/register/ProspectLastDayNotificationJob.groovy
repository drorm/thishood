package com.thishood.schedule.register

public class ProspectLastDayNotificationJob {
	def sessionRequired = false
	def concurrent = false

	def prospectService

	static triggers = {
		cron name: "register-prospect", cronExpression: "0 0 12 * * ?"
	}

	def group = "notificationGroup"

	def execute() {
		log.debug "starting " + this.class.simpleName

		prospectService.sendNotifications()
	}
}
