package com.thishood.groups

import com.thishood.domain.ConvertUtils
import com.thishood.domain.GroupNotification
import com.thishood.domain.Membership
import com.thishood.domain.User
import grails.converters.JSON
import grails.plugins.springsecurity.Secured
import com.thishood.domain.NotificationFrequency

@Secured(['isAuthenticated()'])
class MembershipController {

    def springSecurityService
    def membershipService

    def index = { }

    def settingsEdit = {
        User user = springSecurityService.currentUser
        Membership membership = membershipService.findByUserAndGroup(user.id, ConvertUtils.toLong(params.groupId))

        if (!membership) throw new IllegalArgumentException("Can't find membership for user [${user}] with group [${params.groupId}]")

		// adding custom UI property not exists in domain/command
        if (membership.notification) {
            membership.metaClass.customNotification = true
        } else {
            membership.metaClass.customNotification = false
            membership.notification = new GroupNotification()
        }

        render view: "settings", model: ["membership": membership]
    }

    def settingsUpdate = {
        def result = [:]// the JSON response

        User user = springSecurityService.currentUser
        Membership membership = membershipService.findByUserAndGroup(user.id, ConvertUtils.toLong(params.groupId))

        if (!membership) throw new IllegalArgumentException("Can't find membership for user [${user}] with group [${params.groupId}]")

        // custom UI property not exists in domain/command
        def customNotification = params.boolean("customNotification")

        //assembling to have embedded object
        def assembled = new Membership(params)

        try {
            membershipService.update(
                    id: membership.id,
                    //userId: membership.user.id,
                    //userGroupId: membership.userGroup.id,
                    notification: customNotification ? (assembled.notification?:new GroupNotification()) : null,
					frequency: assembled.frequency,
					version: ConvertUtils.toLong(params.version)
            )
            result.success = true
            result.response = "Membership settings were updated"
        } catch (any) {
            log.error("Unknown error on updating membership ${membership}", any)

            result.success = false
            result.response = "Unknown error on updating membership"
        }

        render result as JSON
    }
}
