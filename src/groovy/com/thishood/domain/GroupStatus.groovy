package com.thishood.domain

/**
 * Defines statuses workflow for group
 * @see UserGroup
 */
enum GroupStatus {
	/**
	 * Created by user but not-accessible yet for posting/comments
	 * ThisHood admin have to approve it
	 */
	PENDING,
	/**
	 * Group is not accepted by ThisHood admin.
	 * This is a temporary status -- such groups periodically will be removed
	 */
	REJECTED,
	/**
	 * Fully operational group. Posts/comments/etc operations are possible.
	 */
	ACTIVE,
	/**
	 * Group is closed (aka archived). No any posts/comments are possible
	 */
	ARCHIVED


}
