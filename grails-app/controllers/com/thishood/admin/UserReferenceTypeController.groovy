package com.thishood.admin

import com.thishood.domain.UserReferenceType
import grails.converters.*
import java.util.Map;
import grails.plugins.springsecurity.Secured

@Secured(["hasRole('ROLE_ADMIN')"])
class UserReferenceTypeController {

	
	

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
//		params.max = Math.min(params.max ? params.int('max') : 10, 100)
//		[userReferenceTypeInstanceList: UserReferenceType.list(params), userReferenceTypeInstanceTotal: UserReferenceType.count()]
	}

	def create = {
		def userReferenceTypeInstance = new UserReferenceType()
		userReferenceTypeInstance.properties = params
		return [userReferenceTypeInstance: userReferenceTypeInstance]
	}

	def save = {
		
		def userReferenceTypeInstance = new UserReferenceType(params)
		if (userReferenceTypeInstance.save(flush: true)) {
			
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'userReferenceType.label', default: 'UserReferenceType'), userReferenceTypeInstance.id])}"
			redirect(action: "show", id: userReferenceTypeInstance.id)
		} else {
			render(view: "create", model: [userReferenceTypeInstance: userReferenceTypeInstance])
		}
	}

	def show = {
		def userReferenceTypeInstance = UserReferenceType.get(params.id)
		if (!userReferenceTypeInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'userReferenceType.label', default: 'UserReferenceType'), params.id])}"
			redirect(action: "list")
		}
		else {
			[userReferenceTypeInstance: userReferenceTypeInstance]
		}
	}

	def edit = {
		def userReferenceTypeInstance = UserReferenceType.get(params.id)
		if (!userReferenceTypeInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'userReferenceType.label', default: 'UserReferenceType'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [userReferenceTypeInstance: userReferenceTypeInstance]
		}
	}

	def update = {
		def userReferenceTypeInstance = UserReferenceType.get(params.id)
		if (userReferenceTypeInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (userReferenceTypeInstance.version > version) {
					
					userReferenceTypeInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'userReferenceType.label', default: 'UserReferenceType')] as Object[], "Another user has updated this UserReferenceType while you were editing")
					render(view: "edit", model: [userReferenceTypeInstance: userReferenceTypeInstance])
					return
				}
			}
			
			userReferenceTypeInstance.properties = params
			if (!userReferenceTypeInstance.hasErrors() && userReferenceTypeInstance.save(flush: true)) {
				
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'userReferenceType.label', default: 'UserReferenceType'), userReferenceTypeInstance.id])}"
				redirect(action: "show", id: userReferenceTypeInstance.id)
			} else {
				render(view: "edit", model: [userReferenceTypeInstance: userReferenceTypeInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'userReferenceType.label', default: 'UserReferenceType'), params.id])}"
			redirect(action: "list")
		}
	}

	def delete = {
		def userReferenceTypeInstance = UserReferenceType.get(params.id)
		if (userReferenceTypeInstance) {
			try {
				userReferenceTypeInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'userReferenceType.label', default: 'UserReferenceType'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'userReferenceType.label', default: 'UserReferenceType'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'userReferenceType.label', default: 'UserReferenceType'), params.id])}"
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
			offset = (page-1) * rows
		}
		def dataRows = null
		if (params._search == "true") {
			if (params.sidx == "") {
				dataRows = getDataRows(params, UserReferenceType.class, [ max: rows, offset: offset ])
			} else {
				dataRows = getDataRows(params, UserReferenceType.class, [ max: rows, offset: offset, sort: params.sidx, order: params.sord == "dsc" ? "desc" : "asc" ])
			}
		} else if (params.sidx == "") {
			dataRows = UserReferenceType.list(max: rows, offset: offset)
		} else {
			dataRows = UserReferenceType.list(max: rows, offset: offset, sort: params.sidx, order: params.sord == "dsc" ? "desc" : "asc")
		}
		
		dataRows = dataRows.collect {
			[cell: [ it.id.toString(), it.type ], id: it.id]
		}
		def size = UserReferenceType.list().size()
		def totalPage = (int) Math.ceil(((double) size) / ((double) rows))
		def jsonRows = [ page: "${params.page}", records: size, rows: dataRows, total: totalPage ]
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
			
			def userReferenceTypeInstance = new UserReferenceType(params)
			if (!userReferenceTypeInstance.save(flush: true)) {
				errors = userReferenceTypeInstance.errors
			}
			
		} else if (params.oper == "edit") {
			def userReferenceTypeInstance = UserReferenceType.get(params.id)
			if (userReferenceTypeInstance) {
				if (params.version && userReferenceTypeInstance.version > params.version.toLong()) {
					errors.errors = [[ message: "Another user has updated this UserReferenceType while you were editing" ]]
				} else {
					
					userReferenceTypeInstance.properties = params
					if (!userReferenceTypeInstance.save(flush: true)) {
						errors = userReferenceTypeInstance.errors
					}
					
				}
			} else {
				errors.errors = [[ message: "${message(code: 'default.not.found.message', args: [message(code: 'userReferenceType.label', default: 'UserReferenceType'), params.id])}" ]]
			}
		} else if (params.oper == "del") {
			def userReferenceTypeInstance = UserReferenceType.findById(params.id)
			if (userReferenceTypeInstance) {
				try {
					userReferenceTypeInstance.delete(flush: true)
				}
				catch (org.springframework.dao.DataIntegrityViolationException e) {
					errors.errors = [[ message: "${message(code: 'default.not.deleted.message', args: [message(code: 'userReferenceType.label', default: 'UserReferenceType'), params.id])}" ]]
				}
			} else {
				errors.errors = [[ message: "${message(code: 'default.not.found.message', args: [message(code: 'userReferenceType.label', default: 'UserReferenceType'), params.id])}" ]]
			}
		}
		render errors as JSON
	}

}
