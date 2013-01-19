package com.thishood

import com.thishood.domain.Prospect
import com.thishood.domain.User
import com.thishood.domain.UserRole

class ProspectService {

	static transactional = true

	def queueMailerService
	def userService
	def grailsApplication


	List<Prospect> findAllSince(Date date) {
		Prospect.executeQuery("from Prospect p  where p.dateCreated >= :date", [date: date])
	}

	void sendNotifications() {
		Date date = new Date() - 1
		List<Prospect> prospects = findAllSince(date)

		if (prospects.size() > 0) {
			List<User> admins = userService.findAllActiveByRole(UserRole.ROLE_ADMIN)
			queueMailerService.sendEmail(
					to: admins.collectAll {it.emailWithName},
					subject: "New prospects from " + date.format("MMM d 'at' HH:mm"),
					model: [
							config: grailsApplication.config,
							prospects: prospects
					],
					view: "/template/email/signup/newProspects")
		}
	}
}
