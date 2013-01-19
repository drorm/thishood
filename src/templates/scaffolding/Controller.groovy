<%=packageName ? "package ${packageName}\n\n" : ''%>

import grails.converters.*
import java.util.Map;
<% if (className == "User") { %>
	import org.codehaus.groovy.grails.plugins.springsecurity.NullSaltSource
<% } %>

class ${className}Controller {

	<% if (className == "User" || className == "SecRequestmap") { %>
		def springSecurityService
	<% } %>
	<% if (className == "User") { %>
		def saltSource
	<% } %>

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
//		params.max = Math.min(params.max ? params.int('max') : 10, 100)
//		[${propertyName}List: ${className}.list(params), ${propertyName}Total: ${className}.count()]
	}

	def create = {
		def ${propertyName} = new ${className}()
		${propertyName}.properties = params
		return [${propertyName}: ${propertyName}]
	}

	def save = {
		<% if (className == "User") { %>
			if (params.password) {
				// Encrypt password
				String salt = saltSource instanceof NullSaltSource ? null : params.username
				params.password = springSecurityService.encodePassword(params.password, salt)
			}
		<% } %>
		def ${propertyName} = new ${className}(params)
		if (${propertyName}.save(flush: true)) {
			<% if (className == "SecRequestmap") { %>
				// reload the cache from the db with the new rule
				springSecurityService.clearCachedRequestmaps()
			<% } %>
			flash.message = "\${message(code: 'default.created.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), ${propertyName}.id])}"
			redirect(action: "show", id: ${propertyName}.id)
		} else {
			render(view: "create", model: [${propertyName}: ${propertyName}])
		}
	}

	def show = {
		def ${propertyName} = ${className}.get(params.id)
		if (!${propertyName}) {
			flash.message = "\${message(code: 'default.not.found.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])}"
			redirect(action: "list")
		}
		else {
			[${propertyName}: ${propertyName}]
		}
	}

	def edit = {
		def ${propertyName} = ${className}.get(params.id)
		if (!${propertyName}) {
			flash.message = "\${message(code: 'default.not.found.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [${propertyName}: ${propertyName}]
		}
	}

	def update = {
		def ${propertyName} = ${className}.get(params.id)
		if (${propertyName}) {
			if (params.version) {
				def version = params.version.toLong()
				if (${propertyName}.version > version) {
					<% def lowerCaseName = grails.util.GrailsNameUtils.getPropertyName(className) %>
					${propertyName}.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: '${domainClass.propertyName}.label', default: '${className}')] as Object[], "Another user has updated this ${className} while you were editing")
					render(view: "edit", model: [${propertyName}: ${propertyName}])
					return
				}
			}
			<% if (className == "User") { %>
				if (${propertyName}.properties.password != params.password) {
					// Encrypt password
					String salt = saltSource instanceof NullSaltSource ? null : params.username
					params.password = springSecurityService.encodePassword(params.password, salt)
				}
			<% } %>
			${propertyName}.properties = params
			if (!${propertyName}.hasErrors() && ${propertyName}.save(flush: true)) {
				<% if (className == "SecRequestmap") { %>
					// reload the cache from the db with the new rule
					springSecurityService.clearCachedRequestmaps()
				<% } %>
				flash.message = "\${message(code: 'default.updated.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), ${propertyName}.id])}"
				redirect(action: "show", id: ${propertyName}.id)
			} else {
				render(view: "edit", model: [${propertyName}: ${propertyName}])
			}
		}
		else {
			flash.message = "\${message(code: 'default.not.found.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])}"
			redirect(action: "list")
		}
	}

	def delete = {
		def ${propertyName} = ${className}.get(params.id)
		if (${propertyName}) {
			try {
				${propertyName}.delete(flush: true)
				flash.message = "\${message(code: 'default.deleted.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "\${message(code: 'default.not.deleted.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "\${message(code: 'default.not.found.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])}"
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
				dataRows = getDataRows(params, ${className}.class, [ max: rows, offset: offset ])
			} else {
				dataRows = getDataRows(params, ${className}.class, [ max: rows, offset: offset, sort: params.sidx, order: params.sord == "dsc" ? "desc" : "asc" ])
			}
		} else if (params.sidx == "") {
			dataRows = ${className}.list(max: rows, offset: offset)
		} else {
			dataRows = ${className}.list(max: rows, offset: offset, sort: params.sidx, order: params.sord == "dsc" ? "desc" : "asc")
		}
		<%
		excludedProps = ['version'] << 'id' << 'dateCreated' << 'lastUpdated'
		persistentPropNames = domainClass.persistentProperties*.name
		props = domainClass.properties.findAll { persistentPropNames.contains(it.name) && !excludedProps.contains(it.name) }
		Collections.sort(props, comparator.constructors[0].newInstance([domainClass] as Object[]))
		props = props.collect { "it." + it.name }
		cells = props.join(", ")
		%>
		dataRows = dataRows.collect {
			[cell: [ it.id.toString(), ${cells} ], id: it.id]
		}
		def size = ${className}.list().size()
		def totalPage = (int) Math.ceil(((double) size) / ((double) rows))
		def jsonRows = [ page: "\${params.page}", records: size, rows: dataRows, total: totalPage ]
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
			<% if (className == "User") { %>
				if (params.password) {
					// Encrypt password
					String salt = saltSource instanceof NullSaltSource ? null : params.username
					params.password = springSecurityService.encodePassword(params.password, salt)
				}
			<% } %>
			def ${propertyName} = new ${className}(params)
			if (!${propertyName}.save(flush: true)) {
				errors = ${propertyName}.errors
			}
			<% if (className == "SecRequestmap") { %>
				else {
					// reload the cache from the db with the new rule
					springSecurityService.clearCachedRequestmaps()
				}
			<% } %>
		} else if (params.oper == "edit") {
			def ${propertyName} = ${className}.get(params.id)
			if (${propertyName}) {
				if (params.version && ${propertyName}.version > params.version.toLong()) {
					errors.errors = [[ message: "Another user has updated this ${className} while you were editing" ]]
				} else {
					<% if (className == "User") { %>
						if (params.password) {
							// Encrypt password
							String salt = saltSource instanceof NullSaltSource ? null : params.username
							params.password = springSecurityService.encodePassword(params.password, salt)
						}
					<% } %>
					${propertyName}.properties = params
					if (!${propertyName}.save(flush: true)) {
						errors = ${propertyName}.errors
					}
					<% if (className == "SecRequestmap") { %>
						else {
							// reload the cache from the db with the new rule
							springSecurityService.clearCachedRequestmaps()
						}
					<% } %>
				}
			} else {
				errors.errors = [[ message: "\${message(code: 'default.not.found.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])}" ]]
			}
		} else if (params.oper == "del") {
			def ${propertyName} = ${className}.findById(params.id)
			if (${propertyName}) {
				try {
					${propertyName}.delete(flush: true)
				}
				catch (org.springframework.dao.DataIntegrityViolationException e) {
					errors.errors = [[ message: "\${message(code: 'default.not.deleted.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])}" ]]
				}
			} else {
				errors.errors = [[ message: "\${message(code: 'default.not.found.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])}" ]]
			}
		}
		render errors as JSON
	}

}
