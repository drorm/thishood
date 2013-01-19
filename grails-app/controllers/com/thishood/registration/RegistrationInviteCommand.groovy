package com.thishood.registration

/**
 * @see com.thishood.domain.RegistrationInvite
 */
class RegistrationInviteCommand {
    def registrationInviteService

    String first
    String last
    String email
    String lat
    String lon
    //todo vitaliy@19.02.11 add captch support

    static constraints = {
        first(blank: false, minSize: 2, maxSize: 24)
        last(blank: false, minSize: 2, maxSize: 20)
        email(blank: false, email: true, validator: { value, obj ->
            obj.registrationInviteService.isNewEmail(value) ? true : ["invite.email.alreadyRegistered"]
        })
        lat(nullable: false)
        lon(nullable: false)
    }
}
