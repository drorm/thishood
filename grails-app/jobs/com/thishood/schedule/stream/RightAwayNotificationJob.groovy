package com.thishood.schedule.stream

import com.thishood.domain.NotificationFrequency

/**
 * @see NotificationFrequency#RIGHT_AWAY
 */
class RightAwayNotificationJob {
    def sessionRequired = false
    def concurrent = false

    def notifyUserService

    static triggers = {
        cron name: "Notification-${NotificationFrequency.RIGHT_AWAY}", cronExpression: "0 0/1 * * * ?"
    }

    def group = "notificationGroup"

    def execute() {
        log.debug "starting " + this.class.simpleName

        notifyUserService.notifyByFrequency(NotificationFrequency.RIGHT_AWAY)
    }
}
