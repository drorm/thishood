package com.thishood

import com.thishood.domain.GroupResource
import com.thishood.domain.UserGroupDiscriminatorType

class GroupResourceService {

	static transactional = true

	def findAllByUserGroup(group) {
		if (group.discriminatorType == UserGroupDiscriminatorType.HOOD) {
			GroupResource.findAllByUserGroupOrForAllBlocks(group, true, [sort:"editedTime", order:"desc"])
		} else {
			GroupResource.findAllByUserGroup(group, [sort:"editedTime", order:"desc"])
		}
	}

	def findAllTitlesByUserGroup(group) {
		findAllByUserGroup(group).unique{[it.title]}
	}

	def findAllByForAllBlocks() {
		GroupResource.findAllByForAllBlocks(true, [sort:"editedTime", order:"desc"])
	}

	def findAllTitlesByForAllBlocks() {
		findAllByForAllBlocks().unique{[it.title]}
	}

	def findAllByUserGroupAndTitle(group, title) {
		if (group.discriminatorType == UserGroupDiscriminatorType.HOOD) {
			def c = GroupResource.createCriteria()
			c.list {
				and {
					or {
						eq("userGroup", group)
						eq("forAllBlocks", true)
					}
					eq("title", title)
				}
				order("createdTime", "asc")
			}
		} else {
			GroupResource.findAllByUserGroupAndTitle(group, title, [sort:"createdTime", order:"asc"])
		}
	}

}