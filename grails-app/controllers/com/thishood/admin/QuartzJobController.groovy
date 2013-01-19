package com.thishood.admin

import com.thishood.domain.NotificationFrequency
import com.thishood.schedule.stream.DailyNotificationJob
import com.thishood.schedule.stream.HourlyNotificationJob
import com.thishood.schedule.stream.RightAwayNotificationJob
import com.thishood.schedule.stream.ThreePerDayNotificationJob
import grails.plugins.springsecurity.Secured
import com.thishood.schedule.stream.OncePer2DaysNotificationJob
import com.thishood.schedule.stream.WeeklyNotificationJob

import com.thishood.domain.GroupStatus
import com.thishood.schedule.reminder.PendingGroupRemindJob
import com.thishood.schedule.stream.RssFetchJob
import com.thishood.schedule.metric.CommunityMetricJob
import com.thishood.schedule.metric.ApplicationMetricJob
import com.thishood.schedule.register.ProspectLastDayNotificationJob

/**
 * Controller for admin purpose only
 * Allows to manually trigger jobs from UI
 */
@Secured(["hasRole('ROLE_ADMIN')"])
class QuartzJobController {

    def index = {
        render(view: "index", model: [data: data])
    }

    def execute = {
        def index = params.int('id')
        def job = data[index].job

        log.debug "Explicitely triggering job ${job}"

        job.triggerNow()
    }

    /**
     * List of all jobs with their description
     * Data is defined in controller instead of gsp because it solves problem with security (only jobs mentioned could be executed)
     * and easy solves problem with identification -- index of job is more than enough
     */
    static def data = [
            [
                    job: RightAwayNotificationJob,
                    description: "Notify users who subscribed for notification via frequency '${NotificationFrequency.RIGHT_AWAY}'"
            ],
            [
                    job: HourlyNotificationJob,
                    description: "Notify users who subscribed for notification via frequency '${NotificationFrequency.HOURLY}'"
            ],
            [
                    job: ThreePerDayNotificationJob,
                    description: "Notify users who subscribed for notification  via frequency '${NotificationFrequency.THREE_PER_DAY}'"
            ],
            [
                    job: DailyNotificationJob,
                    description: "Notify users who subscribed for notification via frequency '${NotificationFrequency.DAILY}'"
            ],
            [
                    job: OncePer2DaysNotificationJob,
                    description: "Notify users who subscribed for notification via frequency '${NotificationFrequency.ONCE_PER_2DAYS}'"
            ],
            [
                    job: WeeklyNotificationJob,
                    description: "Notify users who subscribed for notification via frequency '${NotificationFrequency.WEEKLY}'"
            ],
            [
                    job: PendingGroupRemindJob,
                    description: "Remind admins about groups with status '${GroupStatus.PENDING}'"
            ],
            [
                    job: RssFetchJob,
                    description: "Fetch RSS content"
            ],
            [
                    job: ProspectLastDayNotificationJob,
                    description: "Notify about new prospect users since last 24h"
            ],
            [
                    job: ApplicationMetricJob,
                    description: "Gathering of application metrics (users, communities, posts, comments, etc)"
            ],
            [
                    job: CommunityMetricJob,
                    description: "Gathering of community metrics (posts, comments, etc)"
            ]
    ]

}

