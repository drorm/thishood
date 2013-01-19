package com.thishood.domain

/**
 * User is a member in Group with specific authorities
 * @see GroupJoinAccessLevel
 * @see GroupModerationType
 * @see MembershipVerification
 */
class Membership {
	UserGroup userGroup
	User user

	NotificationFrequency frequency
	GroupNotification notification

	//
	//static belongsTo = [UserGroup, User]

	static hasMany = [
			authorities: MembershipAuthority	// in 90% cases it will be 1 authority only, in other 10% will be < 5 authorities
	]
	static mapping = {
		authorities joinTable: [
				name: 'membership_authority',
				key: 'membership_id',
				column: 'authority'
		]
	}

	static constraints = {
		userGroup nullable: false
		user nullable: false
		frequency nullable: true
		notification nullable: true
	}

	static embedded = ['notification']

	//Transient ID's of fields
	Long userId
	Long userGroupId

	static transients = ['userId', 'userGroupId']

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	boolean hasAuthority(authority) {
		def value
		if (authority instanceof MembershipAuthority) {
			value = authority
		} else if (authority instanceof String) {
			value = MembershipAuthority.valueOf(authority)
		} else {
			throw new IllegalArgumentException("Can't process ${authority}")
		}
		authorities.contains(value)
	}

}
