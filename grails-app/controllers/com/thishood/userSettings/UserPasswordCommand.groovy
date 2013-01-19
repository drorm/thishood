package com.thishood.userSettings

import com.thishood.validator.UserValidatorHolder

class UserPasswordCommand {
    def userService

    //needed for validator
    String email

    String password
    String password2

    static constraints = {
        password blank:false, validator: UserValidatorHolder.passwordValidator
        password2 blank:false, validator: UserValidatorHolder.password2Validator
    }
}