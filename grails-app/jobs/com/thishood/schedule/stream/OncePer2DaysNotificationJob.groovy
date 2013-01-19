package com.thishood.schedule.stream

import com.thishood.domain.NotificationFrequency

/**
 * @see com.thishood.domain.NotificationFrequency#ONCE_PER_2DAYS
 */
class OncePer2DaysNotificationJob {
    def sessionRequired = false
    def concurrent = false

    def notifyUserService

    static triggers = {
        cron name: "Notification-${NotificationFrequency.ONCE_PER_2DAYS}", cronExpression: "0 0 6 */2 * ?"
    }

    def group = "notificationGroup"

    def execute() {
        log.debug "starting " + this.class.simpleName

        notifyUserService.notifyByFrequency(NotificationFrequency.ONCE_PER_2DAYS)
    }
}
