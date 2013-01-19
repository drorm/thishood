package com.thishood.schedule.stream

import com.thishood.domain.NotificationFrequency

/**
 * @see com.thishood.domain.NotificationFrequency#THREE_PER_DAY
 */
class ThreePerDayNotificationJob {
    def sessionRequired = false
    def concurrent = false

    def notifyUserService

    static triggers = {
        cron name: "Notification-${NotificationFrequency.THREE_PER_DAY}", cronExpression: "0 0 0/8 * * ?"
    }

    def group = "notificationGroup"

    def execute() {
        log.debug "starting " + this.class.simpleName

        notifyUserService.notifyByFrequency(NotificationFrequency.THREE_PER_DAY)
    }
}
