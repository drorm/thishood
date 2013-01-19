package com.thishood.domain

import org.apache.commons.lang.builder.HashCodeBuilder
import com.thishood.domain.User

class SecUserSecRole implements Serializable {

	User user
	SecRole secRole

	boolean equals(other) {
		if (!(other instanceof SecUserSecRole)) {
			return false
		}

		other.user?.id == user?.id &&
			other.secRole?.id == secRole?.id
	}

	int hashCode() {
		def builder = new HashCodeBuilder()
		if (user) builder.append(user.id)
		if (secRole) builder.append(secRole.id)
		builder.toHashCode()
	}

	static SecUserSecRole get(long userId, long secRoleId) {
		find 'from SecUserSecRole where user.id=:userId and secRole.id=:secRoleId',
			[userId: userId, secRoleId: secRoleId]
	}

	static SecUserSecRole create(User user, SecRole secRole, boolean flush = false) {
		new SecUserSecRole(user: user, secRole: secRole).save(flush: flush, insert: true)
	}

	static boolean remove(User user, SecRole secRole, boolean flush = false) {
		SecUserSecRole instance = SecUserSecRole.findBySecUserAndSecRole(user, secRole)
		instance ? instance.delete(flush: flush) : false
	}

	static void removeAll(User user) {
		executeUpdate 'DELETE FROM SecUserSecRole WHERE user=:user', [user: user]
	}

	static void removeAll(SecRole secRole) {
		executeUpdate 'DELETE FROM SecUserSecRole WHERE secRole=:secRole', [secRole: secRole]
	}

	static mapping = {
		id composite: ['secRole', 'user']
		version false
	}
}
