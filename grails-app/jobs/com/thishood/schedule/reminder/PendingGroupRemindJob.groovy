package com.thishood.schedule.reminder

class PendingGroupRemindJob {
	def sessionRequired = false
	def concurrent = false

	def remindAdminService

	static triggers = {
		cron name: "Remind-PendingGroup", cronExpression: "0 0 12 * * ?"
	}

	def group = "remindGroup"

	def execute() {
		log.debug "starting " + this.class.simpleName

		remindAdminService.pendingGroup()
	}

}
