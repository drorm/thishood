package com.thishood.domain

class CommunityMetric {
	UserGroup community
	CommunityMetricType type
	Date dateCreated
	Integer value

    static constraints = {
		community nullable: false
		value nullable: false
    }
}
