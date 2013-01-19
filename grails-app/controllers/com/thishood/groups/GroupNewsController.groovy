package com.thishood.groups

import com.thishood.UserGroupService;
import com.thishood.domain.ConvertUtils;
import com.thishood.domain.UserGroup;

import grails.converters.*
import java.util.Map;

import com.thishood.domain.GroupNews;

import grails.plugins.springsecurity.Secured;

@Secured(['isAuthenticated()'])
class GroupNewsController {
	def userGroupService
	def groupNewsService
	
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		return [groupId: params.id]
	}

	def create = {
		def groupId
		if (params.groupId > 0) {
			groupId = ConvertUtils.toLong(params.groupId)
		}
		def groupNews = new GroupNews()
		groupNews.properties = params
		return [groupNews: groupNews, groupId: groupId]
	}

	def save = {
		def groupNews= new GroupNews(params)
		groupNews.dateCreated = new Date()
		if (groupNews.save(flush: true)) {
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'groupNews.label', default: 'GroupNews'), groupNews.id])}"
			redirect(action: "show", id: groupNews.id)
		} else {
			render(view: "create", model: [groupNews: groupNews])
		}
	}

	def show = {
		def groupNews = GroupNews.get(params.id)
		if (!groupNews) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'groupNews.label', default: 'GroupNews'), params.id])}"
			redirect(action: "list")
		} else {
			[groupNews: groupNews]
		}
	}

	def edit = {
		def groupId
		if (params.groupId > 0) {
			groupId = ConvertUtils.toLong(params.groupId)
		}
		def groupNews = GroupNews.get(params.id)
		if (!groupNews) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'groupNews.label', default: 'GroupNews'), params.id])}"
			redirect(action: "list")
		} else {
			return [groupNews: groupNews, groupId: groupId]
		}
	}

	def update = {
		def groupNews = GroupNews.get(params.id)
		if (groupNews) {
			if (params.version) {
				def version = params.version.toLong()
				if (groupNews.version > version) {
					
					groupNews.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'groupNews.label', default: 'GroupNews')] as Object[], "Another user has updated this GroupNews while you were editing")
					render(view: "edit", model: [groupNews: groupNews])
					return
				}
			}
			
			groupNews.properties = params
			if (!groupNews.hasErrors() && groupNews.save(flush: true)) {
				
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'groupNews.label', default: 'GroupNews'), groupNews.id])}"
				redirect(action: "show", id: groupNews.id)
			} else {
				render(view: "edit", model: [groupNews: groupNews])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'groupNews.label', default: 'GroupNews'), params.id])}"
			redirect(action: "list")
		}
	}

	def delete = {
		def groupNews = GroupNews.get(params.id)
		if (groupNews) {
			try {
				groupNews.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'groupNews.label', default: 'GroupNews'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'groupNews.label', default: 'GroupNews'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'groupNews.label', default: 'GroupNews'), params.id])}"
			redirect(action: "list")
		}
	}

	def getData = {
		def groupId
		def group
		if (params.groupId > 0) {
			groupId = ConvertUtils.toLong(params.groupId)
			group = userGroupService.getById(groupId)
		}
		
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
				if (groupId) {
					dataRows = getDataRows(params, GroupNews.class, [ max: rows, offset: offset ], groupId)
				} else {
				dataRows = getDataRows(params, GroupNews.class, [ max: rows, offset: offset ])
				}
			} else {
				if (groupId) { 
					dataRows = getDataRows(params, GroupNews.class, [ max: rows, offset: offset, sort: params.sidx, order: params.sord == "dsc" ? "desc" : "asc" ], groupId)
				} else {
					dataRows = getDataRows(params, GroupNews.class, [ max: rows, offset: offset, sort: params.sidx, order: params.sord == "dsc" ? "desc" : "asc" ])
				}
			}
		} else if (params.sidx == "") {
			if (groupId) {
				dataRows = GroupNews.findAllByUserGroup(group, [max: rows, offset: offset])
			} else {
				dataRows = GroupNews.list(max: rows, offset: offset)
			}
		} else {
			if (groupId) {
				dataRows = GroupNews.findAllByUserGroup(group, [max: rows, offset: offset, sort: params.sidx, order: params.sord == "dsc" ? "desc" : "asc"])
			} else {
			dataRows = GroupNews.list(max: rows, offset: offset, sort: params.sidx, order: params.sord == "dsc" ? "desc" : "asc")
			}
		}
		
		dataRows = dataRows.collect {
			[cell: [ it.id.toString(), it.title, it.content, it.userGroup.name, it.dateCreated ], id: it.id]
		}
		def size = GroupNews.list().size()
		def totalPage = (int) Math.ceil(((double) size) / ((double) rows))
		def jsonRows = [ page: "${params.page}", records: size, rows: dataRows, total: totalPage ]
		render jsonRows as JSON
	}

	def getDataRows(Map param, Class domain, Map offset, Long groupId) {
		switch (param.searchOper) {
			case "bw":
			return domain.findAll("from " + domain.name + " as d where d." + param.searchField + " like ? AND user_group_id = ?", [parseSearchParam(param), groupId], offset)
			break
			case "ew":
			return domain.findAll("from " + domain.name + " as d where d." + param.searchField + " like ? AND user_group_id = ?", [parseSearchParam(param), groupId], offset)
			break
			case "eq":
			return domain.findAll("from " + domain.name + " as d where d." + param.searchField + " = ? AND user_group_id = ?", [parseSearchParam(param), groupId], offset)
			break
			case "ne":
			return domain.findAll("from " + domain.name + " as d where d." + param.searchField + " != ? AND user_group_id = ?", [parseSearchParam(param), groupId], offset)
			break
			case "lt":
			return domain.findAll("from " + domain.name + " as d where d." + param.searchField + " < ? AND user_group_id = ?", [parseSearchParam(param), groupId], offset)
			break
			case "le":
			return domain.findAll("from " + domain.name + " as d where d." + param.searchField + " <= ? AND user_group_id = ?", [parseSearchParam(param), groupId], offset)
			break
			case "gt":
			return domain.findAll("from " + domain.name + " as d where d." + param.searchField + " > ? AND user_group_id = ?", [parseSearchParam(param), groupId], offset)
			break
			case "ge":
			return domain.findAll("from " + domain.name + " as d where d." + param.searchField + " >= ? AND user_group_id = ?", [parseSearchParam(param), groupId], offset)
			break
			case "cn":
			return domain.findAll("from " + domain.name + " as d where d." + param.searchField + " like ? AND user_group_id = ?", [parseSearchParam(param), groupId], offset)
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
			
			def groupNews = new GroupNews(params)
			if (!groupNews.save(flush: true)) {
				errors = groupNews.errors
			}
			
		} else if (params.oper == "edit") {
			def groupNews = GroupNews.get(params.id)
			if (groupNews) {
				if (params.version && groupNews.version > params.version.toLong()) {
					errors.errors = [[ message: "Another user has updated this GroupNews while you were editing" ]]
				} else {
					
					groupNews.properties = params
					if (!groupNews.save(flush: true)) {
						errors = groupNews.errors
					}
					
				}
			} else {
				errors.errors = [[ message: "${message(code: 'default.not.found.message', args: [message(code: 'groupNews.label', default: 'GroupNews'), params.id])}" ]]
			}
		} else if (params.oper == "del") {
			def groupNews = GroupNews.findById(params.id)
			if (groupNews) {
				try {
					groupNews.delete(flush: true)
				}
				catch (org.springframework.dao.DataIntegrityViolationException e) {
					errors.errors = [[ message: "${message(code: 'default.not.deleted.message', args: [message(code: 'groupNews.label', default: 'GroupNews'), params.id])}" ]]
				}
			} else {
				errors.errors = [[ message: "${message(code: 'default.not.found.message', args: [message(code: 'groupNews.label', default: 'GroupNews'), params.id])}" ]]
			}
		}
		render errors as JSON
	}
	
	def latest = {
		[news : groupNewsService.getLatestNews(ConvertUtils.toLong(params.id))]
	}

}
