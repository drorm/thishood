package com.thishood.domain

/**
 * Entity holds commont properties for 2 kind of groups:
 * - hood (geo locational)
 * - group (by interest)
 *
 * @see Membership
 * @see GroupStatus
 * @see GroupModerationType
 * @see GroupJoinAccessLevel
 * @see GroupPrivacyLevel
 */
//todo rename to Community as describing abstract meaning: group people by interest and people located in the same area
class UserGroup implements Serializable {
	/**
	 * Read-only field only for convinient
	 */
	private UserGroupDiscriminatorType discriminatorType

	String name
	String description
	String about
	String news //temporary till we get the news section working
	Date dateCreated
	Date lastUpdated
	Integer photo	// will be removed!!! needed for Delta only
	Long imageUploadId

	GroupStatus status

	GroupModerationType moderationType
	GroupJoinAccessLevel joinAccessLevel
	GroupPrivacyLevel privacyLevel

	UserGroup parent

	//statistics
	Integer statPopularityIndex = 0

	static constraints = {
		name(blank: false, unique: true)
		description(nullable: false, blank: false)
		about(nullable: false, blank: false)
		news(nullable: true, blank: false)
		photo(display: false, nullable: true)
		imageUploadId(display: false, nullable: true)
		status(nullable: false)
		moderationType(nullable: false)
		joinAccessLevel(nullable: false)
		privacyLevel(nullable: false)
		statPopularityIndex(nullable: false)
		parent(nullable: true)					 // this will be changed in TH-186
	}

	String toString() {
		"[${id}] ${name}"
	}

	static mapping = {
		table "user_group"
		tablePerHierarchy true
		discriminator column: [name: "discriminator", length: 24]
		description type:"text"
		about type:"text"
	}

	static transients = ["discriminatorType"]


	//---------------------

	protected void setDiscriminatorType(UserGroupDiscriminatorType discriminatorType) {
		this.discriminatorType = discriminatorType
	}

	UserGroupDiscriminatorType getDiscriminatorType() {
		this.discriminatorType
	}
}
