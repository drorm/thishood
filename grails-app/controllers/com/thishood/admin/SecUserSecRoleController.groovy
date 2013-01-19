package com.thishood.admin

import com.thishood.domain.SecUserSecRole
import grails.converters.JSON
import grails.plugins.springsecurity.Secured

@Secured(["hasRole('ROLE_ADMIN')"])
class SecUserSecRoleController {


    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
//		params.max = Math.min(params.max ? params.int('max') : 10, 100)
//		[secUserSecRoleInstanceList: SecUserSecRole.list(params), secUserSecRoleInstanceTotal: SecUserSecRole.count()]
    }

    def create = {
        def secUserSecRoleInstance = new SecUserSecRole()
        secUserSecRoleInstance.properties = params
        return [secUserSecRoleInstance: secUserSecRoleInstance]
    }

    def save = {

        def secUserSecRoleInstance = new SecUserSecRole(params)
        if (secUserSecRoleInstance.save(flush: true)) {

            flash.message = "${message(code: 'default.created.message', args: [message(code: 'secUserSecRole.label', default: 'SecUserSecRole'), secUserSecRoleInstance.id])}"
            redirect(action: "show", id: secUserSecRoleInstance.id)
        } else {
            render(view: "create", model: [secUserSecRoleInstance: secUserSecRoleInstance])
        }
    }

    def show = {
        def secUserSecRoleInstance = SecUserSecRole.get(params.id)
        if (!secUserSecRoleInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'secUserSecRole.label', default: 'SecUserSecRole'), params.id])}"
            redirect(action: "list")
        }
        else {
            [secUserSecRoleInstance: secUserSecRoleInstance]
        }
    }

    def edit = {
        def secUserSecRoleInstance = SecUserSecRole.get(params.id)
        if (!secUserSecRoleInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'secUserSecRole.label', default: 'SecUserSecRole'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [secUserSecRoleInstance: secUserSecRoleInstance]
        }
    }

    def update = {
        def secUserSecRoleInstance = SecUserSecRole.get(params.id)
        if (secUserSecRoleInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (secUserSecRoleInstance.version > version) {

                    secUserSecRoleInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'secUserSecRole.label', default: 'SecUserSecRole')] as Object[], "Another user has updated this SecUserSecRole while you were editing")
                    render(view: "edit", model: [secUserSecRoleInstance: secUserSecRoleInstance])
                    return
                }
            }

            secUserSecRoleInstance.properties = params
            if (!secUserSecRoleInstance.hasErrors() && secUserSecRoleInstance.save(flush: true)) {

                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'secUserSecRole.label', default: 'SecUserSecRole'), secUserSecRoleInstance.id])}"
                redirect(action: "show", id: secUserSecRoleInstance.id)
            } else {
                render(view: "edit", model: [secUserSecRoleInstance: secUserSecRoleInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'secUserSecRole.label', default: 'SecUserSecRole'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def secUserSecRoleInstance = SecUserSecRole.get(params.id)
        if (secUserSecRoleInstance) {
            try {
                secUserSecRoleInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'secUserSecRole.label', default: 'SecUserSecRole'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'secUserSecRole.label', default: 'SecUserSecRole'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'secUserSecRole.label', default: 'SecUserSecRole'), params.id])}"
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
                dataRows = getDataRows(params, SecUserSecRole.class, [max: rows, offset: offset])
            } else {
                dataRows = getDataRows(params, SecUserSecRole.class, [max: rows, offset: offset, sort: params.sidx, order: params.sord == "dsc" ? "desc" : "asc"])
            }
        } else if (params.sidx == "") {
            dataRows = SecUserSecRole.list(max: rows, offset: offset)
        } else {
            dataRows = SecUserSecRole.list(max: rows, offset: offset, sort: params.sidx, order: params.sord == "dsc" ? "desc" : "asc")
        }

        dataRows = dataRows.collect {
            [cell: [it.id.toString(), it.secRole, it.user], id: it.id]
        }
        def size = SecUserSecRole.list().size()
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

            def secUserSecRoleInstance = new SecUserSecRole(params)
            if (!secUserSecRoleInstance.save(flush: true)) {
                errors = secUserSecRoleInstance.errors
            }

        } else if (params.oper == "edit") {
            def secUserSecRoleInstance = SecUserSecRole.get(params.id)
            if (secUserSecRoleInstance) {
                if (params.version && secUserSecRoleInstance.version > params.version.toLong()) {
                    errors.errors = [[message: "Another user has updated this SecUserSecRole while you were editing"]]
                } else {

                    secUserSecRoleInstance.properties = params
                    if (!secUserSecRoleInstance.save(flush: true)) {
                        errors = secUserSecRoleInstance.errors
                    }

                }
            } else {
                errors.errors = [[message: "${message(code: 'default.not.found.message', args: [message(code: 'secUserSecRole.label', default: 'SecUserSecRole'), params.id])}"]]
            }
        } else if (params.oper == "del") {
            def secUserSecRoleInstance = SecUserSecRole.findById(params.id)
            if (secUserSecRoleInstance) {
                try {
                    secUserSecRoleInstance.delete(flush: true)
                }
                catch (org.springframework.dao.DataIntegrityViolationException e) {
                    errors.errors = [[message: "${message(code: 'default.not.deleted.message', args: [message(code: 'secUserSecRole.label', default: 'SecUserSecRole'), params.id])}"]]
                }
            } else {
                errors.errors = [[message: "${message(code: 'default.not.found.message', args: [message(code: 'secUserSecRole.label', default: 'SecUserSecRole'), params.id])}"]]
            }
        }
        render errors as JSON
    }

}
