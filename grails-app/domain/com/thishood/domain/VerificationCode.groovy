package com.thishood.domain

import com.thishood.UUIDGenerator

/**
 * Entity which holds tokens for users who
 * - registered and should confirm email
 * - forgot their password
 * It should be cleaned periodically by Quartz
 * Replaced original implementation {@link org.codehaus.groovy.grails.plugins.springsecurity.ui.RegistrationCode}
 */
class VerificationCode {

    String email
    String token = UUIDGenerator.next()
    Date dateCreated

    static mapping = {
        version false
    }
}
