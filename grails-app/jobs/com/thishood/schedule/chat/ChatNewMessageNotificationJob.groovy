package com.thishood.schedule.chat

public class ChatNewMessageNotificationJob {
	def sessionRequired = false
	def concurrent = false

	def chatNotificationService

	static triggers = {
		cron name: "chatNewMessage", cronExpression: "0 0/1 * * * ?"
	}

	def group = "notificationGroup"

	def execute() {
		log.debug "starting " + this.class.simpleName

		chatNotificationService.sendNotifications()
	}
}
