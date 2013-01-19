package com.thishood.registration

import com.thishood.validator.UserValidatorHolder

class RegisterCommand {
    def userService

    String first
    String middle
    String last
    String email
    String password
    String password2
    String address
    String city
    String state
    String zip
    String country
	//
	Integer hoodId
	String token

    static constraints = {
        email blank: false, email:true, validator: { value, command ->
            if (value) {
                if (command.userService.findByEmail(value))
                    return 'registerCommand.email.unique'
            }
        }
        password validator: UserValidatorHolder.passwordValidator
        password2 validator: UserValidatorHolder.password2Validator

        first (blank: false)
        middle (nullable: true)
        last (blank: false)
        address (blank: false)
        city (blank: false)
        state (blank: false)
        country blank: false
        zip(blank: false)
        country(blank: false)
		//
		hoodId nullable:true
		token nullable:true
    }
}

