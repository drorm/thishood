package com.thishood.domain

class ApplicationMetric {
	ApplicationMetricType type
	Date dateCreated
	Integer value

    static constraints = {
		value nullable: false
    }
}
