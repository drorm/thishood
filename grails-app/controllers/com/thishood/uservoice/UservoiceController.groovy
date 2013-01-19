package com.thishood.uservoice

import grails.plugins.springsecurity.Secured
import java.text.SimpleDateFormat
import javax.mail.internet.InternetAddress

import com.uservoice.TokenGenerator

import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.web.json.JSONObject
import com.thishood.domain.User
import com.thishood.domain.UserRole
import com.thishood.image.ImageSize

@Secured(["isAuthenticated()"])
class UservoiceController {

	def springSecurityService
	def userService

	def getSsoToken = {
		render '"' + getSSOToken(springSecurityService.currentUser) + '"'
	}

	private String getSSOToken(User user) {
			JSONObject jsonObj = new JSONObject()
			jsonObj.put("guid", user.id)
			// Token expires in 1 hour
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
			dateformat.timeZone = TimeZone.getTimeZone("UTC")
			Calendar cal = Calendar.instance
			cal.add(Calendar.HOUR, 1)
			jsonObj.put("expires", dateformat.format(cal.time))
			jsonObj.put("email", user.email)
			jsonObj.put("display_name", user.displayName)
			jsonObj.put("admin", user.hasRole(UserRole.ROLE_ADMIN) ? "accept" : "deny")
			jsonObj.put("url", grailsApplication.config.grails.serverURL + "/home/viewUserProfile?userId=" + user.id)
			jsonObj.put("avatar_url", userService.getPhotoUrl(user.id, ImageSize.SMALL))

			def token = TokenGenerator.getInstance().create(jsonObj)

			return token
	}
}
