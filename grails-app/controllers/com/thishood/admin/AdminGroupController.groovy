package com.thishood.admin

import grails.plugins.springsecurity.Secured
import com.thishood.domain.ConvertUtils
import grails.converters.JSON
import com.thishood.domain.UserGroup

@Secured(["hasRole('ROLE_ADMIN')"])
class AdminGroupController {
	def springSecurityService
	def userGroupService
	def groupNewsService

	def index = {
		redirect(action: "list", params: params)
	}


	def createApprove= {
		def user = springSecurityService.currentUser
		def groupId = ConvertUtils.toLong(params.group)
		try {
			userGroupService.createApprove(user.id, groupId)
			flash.message = 'Group approved'
		} catch(any) {
			log.error("Unknown error on approving group [${groupId}] by user [${user}]",any)
			flash.error = "Error occured on group approval: ${any}"
		}
		render view: "/loggedin-info"
	}

	def createReject = {
		def user = springSecurityService.currentUser
		def groupId = ConvertUtils.toLong(params.group)
		try {
			userGroupService.createReject(user.id, ConvertUtils.toLong(params.group))
			flash.message = 'Group rejected'
		} catch(any) {
			log.error("Unknown error on approving group [${groupId}] by user [${user}]",any)
			flash.error = "Error occured on group reject: ${any}"
		}
		render view: "/loggedin-info"
	}

	// -------------------------------------------------------------------------------

	def list = {
	}


	def create = {
		[userGroupInstance: userGroupService.create(params)]
	}

