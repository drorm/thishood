package com.thishood

import com.thishood.domain.MembershipVerification
import com.thishood.domain.User
import com.thishood.domain.UserGroup
import com.thishood.domain.MembershipVerificationStatus

class MembershipVerificationService {

	static transactional = true

	MembershipVerification findByCode(String code) {
		MembershipVerification.findByCode(code)
	}

	MembershipVerification findByUserAndGroup(Long userId, Long groupId) {
		User user = User.getOrFail(userId)
		UserGroup group = UserGroup.getOrFail(groupId)

		MembershipVerification.find("from MembershipVerification mv where mv.user = :user and mv.group = :group", [user: user, group: group])
	}

	MembershipVerification requestJoin(Long userId, Long groupId, String description) {
		User user = User.getOrFail(userId)
		UserGroup group = UserGroup.getOrFail(groupId)

		MembershipVerification membershipVerification = findByUserAndGroup(userId, groupId)
		if (membershipVerification?.status == MembershipVerificationStatus.PENDING) throw new IllegalArgumentException("User [${user}] is already requested to join group [${group}]")

		membershipVerification = new MembershipVerification(
				status: MembershipVerificationStatus.PENDING,
				user: user,
				group: group,
				code: UUID.randomUUID().toString(),
				description: description
		)

		membershipVerification.save(failOnError: true, flush: true)
	}

	void changeStatus(Long membershipVerificationId, MembershipVerificationStatus status) {
		MembershipVerification membershipVerification = MembershipVerification.getOrFail(membershipVerificationId)

		if (membershipVerification.status != MembershipVerificationStatus.PENDING) throw new IllegalArgumentException("Can't change status of [${membershipVerification}] because it's not equal to ${MembershipVerificationStatus.PENDING}")

		membershipVerification.status = status

		membershipVerification.save(failOnError: true, flush:true)
	}
}
