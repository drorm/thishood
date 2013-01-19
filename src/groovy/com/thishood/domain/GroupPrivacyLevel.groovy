package com.thishood.domain

/**
 * Defines view access level of group content posted by members
 * @see UserGroup
 * @see Membership
 */
public enum GroupPrivacyLevel implements StringValuedEnum {
    /**
     *  anyone can see posts in the group
     */
    OPEN('OPEN'),
    /**
     * only people in the group can see
     */
    GROUP('GROUP')

    final String value

    GroupPrivacyLevel(String value) {
        this.value = value
    }

    static list() {
        [OPEN, GROUP]
    }

}
