package com.thishood.domain

class Market extends UserGroup {

	Market() {
		discriminatorType = UserGroupDiscriminatorType.MARKET
	}

	static constraints = {
	}

	static mapping = {
		discriminator UserGroupDiscriminatorType.MARKET.name()
	}

}
