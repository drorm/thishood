package com.thishood.schedule.stream

import com.thishood.domain.NotificationFrequency

/**
 * @see NotificationFrequency#HOURLY
 */
class HourlyNotificationJob {
    def sessionRequired = false
    def concurrent = false

    def notifyUserService

    static triggers = {
        cron name: "notification-${NotificationFrequency.HOURLY}", cronExpression: "0 0 0/1 * * ?"
    }

    def group = "notificationGroup"

    def execute() {
        log.debug "starting " + this.class.simpleName

        notifyUserService.notifyByFrequency(NotificationFrequency.HOURLY)
    }
}
