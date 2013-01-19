package com.thishood.schedule.stream

import com.thishood.domain.NotificationFrequency

/**
 * @see com.thishood.domain.NotificationFrequency#DAILY
 */
class DailyNotificationJob {
    def sessionRequired = false
    def concurrent = false

    def notifyUserService

    static triggers = {
        cron name: "Notification-${NotificationFrequency.DAILY}", cronExpression: "0 0 12 * * ?"
    }

    def group = "notificationGroup"

    def execute() {
        log.debug "starting " + this.class.simpleName

        notifyUserService.notifyByFrequency(NotificationFrequency.DAILY)
    }
}
