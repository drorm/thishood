package com.thishood.security;

import org.codehaus.groovy.grails.plugins.springsecurity.AjaxAwareAuthenticationSuccessHandler;
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This is an replacement of {@link AjaxAwareAuthenticationSuccessHandler} which replaces original behavior
 * and keeps redirection url.
 * So when anonymous user accesses resource which is protected then on successful auth'z he/she will be redirected to it.
 *
 * <b>Note:</b> switch on your brain and think about why successHandler.alwaysUseDefault should be set to 'true'
 */
public class RedirectableAjaxAwareAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
	public static final String ATTRIBUTE_SAVED_REDIRECT_URL = "_savedRedirectUrl";

	private String _ajaxSuccessUrl;
	private RequestCache _requestCache;

	@Override
	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
		if (SpringSecurityUtils.isAjax(request)) {
			return _ajaxSuccessUrl;
		}
		return super.determineTargetUrl(request, response);
	}

	/**
	 * Dependency injection for the Ajax success url, e.g. '/login/ajaxSuccess'
	 *
	 * @param ajaxSuccessUrl the url
	 */
	public void setAjaxSuccessUrl(final String ajaxSuccessUrl) {
		_ajaxSuccessUrl = ajaxSuccessUrl;
	}

	@Override
	public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) throws ServletException, IOException {
		SavedRequest savedRequest = _requestCache.getRequest(request, response);

		if (savedRequest != null) {
			request.getSession().setAttribute(ATTRIBUTE_SAVED_REDIRECT_URL, savedRequest.getRedirectUrl());
		}

		super.onAuthenticationSuccess(request, response, authentication);

		// always remove the saved request
		_requestCache.removeRequest(request, response);
	}

	@Override
	public void setRequestCache(RequestCache requestCache) {
		super.setRequestCache(requestCache);
		_requestCache = requestCache;
	}
}
