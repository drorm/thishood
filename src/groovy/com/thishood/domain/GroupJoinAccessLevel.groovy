package com.thishood.domain

/**
 * Defines how users can become new members to group
 * @see Membership
 * @see UserGroup
 */
enum GroupJoinAccessLevel implements StringValuedEnum {
    /**
     * anyone can join
     */
    OPEN('OPEN'),
    /**
     * moderator needs to approve people
     */
    RESTRICTED('RESTRICTED');

    final String value

    GroupJoinAccessLevel(String value) {
        this.value = value
    }

    static list() {
        [OPEN, RESTRICTED]
    }

}
