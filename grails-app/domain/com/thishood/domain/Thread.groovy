package com.thishood.domain

import com.thishood.domain.UserGroup

/**
 * @see ThreadReply
 */
class Thread {
    static belongsTo = [userGroup: UserGroup]

    User fromUser
    Date dateCreated
    Date dateUpdated
    String message
    Integer repliesCount
	Boolean disableComments = false

	static constraints = {
		message(blank:false)
	}

    static mapping = {
        message type: "text"
    }

    String toString() {
        message
    }

    //Transient ID's of fields
    Long userGroupId
    Long fromUserId

    static transients = ['userGroupId', 'fromUserId']

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


}
