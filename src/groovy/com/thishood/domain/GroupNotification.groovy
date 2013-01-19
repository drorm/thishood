package com.thishood.domain

class GroupNotification {
    //Someone replies to your message
    Boolean myCommentReply = false
    //Someone starts a new topic
    Boolean postCreated = false
    //someone replies to a new topc
    Boolean commentCreated = false

    static constraints = {
        myCommentReply(nullable: false)
        postCreated(nullable: false)
        commentCreated(nullable: false)
    }

}
