package com.thishood.schedule.stream

import com.thishood.domain.NotificationFrequency

/**
 * @see com.thishood.domain.NotificationFrequency#WEEKLY
 */
class WeeklyNotificationJob {
    def sessionRequired = false
    def concurrent = false

    def notifyUserService

    static triggers = {
        cron name: "Notification-${NotificationFrequency.WEEKLY}", cronExpression: "0 0 6 ? * 2"
    }

    def group = "notificationGroup"

    def execute() {
        log.debug "starting " + this.class.simpleName

        notifyUserService.notifyByFrequency(NotificationFrequency.WEEKLY)
    }
}
