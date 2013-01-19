package com.thishood.admin

import grails.plugins.springsecurity.Secured
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import com.thishood.domain.*

@Secured(["hasRole('ROLE_ADMIN')"])
class AdminController {
    def grailsApplication

    def index = {
        //these controllers have method 'list'
        def tabs = [
                //'SecRequestmap (notused)': getDefaultDomainUrl(SecRequestmap),
                'SecRole': getDefaultDomainUrl(SecRole),
                //'SecUserSecRole': getDefaultDomainUrl(SecUserSecRole),
                'User': getDefaultDomainUrl(User),
                'Group': generateLink('adminGroup', 'index', null),
                'Group News': getDefaultDomainUrl(GroupNews),
                'Quartz job': generateLink('quartzJob', 'index', null),
                'Recommendation Type': generateLink('userReferenceType', 'index', null)
        ] //TODO move this to a config file or calculate only once

        render(view: "index", model: [tabs: tabs])
    }

    def help = {
    }


    protected String generateLink(String controller, String action, linkParams) {
        createLink(base: grailsApplication.config.grails.serverURL,
                controller: controller,
                action: action,
                params: linkParams)

    }

    protected String getDefaultDomainUrl(def clazz) {
        def domainClass = new DefaultGrailsDomainClass(clazz)
        def controllerName = domainClass.shortName.charAt(0).toLowerCase().toString() + domainClass.shortName.substring(1)
        grailsApplication.config.grails.serverURL + "/" + controllerName + "/list";
    }



}
