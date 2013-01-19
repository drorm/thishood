package com.thishood.userSettings

import com.thishood.domain.NotificationFrequency

/**
 * @see com.thishood.domain.UserNotification
 */
class UserNotificationCommand {
    Long version

    NotificationFrequency frequency = NotificationFrequency.RIGHT_AWAY
    /**
     * @see com.thishood.domain.UserNotification#myCommentReply
     */
    Boolean myCommentReply = true
    /**
     * @see com.thishood.domain.UserNotification#postCreated
     */
    Boolean postCreated = true
    /**
     * @see com.thishood.domain.UserNotification#commentCreated
     */
    Boolean commentCreated = true

    static constraints = {
        frequency(nullable: false)
        myCommentReply(nullable: false)
        postCreated(nullable: false)
        commentCreated(nullable: false)
    }
}
