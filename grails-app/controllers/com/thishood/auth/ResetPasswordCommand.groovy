package com.thishood.auth

import com.thishood.validator.UserValidatorHolder

class ResetPasswordCommand {
    String email
	String password
	String password2

	static constraints = {
		password validator: UserValidatorHolder.passwordValidator
		password2 validator: UserValidatorHolder.password2Validator
	}
}
