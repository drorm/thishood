package com.thishood.domain

/**
 * Domain for storing users who would like to join ThisHood (signup for registration invite)
 */
class RegistrationInvite {
    String first
    String last
    String email
    String lat
    String lon

    //unique confirmation hash which will be sent via email
    String hash

    // invite will be blocked in case if more than N days not confirmed
    Date createdAt
    Date confirmedAt


    static constraints = {
        email(nullable: false, email: true)
        confirmedAt(nullable: true)
    }
}
