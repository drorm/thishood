package com.thishood.domain

/**
 * Keeps information of users who have to be approved by group admins for join
 * @see Membership
 */
class MembershipVerification {
	UserGroup group
	User user
	String description
	//unique code
	String code
	MembershipVerificationStatus status

	//will be used to find too old records
	Date dateCreated


    static constraints = {
		description nullable: true
		code nullable: false
		status nullable: false
    }
}
