import com.thishood.domain.SecRole
import com.thishood.domain.SecUserSecRole
import grails.util.GrailsConfig
import javax.servlet.http.HttpServletRequest
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.plugins.springsecurity.ReflectionUtils

class BootStrap {
    def grailsApplication
    def springSecurityService
	def marketService
    def membershipService
    def userGroupService
    def userService

    def init = { servletContext ->
        HttpServletRequest.metaClass.getBaseUrl = {->
            servletContext.getContextPath()
        }

        /**
        SpringSecurityUtils.metaClass.'static'.isAjax = {request ->
            if (!(request instanceof HttpServletRequest)) throw new IllegalArgumentException("Can't process type [${request}]")

            String ajaxHeaderName = (String) ReflectionUtils.getConfigProperty("ajaxHeader");

            // check the current request's headers
            if (request.getHeader(ajaxHeaderName) != null) {
                return true
            }

            // look for an ajax=true parameter
            if ("true".equals(request.getParameter("ajax"))) {
                return true
            }

            // we don't check it because it breaks further coming requests -- they are all redirected to login page
            //
            // check the SavedRequest's headers
            //SavedRequest savedRequest = (SavedRequest) request.getSession().getAttribute(WebAttributes.SAVED_REQUEST);
            //if (savedRequest != null) {
            //    return !savedRequest.getHeaderValues(ajaxHeaderName).isEmpty();
            //}

            false
        }
        */

        // Black magic -- adding extra dynamic methods to all domains
        grailsApplication.domainClasses.each {dc ->
            // Domain.getOrFail(id)
            // Retrieves an instance of the domain class for the specified id,
            // if the object doesn't exist a runtime exception throwed
            dc.metaClass.'static'.getOrFail = { id ->
                if (!id) throw new RuntimeException("Can't get with [null] id for ${dc}")
                def entity = delegate.get(id)
                if (!entity) throw new RuntimeException("Can't get with [${id}] for ${dc}")
                entity
            }
            // works as domain.save() but doesn't return null
            // in case of fail returns original domain
            dc.metaClass.saveSafely = { map ->
                def persisted = delegate.save(map)
                persisted?:delegate
            }
        }
		grailsApplication.controllerClasses.each {clazz ->
			clazz.metaClass.error404 = { ->
				delegate.log.warn("Sending 404 response from [${getControllerName()}:${getActionName()}] of request [${request.requestURL}]")
				response.sendError(404)
				return
			}
		}



        userGroupService.createDefaultUserGroups()
		marketService.create()

        def adminRole = SecRole.findByAuthority('ROLE_ADMIN') ?: new SecRole(authority: 'ROLE_ADMIN').save(failOnError: true) //Create role for application admin

        //If no Admin user in the db, create one.
        def adminUser = userService.createUserOnce(
                first: 'ThisHood',
                last: 'Admin',
                password: 'AlXXXXXXXXi6',       // password will be encoded in service
                email: 'admin@thishood.com',
                address: 'none',
                city: 'none',
                state: 'none',
                zip: 'none',
                country: 'none',
                enabled: true)

        if (adminUser.accountLocked) {
            // by default user has locked account and on verification it unlocks
            // for admin user lets use a hack ;-)
            adminUser.enabled=true
            adminUser.accountLocked=false
            adminUser.tourTaken=true
            adminUser.save(failOnError:true)
        }

        if (!adminUser.roles.contains(adminRole)) {
            SecUserSecRole.create adminUser, adminRole
        }

    }

    def destroy = {
    }
}
