package com.thishood.domain

/**
 * Defines statuses workflow for signup invitation
 * @see SignupInvitation
 */
enum SignupInvitationStatus {
	/**
	 * Instance with this status created when Block Lead invites neighbor
	 */
	INVITED,
	/**
	 * When user registered but not verified own email.
	 * Created {@link User} account is created but locked
	 */
	REGISTERED,
	/**
	 * User registered and verified email
	 * Created {@link User} account is ready for usage
	 */
	VERIFIED
}
