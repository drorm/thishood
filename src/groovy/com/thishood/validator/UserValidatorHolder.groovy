package com.thishood.validator

class UserValidatorHolder {
    static final passwordValidator = { String password, command ->
        if (command.email && command.email.equals(password)) {
            return 'command.password.error.email'
        }

        if (!password || password.length() < 6 || password.length() > 64 ){
            return 'command.password.error.strength'
        }
    }

    static final password2Validator = { value, command ->
        if (command.password != command.password2) {
            return 'command.password2.error.mismatch'
        }
    }
}
