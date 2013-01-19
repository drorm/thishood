package com.thishood.domain

/**
 * Keeps history information about users who were members: when join, left, why, etc
 * @see Membership
 */
class MembershipHistory {
	UserGroup group
	User user
	MembershipStatus status

	Date requestAt
	Date joinAt
	Date leaveAt

	String joinReason
	String declineReason
	String leaveReason

    static constraints = {
    }
}
