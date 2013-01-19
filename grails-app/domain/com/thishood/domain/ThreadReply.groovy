package com.thishood.domain


class ThreadReply {

    User fromUser
    Thread thread
    Date dateCreated
    Date dateUpdated
    String reply

    static constraints = {
        reply(blank: false)
    }

    static mapping = {
        reply type: "text"
    }

    // Transient ID's
    Long fromUserId
    Long threadId

    static transients = ['fromUserId', 'threadId']

    String toString() {
        reply
    }
}