	def save = {
		def userGroupInstance = userGroupService.save(params)
		if (userGroupInstance != null) {
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), userGroupInstance.id])}"
			redirect(action: "show", id: userGroupInstance.id)
		} else {
			render(view: "create", model: [userGroupInstance: userGroupInstance])
		}
	}


	def show = {
		def userGroupInstance = userGroupService.getById(ConvertUtils.toLong(params.id))
		if (!userGroupInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"
			redirect(action: "list")
		}
		else {
			[userGroupInstance: userGroupInstance]
		}
	}


	def edit = {
		def userGroupInstance = userGroupService.getById(ConvertUtils.toLong(params.id))
		if (!userGroupInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [userGroupInstance: userGroupInstance]
		}
	}

	def update = {
		def userGroupInstance = UserGroup.get(ConvertUtils.toLong(params.id))
		if (userGroupInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (userGroupInstance.version > version) {

					userGroupInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'userGroup.label', default: 'UserGroup')] as Object[], "Another user has updated this UserGroup while you were editing")
					render(view: "edit", model: [userGroupInstance: userGroupInstance])
					return
				}
			}

			def news = params.remove("news")

			userGroupInstance.properties = params
			if (!userGroupInstance.hasErrors() && userGroupInstance.save(flush: true)) {
				if (news) {
					groupNewsService.addGroupNews(userGroupInstance.id, news)
				}

				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), userGroupInstance.id])}"
				redirect(action: "show", id: userGroupInstance.id)
			} else {
				render(view: "edit", model: [userGroupInstance: userGroupInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"
			redirect(action: "list")
		}
	}

	def delete = {
		def userGroupInstance = UserGroup.get(params.id)
		if (userGroupInstance) {
			try {
				userGroupInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"
			redirect(action: "list")
		}
	}


	def getData = {
		String pageInString = params.page
		String rowsInString = params.rows
		int page = 0
		int rows = 0
		if (pageInString) {
			page = Integer.parseInt(pageInString)
		}
		if (rowsInString) {
			rows = Integer.parseInt(rowsInString)
		}
		if (page == null || page == 0) {
			page = 1
		}
		if (rows == null || rows == 0) {
			rows = 5
		}
		int offset = 0
		if (page > 1) {
			offset = (page - 1) * rows
		}
		def dataRows = null
		if (params._search == "true") {
			if (params.sidx == "") {
				dataRows = getDataRows(params, UserGroup.class, [max: rows, offset: offset])
			} else {
				dataRows = getDataRows(params, UserGroup.class, [max: rows, offset: offset, sort: params.sidx, order: params.sord == "dsc" ? "desc" : "asc"])
			}
		} else if (params.sidx == "") {
			dataRows = UserGroup.list(max: rows, offset: offset)
		} else {
			dataRows = UserGroup.list(max: rows, offset: offset, sort: params.sidx, order: params.sord == "dsc" ? "desc" : "asc")
		}

		dataRows = dataRows.collect {
			[cell: [it.id.toString(), it.name, it.description, it.parent, it.dateCreated, it.lastUpdated, it.moderationType.value, it.joinAccessLevel.value, it.privacyLevel.value], id: it.id]
		}
		def size = UserGroup.list().size()
		def totalPage = (int) Math.ceil(((double) size) / ((double) rows))
		def jsonRows = [page: "${params.page}", records: size, rows: dataRows, total: totalPage]
		render jsonRows as JSON
	}

	def getDataRows(Map param, Class domain, Map offset) {
		switch (param.searchOper) {
			case "bw":
				return domain.findAll("from " + domain.name + " as d where d." + param.searchField + " like ?", [parseSearchParam(param)], offset)
				break
			case "ew":
				return domain.findAll("from " + domain.name + " as d where d." + param.searchField + " like ?", [parseSearchParam(param)], offset)
				break
			case "eq":
				return domain.findAll("from " + domain.name + " as d where d." + param.searchField + " = ?", [parseSearchParam(param)], offset)
				break
			case "ne":
				return domain.findAll("from " + domain.name + " as d where d." + param.searchField + " != ?", [parseSearchParam(param)], offset)
				break
			case "lt":
				return domain.findAll("from " + domain.name + " as d where d." + param.searchField + " < ?", [parseSearchParam(param)], offset)
				break
			case "le":
				return domain.findAll("from " + domain.name + " as d where d." + param.searchField + " <= ?", [parseSearchParam(param)], offset)
				break
			case "gt":
				return domain.findAll("from " + domain.name + " as d where d." + param.searchField + " > ?", [parseSearchParam(param)], offset)
				break
			case "ge":
				return domain.findAll("from " + domain.name + " as d where d." + param.searchField + " >= ?", [parseSearchParam(param)], offset)
				break
			case "cn":
				return domain.findAll("from " + domain.name + " as d where d." + param.searchField + " like ?", [parseSearchParam(param)], offset)
				break
		}
	}

	def parseSearchParam(Map param) {
		switch (param.searchOper) {
			case "ew":
				return "%" + param.searchString
				break
			case "bw":
				return param.searchString + "%"
				break
			case "cn":
				return "%" + param.searchString + "%"
				break
			default:
				return param.searchString
		}
	}

	def setData = {
		def errors = [:]
		if (params.oper == "add") {

			def userGroupInstance = new UserGroup(params)
			if (!userGroupInstance.save(flush: true)) {
				errors = userGroupInstance.errors
			}

		} else if (params.oper == "edit") {
			def userGroupInstance = UserGroup.get(params.id)
			if (userGroupInstance) {
				if (params.version && userGroupInstance.version > params.version.toLong()) {
					errors.errors = [[message: "Another user has updated this UserGroup while you were editing"]]
				} else {

					userGroupInstance.properties = params
					if (!userGroupInstance.save(flush: true)) {
						errors = userGroupInstance.errors
					}

				}
			} else {
				errors.errors = [[message: "${message(code: 'default.not.found.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"]]
			}
		} else if (params.oper == "del") {
			def userGroupInstance = UserGroup.findById(params.id)
			if (userGroupInstance) {
				try {
					userGroupInstance.delete(flush: true)
				}
				catch (org.springframework.dao.DataIntegrityViolationException e) {
					errors.errors = [[message: "${message(code: 'default.not.deleted.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"]]
				}
			} else {
				errors.errors = [[message: "${message(code: 'default.not.found.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"]]
			}
		}
		render errors as JSON
	}

}
