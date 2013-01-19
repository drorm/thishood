package com.thishood.domain

/**
 * A group of people having common interests
 * @see Hood
 *
 */
class Group extends UserGroup {

	Group() {
		discriminatorType = UserGroupDiscriminatorType.GROUP
	}

	static constraints = {
	}

	static mapping = {
		discriminator UserGroupDiscriminatorType.GROUP.name()
	}

}
