package com.thishood

import com.thishood.domain.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.AddressException
import groovy.text.GStringTemplateEngine
import groovy.text.SimpleTemplateEngine

/**
* @see GroupService
 * @see UserGroupService
 */
class HoodService extends AbstractHolderableService{

    static transactional = true

	def grailsApplication
	def queueMailerService
	def helperService
	def persistableTemplateService

	List<Hood> findAllActive() {
		Hood.findAll("from Hood h where h.status = :status order by h.name",[status:GroupStatus.ACTIVE])
	}

	List<Hood> findAllByUser(Long userId) {
		Hood.findAll("from Hood h, Membership m where m.userGroup = h AND m.user.id = :userId order by h.name",[userId: userId])
	}

	/**
	* @see GroupService#create(java.lang.Object)
	 */
	Hood create(params) {
		User user = User.getOrFail(params.userId)

		def calculable = "${params.number} ${params.street}, ${params.city}"

		Hood hood = new Hood(
				name: calculable,
				description: calculable,
				about: params.about,
				status: GroupStatus.PENDING,
				moderationType: GroupModerationType.NONE,
				joinAccessLevel: GroupJoinAccessLevel.RESTRICTED,
				privacyLevel: GroupPrivacyLevel.OPEN,
				//--hood specific
				street: params.street,
				number: params.number,
				city: params.city,
				state: params.state,
				country: params.country
		)
		if (hood.validate()) {
			hood = hood.save(failOnError: true, flush: true)

			userGroupService.onCreate(hood, user, params)
		}

		hood
	}

	/**
	* @see GroupService#delete(long, long)
	 */
	void delete(Long hoodId, Long userId) {
		Hood hood = Hood.getOrFail(hoodId)
		User user = User.getOrFail(userId)

		userGroupService.onDelete(hood, user)
	}

	/**
	* @see GroupService#update(java.lang.Object)
	 */
	Hood update(params) {
		Hood hood = Hood.getOrFail(params.id)
		User user = User.getOrFail(params.userId)

		if (!userGroupService.hasAction(hood.id, user.id, ContentAction.Group.EDIT)) throw new DataAccessDeniedException("User [${user}] is not allowed to update [${hood}]")

		hood.version = params.version
		hood.description = params.description
		hood.about = params.about
		hood.moderationType = GroupModerationType.NONE
		hood.joinAccessLevel = GroupJoinAccessLevel.RESTRICTED
		hood.privacyLevel = GroupPrivacyLevel.OPEN

		if (hood.validate()) {
			hood = hood.save(flush:true, failOnError: true)

			userGroupService.onUpdate(hood, user, params)
		}

		hood
	}

	void inviteToHood(Long userId, Long hoodId, String emails, String contentTemplate) {
		User user = User.getOrFail(userId)
		Hood hood = Hood.getOrFail(hoodId)


		PersistableTemplate persistableTemplate = persistableTemplateService.findByUserGroup(hood.id)
		if (!persistableTemplate) throw new IllegalArgumentException("Can't find persistable template for group [${hood}]")
		persistableTemplate.content = contentTemplate
		persistableTemplate.save(failOnError:true, flush:true)

		//todo recheck authority

		def emailList = emails.split("\n").join(",")

		def errors = []

		List<InternetAddress> internetAddresses
		try {
			internetAddresses = InternetAddress.parse(emailList, false)
		} catch (AddressException e) {
			log.error("Unable to parse [${emails}]", e)
			throw new IllegalArgumentException("Can't parse emails", e)
		}

		internetAddresses.each {InternetAddress internetAddress ->
			//todo add also users who sent invite but not signup yet? (VerificationCode)
			try {
				internetAddress.validate()
				User recipient = userService.findByEmail(internetAddress.address)
				if (recipient) {
					errors.add("Email ${internetAddress.address} is already registered")
				}
			} catch (AddressException ignore) {
				errors.add("Unable to parse ${internetAddress.address}")
			}
		}

		if (errors.empty) {
			internetAddresses.each {InternetAddress internetAddress ->
				sendInvitationTo(user, internetAddress, hood, contentTemplate)
			}
		} else {
			throw new IllegalStateException(errors.join("\n"))
		}
	}

	private sendInvitationTo(User from, InternetAddress recipient, Hood hood, String contentTemplate) {
		log.info("Sending invitation on behalf of [${from}] to group [${hood}] to [${recipient}]")

		SignupInvitation signupInvitation = new SignupInvitation(
				status: SignupInvitationStatus.INVITED,
				personal: recipient.personal,
				email: recipient.address,
				hood: hood,
				invitedPersonally: true,
				dateCreated: new Date()
		).save(flush:true, failOnError:true)

		def url = helperService.generateLink('register', ' ', [hood: hood.id, token: signupInvitation.token])

		def model = [
				config: grailsApplication.config,
				from: from,
				hood: hood,
				recipient: recipient.personal ?: "",
				greeting: recipient.personal ? """Dear ${recipient.personal},<br/>Hi neighbor!""" : """Hi neighbor!<br/>""",
				signupInvitation: signupInvitation,
				url: url
		]


		def engine = new SimpleTemplateEngine()
		def template = engine.createTemplate(contentTemplate)
		def writable = template.make(model)

		//TH-305 Automatic line breaking in email invitation
		def content = writable.toString()
		model.content = content.replaceAll("\r\n|\r|\n","<BR/>\r\n")

		//def subject = "Invitation to ThisHood"
		def subject = from.first + " at " + from.address + " invites you to join ThisHood"

		queueMailerService.sendEmail(
				to: recipient.toUnicodeString(),
				subject: subject,
				model: model,
				view: "/template/email/invite/hood")
	}

}
