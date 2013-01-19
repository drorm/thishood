package com.thishood.domain

/**
 * Note: on this stage we use annotations and this domain has no sense yet
 */
class SecRequestmap {

	String url
	String configAttribute

	static mapping = {
		cache true
	}

	static constraints = {
		url blank: false, unique: true
		configAttribute blank: false
	}
}
