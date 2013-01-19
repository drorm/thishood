package com.thishood.admin

import com.thishood.domain.SecRole
import grails.converters.JSON
import grails.plugins.springsecurity.Secured

@Secured(["hasRole('ROLE_ADMIN')"])
class SecRoleController {


    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
//		params.max = Math.min(params.max ? params.int('max') : 10, 100)
//		[secRoleInstanceList: SecRole.list(params), secRoleInstanceTotal: SecRole.count()]
    }

    def create = {
        def secRoleInstance = new SecRole()
        secRoleInstance.properties = params
        return [secRoleInstance: secRoleInstance]
    }

    def save = {

        def secRoleInstance = new SecRole(params)
        if (secRoleInstance.save(flush: true)) {

            flash.message = "${message(code: 'default.created.message', args: [message(code: 'secRole.label', default: 'SecRole'), secRoleInstance.id])}"
            redirect(action: "show", id: secRoleInstance.id)
        } else {
            render(view: "create", model: [secRoleInstance: secRoleInstance])
        }
    }

    def show = {
        def secRoleInstance = SecRole.get(params.id)
        if (!secRoleInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'secRole.label', default: 'SecRole'), params.id])}"
            redirect(action: "list")
        }
        else {
            [secRoleInstance: secRoleInstance]
        }
    }

    def edit = {
        def secRoleInstance = SecRole.get(params.id)
        if (!secRoleInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'secRole.label', default: 'SecRole'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [secRoleInstance: secRoleInstance]
        }
    }

    def update = {
        def secRoleInstance = SecRole.get(params.id)
        if (secRoleInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (secRoleInstance.version > version) {

                    secRoleInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'secRole.label', default: 'SecRole')] as Object[], "Another user has updated this SecRole while you were editing")
                    render(view: "edit", model: [secRoleInstance: secRoleInstance])
                    return
                }
            }

            secRoleInstance.properties = params
            if (!secRoleInstance.hasErrors() && secRoleInstance.save(flush: true)) {

                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'secRole.label', default: 'SecRole'), secRoleInstance.id])}"
                redirect(action: "show", id: secRoleInstance.id)
            } else {
                render(view: "edit", model: [secRoleInstance: secRoleInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'secRole.label', default: 'SecRole'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def secRoleInstance = SecRole.get(params.id)
        if (secRoleInstance) {
            try {
                secRoleInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'secRole.label', default: 'SecRole'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'secRole.label', default: 'SecRole'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'secRole.label', default: 'SecRole'), params.id])}"
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
                dataRows = getDataRows(params, SecRole.class, [max: rows, offset: offset])
            } else {
                dataRows = getDataRows(params, SecRole.class, [max: rows, offset: offset, sort: params.sidx, order: params.sord == "dsc" ? "desc" : "asc"])
            }
        } else if (params.sidx == "") {
            dataRows = SecRole.list(max: rows, offset: offset)
        } else {
            dataRows = SecRole.list(max: rows, offset: offset, sort: params.sidx, order: params.sord == "dsc" ? "desc" : "asc")
        }

        dataRows = dataRows.collect {
            [cell: [it.id.toString(), it.authority], id: it.id]
        }
        def size = SecRole.list().size()
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

            def secRoleInstance = new SecRole(params)
            if (!secRoleInstance.save(flush: true)) {
                errors = secRoleInstance.errors
            }

        } else if (params.oper == "edit") {
            def secRoleInstance = SecRole.get(params.id)
            if (secRoleInstance) {
                if (params.version && secRoleInstance.version > params.version.toLong()) {
                    errors.errors = [[message: "Another user has updated this SecRole while you were editing"]]
                } else {

                    secRoleInstance.properties = params
                    if (!secRoleInstance.save(flush: true)) {
                        errors = secRoleInstance.errors
                    }

                }
            } else {
                errors.errors = [[message: "${message(code: 'default.not.found.message', args: [message(code: 'secRole.label', default: 'SecRole'), params.id])}"]]
            }
        } else if (params.oper == "del") {
            def secRoleInstance = SecRole.findById(params.id)
            if (secRoleInstance) {
                try {
                    secRoleInstance.delete(flush: true)
                }
                catch (org.springframework.dao.DataIntegrityViolationException e) {
                    errors.errors = [[message: "${message(code: 'default.not.deleted.message', args: [message(code: 'secRole.label', default: 'SecRole'), params.id])}"]]
                }
            } else {
                errors.errors = [[message: "${message(code: 'default.not.found.message', args: [message(code: 'secRole.label', default: 'SecRole'), params.id])}"]]
            }
        }
        render errors as JSON
    }

}
