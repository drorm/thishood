package com.thishood.domain

/**
 * Slang a neighborhood - people living near one another
 * It's a geo-locational group
 * @see Group
 */
class Hood extends UserGroup {
	//todo add parent to create hierarchy: this has a sense this entity, because categories has to be used for communities
	//todo add type: block, building, district, ??
	//todo add geo polygons -- for echo? foxtrot? TH-270

	String street
	// start of the block: f/i 950
	String number
	String city
	String state
	String country

	Hood() {
		discriminatorType = UserGroupDiscriminatorType.HOOD

		joinAccessLevel = GroupJoinAccessLevel.RESTRICTED
	}

	// we can't set nullable=false because using table-per-hierarchy, but blank also checks for null without adding database NOT NULL constraint
	static constraints = {
		street nullable: true, blank: false
		number nullable: true, blank: false
		city nullable: true, blank: false
		state nullable: true, blank: false
		country nullable: true, blank: false
	}

	static mapping = {
		discriminator UserGroupDiscriminatorType.HOOD.name()
	}

}
