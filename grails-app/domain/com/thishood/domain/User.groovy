package com.thishood.domain

import javax.mail.internet.InternetAddress
import org.apache.commons.lang.StringUtils

class User {

    //Required by Spring Security
    String password
    boolean enabled
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired
    String salt

    String first
    String middle
    String last
    String email
    String address
    String city
    String state
    String zip
    String country
    Date dateCreated
    Date lastUpdated
	String aboutMe
	Long photoUploadId
	Boolean tourTaken


    static hasMany = [
            //memberships: Membership
    ]
    static hasOne = [
            notification: UserNotification,
            ping: UserPing
    ]

    static constraints = {
        password(blank: false, password: true)
        first(blank: false)
        middle(nullable: true)
        last(blank: false)
        email(blank: false, email: true, unique: true)
        salt(nullable: true)                            // nullable while we use NullSaltSource
        address(blank: false, maxSize: 100)
        city(blank: false)
        state(blank: false)
        zip(blank: false)
        country(blank: false)
        notification(nullable: true)
        ping(nullable: true)
        aboutMe(nullable: true, maxSize: 1000)
		photoUploadId(display: false, nullable: true)
		tourTaken(display: false, blank: true)
    }

    static mapping = {
        //Required by Spring Security
        password column: '`password`'
		photoImageId column: 'photo'
    }

    String toString() {
        "[${id}] ${displayName}"
    }

    //Generated by Spring Security

    Set<SecRole> getRoles() {
        SecUserSecRole.findAllByUser(this).collect { it.secRole } as Set
    }

	boolean hasRole(UserRole role) {
		for(SecRole secRole : getRoles() ){
			if (secRole.authority == role.name()) return true;
		}
		false
	}


    static transients = ["emailWithName", "displayName", "joinedMarket", "isAdmin", "profile", "uservoiceSSOToken"]

    String getEmailWithName() {
		new InternetAddress(email, displayName).toString()
        //"${displayName} <${email}>"
    }

    String getDisplayName() {
        StringUtils.isBlank(middle) ? "${first} ${last}" : "${first} ${middle} ${last}"
    }

	def userService

	/**
	def getJoinedMarket() {
		userService.joinedMarket(this)
	}
    */
}
