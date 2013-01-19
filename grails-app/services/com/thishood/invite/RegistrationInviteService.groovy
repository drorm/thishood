package com.thishood.invite

import com.thishood.utils.ClassUtils

import org.apache.commons.lang.RandomStringUtils
import com.thishood.domain.RegistrationInvite
import com.thishood.registration.RegistrationInviteCommand

class RegistrationInviteService {

    static transactional = true

    /**
     * Checks if such email already registered in ThisHood
     * @return true
     */
    def isNewEmail(String email) {
        //todo vitaliy@19.02.11 add check also if email already used by registered user
        RegistrationInvite.findByEmail(email) == null
    }

    def save(RegistrationInviteCommand command) {
        RegistrationInvite entity = new RegistrationInvite()

        ClassUtils.copyProperties(command, entity)
        entity.createdAt = new Date()
        entity.hash = RandomStringUtils.randomAlphanumeric(32)

        if (!entity.save(flush:true)) {
            throw new RuntimeException("can't save" + entity.errors)
        }
        entity
    }

}
