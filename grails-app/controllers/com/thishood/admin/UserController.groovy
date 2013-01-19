package com.thishood.admin

import com.thishood.domain.User
import grails.converters.JSON
import grails.plugins.springsecurity.Secured
import org.codehaus.groovy.grails.plugins.springsecurity.NullSaltSource

@Secured(["hasRole('ROLE_ADMIN')"])
class UserController {

    def userService
	def userGroupService
    def springSecurityService
    def saltSource

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
//		params.max = Math.min(params.max ? params.int('max') : 10, 100)
//		[userInstanceList: User.list(params), userInstanceTotal: User.count()]
    }

    def create = {
        def userInstance = new User()
        userInstance.properties = params

        flash.message = "It's NOT recommended to create in such way a new user. Do it via 'register' feature"

        return [userInstance: userInstance]
    }

    def save = {

        def user = new User(params)
        if (user.save(flush: true) && userService.updatePassword(user.email, params.password)) {

            //flash.message = "${message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), user.id])}"
            redirect(action: "show", id: user.id)
        } else {
            render(view: "create", model: [userInstance: user])
        }
    }

    def show = {
        def userInstance = User.get(params.id)
        if (!userInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            redirect(action: "list")
        }
        else {
            [userInstance: userInstance]
        }
    }

    def edit = {
        def userInstance = User.get(params.id)

        if (!userInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [userInstance: userInstance]
        }
    }

    def update = {
        def userInstance = User.get(params.id)
        if (userInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (userInstance.version > version) {

                    userInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'user.label', default: 'User')] as Object[], "Another user has updated this User while you were editing")
                    render(view: "edit", model: [userInstance: userInstance])
                    return
                }
            }

            def needToUpdatePassword = userInstance.properties.password != params.password

            userInstance.properties = params
            if (!userInstance.hasErrors() && userInstance.save(flush: true)) {
                if (needToUpdatePassword) userService.updatePassword(userInstance.email, params.password)

//				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])}"
                redirect(action: "show", id: userInstance.id)
            } else {
                render(view: "edit", model: [userInstance: userInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def userInstance = User.get(params.id)
        if (userInstance) {
            try {
                userInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
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
                dataRows = getDataRows(params, User.class, [max: rows, offset: offset])
            } else {
                dataRows = getDataRows(params, User.class, [max: rows, offset: offset, sort: params.sidx, order: params.sord == "dsc" ? "desc" : "asc"])
            }
        } else if (params.sidx == "") {
            dataRows = User.list(max: rows, offset: offset)
        } else {
            dataRows = User.list(max: rows, offset: offset, sort: params.sidx, order: params.sord == "dsc" ? "desc" : "asc")
        }

        dataRows = dataRows.collect {
            [cell: [it.id.toString(), it.first, it.middle, it.last, it.email, it.address, it.city, it.state, it.zip, it.country, it.accountExpired, it.accountLocked, it.enabled, it.passwordExpired], id: it.id]
        }
        def size = User.list().size()
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

            if (params.password) {
                // Encrypt password
                String salt = saltSource instanceof NullSaltSource ? null : params.email
                params.password = springSecurityService.encodePassword(params.password, salt)
            }

            def userInstance = new User(params)
            if (!userInstance.save(flush: true)) {
                errors = userInstance.errors
            }

        } else if (params.oper == "edit") {
            def userInstance = User.get(params.id)
            if (userInstance) {
                if (params.version && userInstance.version > params.version.toLong()) {
                    errors.errors = [[message: "Another user has updated this User while you were editing"]]
                } else {

                    if (params.password) {
                        // Encrypt password
                        String salt = saltSource instanceof NullSaltSource ? null : params.email
                        params.password = springSecurityService.encodePassword(params.password, salt)
                    }

                    userInstance.properties = params
                    if (!userInstance.save(flush: true)) {
                        errors = userInstance.errors
                    }

                }
            } else {
                errors.errors = [[message: "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"]]
            }
        } else if (params.oper == "del") {
            def userInstance = User.findById(params.id)
            if (userInstance) {
                try {
                    userInstance.delete(flush: true)
                }
                catch (org.springframework.dao.DataIntegrityViolationException e) {
                    errors.errors = [[message: "${message(code: 'default.not.deleted.message', args: [message(code: 'user.label', default: 'User'), params.id])}"]]
                }
            } else {
                errors.errors = [[message: "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"]]
            }
        }
        render errors as JSON
    }
		
		@Secured(['isAuthenticated()'])
		def initialWizard = {
			def groups = userGroupService.findUserGroupsNotSubscribed(springSecurityService.currentUser.id)
			def user = springSecurityService.currentUser
			
			return [groups: groups, user: user]
		}
		
    @Secured(['isAuthenticated()'])
    def setAboutMe = {
    	def user = springSecurityService.currentUser
    	user.aboutMe = params.aboutMe
    	if (!user.hasErrors() && user.save(flush: true)) {
    		flash.message = "${message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), user.id])}"
    			render user as JSON
    	} else {
    		render errors as JSON
    	}
    }
    
		@Secured(['isAuthenticated()'])
		def tourTaken = {
			def user = springSecurityService.currentUser
			user.tourTaken = true
			if (!user.hasErrors() && user.save(flush: true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), user.id])}"
				render user as JSON
			} else {
				render errors as JSON
			}
		}
	
}
