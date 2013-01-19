package com.thishood.admin

import com.thishood.domain.SecRequestmap
import grails.converters.JSON
import grails.plugins.springsecurity.Secured

/**
 * Note: on this stage we use annotations and this controller has no sense yet
 */
@Secured(["hasRole('ROLE_ADMIN')"])
class SecRequestmapController {


    def springSecurityService



    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
//		params.max = Math.min(params.max ? params.int('max') : 10, 100)
//		[secRequestmapInstanceList: SecRequestmap.list(params), secRequestmapInstanceTotal: SecRequestmap.count()]
    }

    def create = {
        def secRequestmapInstance = new SecRequestmap()
        secRequestmapInstance.properties = params
        return [secRequestmapInstance: secRequestmapInstance]
    }

    def save = {

        def secRequestmapInstance = new SecRequestmap(params)
        if (secRequestmapInstance.save(flush: true)) {

            // reload the cache from the db with the new rule
            springSecurityService.clearCachedRequestmaps()

            flash.message = "${message(code: 'default.created.message', args: [message(code: 'secRequestmap.label', default: 'SecRequestmap'), secRequestmapInstance.id])}"
            redirect(action: "show", id: secRequestmapInstance.id)
        } else {
            render(view: "create", model: [secRequestmapInstance: secRequestmapInstance])
        }
    }

    def show = {
        def secRequestmapInstance = SecRequestmap.get(params.id)
        if (!secRequestmapInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'secRequestmap.label', default: 'SecRequestmap'), params.id])}"
            redirect(action: "list")
        }
        else {
            [secRequestmapInstance: secRequestmapInstance]
        }
    }

    def edit = {
        def secRequestmapInstance = SecRequestmap.get(params.id)
        if (!secRequestmapInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'secRequestmap.label', default: 'SecRequestmap'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [secRequestmapInstance: secRequestmapInstance]
        }
    }

    def update = {
        def secRequestmapInstance = SecRequestmap.get(params.id)
        if (secRequestmapInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (secRequestmapInstance.version > version) {

                    secRequestmapInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'secRequestmap.label', default: 'SecRequestmap')] as Object[], "Another user has updated this SecRequestmap while you were editing")
                    render(view: "edit", model: [secRequestmapInstance: secRequestmapInstance])
                    return
                }
            }

            secRequestmapInstance.properties = params
            if (!secRequestmapInstance.hasErrors() && secRequestmapInstance.save(flush: true)) {

                // reload the cache from the db with the new rule
                springSecurityService.clearCachedRequestmaps()

                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'secRequestmap.label', default: 'SecRequestmap'), secRequestmapInstance.id])}"
                redirect(action: "show", id: secRequestmapInstance.id)
            } else {
                render(view: "edit", model: [secRequestmapInstance: secRequestmapInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'secRequestmap.label', default: 'SecRequestmap'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def secRequestmapInstance = SecRequestmap.get(params.id)
        if (secRequestmapInstance) {
            try {
                secRequestmapInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'secRequestmap.label', default: 'SecRequestmap'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'secRequestmap.label', default: 'SecRequestmap'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'secRequestmap.label', default: 'SecRequestmap'), params.id])}"
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
                dataRows = getDataRows(params, SecRequestmap.class, [max: rows, offset: offset])
            } else {
                dataRows = getDataRows(params, SecRequestmap.class, [max: rows, offset: offset, sort: params.sidx, order: params.sord == "dsc" ? "desc" : "asc"])
            }
        } else if (params.sidx == "") {
            dataRows = SecRequestmap.list(max: rows, offset: offset)
        } else {
            dataRows = SecRequestmap.list(max: rows, offset: offset, sort: params.sidx, order: params.sord == "dsc" ? "desc" : "asc")
        }

        dataRows = dataRows.collect {
            [cell: [it.id.toString(), it.url, it.configAttribute], id: it.id]
        }
        def size = SecRequestmap.list().size()
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

            def secRequestmapInstance = new SecRequestmap(params)
            if (!secRequestmapInstance.save(flush: true)) {
                errors = secRequestmapInstance.errors
            }

            else {
                // reload the cache from the db with the new rule
                springSecurityService.clearCachedRequestmaps()
            }

        } else if (params.oper == "edit") {
            def secRequestmapInstance = SecRequestmap.get(params.id)
            if (secRequestmapInstance) {
                if (params.version && secRequestmapInstance.version > params.version.toLong()) {
                    errors.errors = [[message: "Another user has updated this SecRequestmap while you were editing"]]
                } else {

                    secRequestmapInstance.properties = params
                    if (!secRequestmapInstance.save(flush: true)) {
                        errors = secRequestmapInstance.errors
                    }

                    else {
                        // reload the cache from the db with the new rule
                        springSecurityService.clearCachedRequestmaps()
                    }

                }
            } else {
                errors.errors = [[message: "${message(code: 'default.not.found.message', args: [message(code: 'secRequestmap.label', default: 'SecRequestmap'), params.id])}"]]
            }
        } else if (params.oper == "del") {
            def secRequestmapInstance = SecRequestmap.findById(params.id)
            if (secRequestmapInstance) {
                try {
                    secRequestmapInstance.delete(flush: true)
                }
                catch (org.springframework.dao.DataIntegrityViolationException e) {
                    errors.errors = [[message: "${message(code: 'default.not.deleted.message', args: [message(code: 'secRequestmap.label', default: 'SecRequestmap'), params.id])}"]]
                }
            } else {
                errors.errors = [[message: "${message(code: 'default.not.found.message', args: [message(code: 'secRequestmap.label', default: 'SecRequestmap'), params.id])}"]]
            }
        }
        render errors as JSON
    }

}
