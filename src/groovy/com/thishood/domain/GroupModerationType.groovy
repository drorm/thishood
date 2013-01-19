package com.thishood.domain

/**
 * Defines how group can be moderated
 * @see UserGroup
 * @see Membership
 */
enum GroupModerationType implements StringValuedEnum {
    /**
     * no moderation - messages are delivered directly
     */
    NONE('NONE'),
    /**
     * all messages are held for moderation
     */
    ALL('ALL')
    /**
     * Moderate all messages posted to group for 1 week only of becoming member
     */
    //ONE_WEEK???

    final String value

    GroupModerationType(String value) {
        this.value = value
    }

    static list() {
        [NONE, ALL]
    }

}
