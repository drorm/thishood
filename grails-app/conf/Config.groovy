import grails.util.Environment
import grails.plugins.springsecurity.SecurityConfigType

// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [html: ['text/html', 'application/xhtml+xml'],
        xml: ['text/xml', 'application/xml'],
        text: 'text/plain',
        js: 'text/javascript',
        rss: 'application/rss+xml',
        atom: 'application/atom+xml',
        css: 'text/css',
        csv: 'text/csv',
        all: '*/*',
        json: ['application/json', 'text/json'],
        form: 'application/x-www-form-urlencoded',
        multipartForm: 'multipart/form-data'
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = false
grails.views.javascript.library = "jquery"
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

//grails.gorm.failOnError = false     //false is default value, but will be changed after refactoring
//grails.gorm.autoFlush = false       //by default false, and better to keep it false to help Hibernate optimize database calls

// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "https://www.thishood.com/app"
        log4j.appender.'errors.File' = "/var/log/thishood/stacktrace.log"
    }
    test {
        grails.serverURL = "https://dev.thishood.com/app"
        log4j.appender.'errors.File' = "/var/log/thishood/stacktrace.log"
    }
    development {
        //note: this is a pseudo host -- configure on your dev environment file 'hosts' -- should point to 127.0.0.1
        grails.serverURL = "http://dev-local.thishood.org:8080/${appName}"
    }
}

// log4j configuration
// Basic log levels are ALL < TRACE < DEBUG < INFO < WARN < ERROR < FATAL < OFF
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%c{2} %m%n')
    }

    info 'org.codehaus.groovy.grails.web.servlet',  //  controllers
            'org.codehaus.groovy.grails.web.pages', //  GSP
            'org.codehaus.groovy.grails.web.sitemesh', //  layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping', // URL mapping
            'org.codehaus.groovy.grails.commons', // core / classloading
            'org.codehaus.groovy.grails.plugins', // plugins
            'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
            'org.hibernate',
            'net.sf.ehcache.hibernate'



    warn 'org.mortbay.log'

    info 'org.springframework.security',
            'grails.plugins.springsecurity'
    debug 'grails.app'
}


if (Environment.current == Environment.DEVELOPMENT) {
	grails.mail.host = "smtp.gmail.com"
	grails.mail.from = "ThisHood <thishood@gmail.com>"
	grails.mail.port = "465"
	grails.mail.username = "thishood@gmail.com"
	grails.mail.password = "XXXXXXXXXXXXXXXX"
	grails.mail.props = ["mail.smtp.auth": "true",
			"mail.smtp.socketFactory.port": "465",
			"mail.smtp.socketFactory.class": "javax.net.ssl.SSLSocketFactory",
			"mail.smtp.socketFactory.fallback": "false",
			"mail.smtp.starttls.enable": "true",
			"mail.debug": "true"]
} else {
	grails.mail.host = "localhost"
	grails.mail.from = "ThisHood <support@thishood.com>"
	grails.mail.port = "25"
}

//note - it doesn't work
jquery.plugins.scrollablecombo = jquery.scrollablecombo.js

//
jqueryUi {
    minified = false
    cdn = 'googlecode'

    if (Environment.current == Environment.PRODUCTION) {
        minified = true
    }
}

// Added by the JQuery Validation plugin:
jqueryValidation {
    packed = true
    cdn = false  // false or "microsoft" - note: microsoft doesn't work in 1.7.3!!!!!
    additionalMethods = false
}

// Added by the JQuery Validation UI plugin:
jqueryValidationUi {
    errorClass = 'error'
    validClass = 'valid'
    onsubmit = true
    renderErrorsOnTop = false

    qTip {
        packed = true
        classes = 'ui-tooltip-red ui-tooltip-shadow ui-tooltip-rounded'
    }

    /*
       Grails constraints to JQuery Validation rules mapping for client side validation.
       Constraint not found in the ConstraintsMap will trigger remote AJAX validation.
     */
    StringConstraintsMap = [
            blank: 'required', // inverse: blank=false, required=true
            creditCard: 'creditcard',
            email: 'email',
            inList: 'inList',
            minSize: 'minlength',
            maxSize: 'maxlength',
            size: 'rangelength',
            matches: 'matches',
            notEqual: 'notEqual',
            url: 'url',
            nullable: 'required',
            unique: 'unique',
            validator: 'validator'
    ]

    // Long, Integer, Short, Float, Double, BigInteger, BigDecimal
    NumberConstraintsMap = [
            min: 'min',
            max: 'max',
            range: 'range',
            notEqual: 'notEqual',
            nullable: 'required',
            inList: 'inList',
            unique: 'unique',
            validator: 'validator'
    ]

    CollectionConstraintsMap = [
            minSize: 'minlength',
            maxSize: 'maxlength',
            size: 'rangelength',
            nullable: 'required',
            validator: 'validator'
    ]

    DateConstraintsMap = [
            min: 'minDate',
            max: 'maxDate',
            range: 'rangeDate',
            notEqual: 'notEqual',
            nullable: 'required',
            inList: 'inList',
            unique: 'unique',
            validator: 'validator'
    ]

    ObjectConstraintsMap = [
            nullable: 'required',
            validator: 'validator'
    ]

    CustomConstraintsMap = [
            phone: 'true', // International phone number validation
            phoneUS: 'true'
    ]
}

// Spring Security
grails.plugins.springsecurity.apf.filterProcessesUrl = '/auth/do-login'     //Login form post URL, intercepted by Spring Security filter, default '/j_spring_security_check'
grails.plugins.springsecurity.apf.usernameParameter = "email"               //Login form username parameter, default 'j_username'
grails.plugins.springsecurity.apf.passwordParameter = "XXXXXXxxxxxXX"            //Login form password parameter, default 'j_password'
//grails.plugins.springsecurity.apf.allowSessionCreation=true               //Whether to allow authentication to create an HTTP session
grails.plugins.springsecurity.apf.postOnly=true                             //Whether to allow only POST login requests.
grails.plugins.springsecurity.logout.filterProcessesUrl = '/auth/do-logout' //Logout URL, intercepted by Spring Security filter.
grails.plugins.springsecurity.logout.afterLogoutUrl = '/go/home' 			//URL for redirect after logout.
grails.plugins.springsecurity.successHandler.alwaysUseDefault = true 		//If true, always redirects to the value of successHandler.defaultTargetUrl after successful authentication; otherwise redirects to to originally-requested page.
grails.plugins.springsecurity.requestCache.onlyOnGet = true                         // Whether to cache only a SavedRequest on GET requests.

grails.plugins.springsecurity.userLookup.userDomainClassName = 'com.thishood.domain.User'
grails.plugins.springsecurity.userLookup.usernamePropertyName = 'email'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'com.thishood.domain.SecUserRole'
grails.plugins.springsecurity.userLookup.authoritiesPropertyName = 'roles'
grails.plugins.springsecurity.authority.className = 'com.thishood.domain.SecRole'
grails.plugins.springsecurity.requestMap.className = 'com.thishood.domain.SecRequestmap'
//grails.plugins.springsecurity.dao.reflectionSaltSourceProperty = 'username'
grails.plugins.springsecurity.securityConfigType = SecurityConfigType.Annotation    //'Requestmap'
grails.plugins.springsecurity.auth.loginFormUrl = '/login/show?static=true'         //This line tells the plugin to redirect to the home page whenever authentication is required.
//grails.plugins.springsecurity.auth.ajaxLoginFormUrl = '/auth/loginAjax'           //URL of Ajax login page.
grails.plugins.springsecurity.failureHandler.defaultFailureUrl = '/'                //This line tells the plugin to redirect to the home page whenever authentication fails.
grails.plugins.springsecurity.rejectIfNoRule = true                                // temporary false

grails.plugins.springsecurity.controllerAnnotations.staticRules = [
        '/plugins/blueprint-1.0.1/**': ["permitAll()"],
        '/plugins/jquery-validation-1.7.3/**': ["permitAll()"],
        '/plugins/jquery-1.4.4.1/**': ["permitAll()"],
        '/plugins/jquery-validation-ui-1.1.1/**': ["permitAll()"],
		'/monitoring/**': ["hasRole('ROLE_ADMIN')"],		//javamelody
        '/JQueryRemoteValidator/**': ["permitAll()"],
        '/auth/**': ["permitAll()"],
        '/login/**': ["permitAll()"],
        '/logout/**': ["permitAll()"],
        '/js/**': ["permitAll()"],
        '/css/**': ["permitAll()"],
        '/images/**': ["permitAll()"],
        '/elo/**': ["permitAll()"],     //due email notification
        '/files/**': ["permitAll()"],    //due email notification
        '/DEPLOYED' : ["isAuthenticated()"]
        //'/*': ['isAuthenticated()']
]

//Registration related
grails.plugins.springsecurity.ui.register.emailBody = '''\
Hi $user.username,<br/>
<br/>
You (or someone pretending to be you) created an account using this email address with Thishood.<br/>
<br/>
If you made the request, please  go to <a href="$url">$url</a> to finish the registration. Otherwise, please ignore this message, and the account will expire.
'''

grails.plugins.springsecurity.ui.register.emailFrom = 'support@thishood.com'
grails.plugins.springsecurity.ui.register.emailSubject = 'Your New ThisHood Account'
//Welcome to ThisHood after sign up page
grails.plugins.springsecurity.ui.register.postRegisterUrl = '/register/welcome/'

//
// Remember-me----------------------------------------------------------------------------------------------------------
//
//RememberMe cookie info. See http://burtbeckwith.github.com/grails-spring-security-core/docs/manual/ section 9.3
grails.plugins.springsecurity.rememberMe.cookieName = 'remember-me'
grails.plugins.springsecurity.rememberMe.alwaysRemember = true
//grails.plugins.springsecurity.rememberMe.tokenValiditySeconds  = 1209600 //14days
//grails.plugins.springsecurity.rememberMe.parameter = '_spring_security_remember_me'
grails.plugins.springsecurity.rememberMe.key = 'thishood-rememberme-key' //Part of a salt when the cookie is encrypted. Changing the default makes it harder to execute brute-force attacks.
grails.plugins.springsecurity.rememberMe.useSecureCookie = false
grails.plugins.springsecurity.rememberMe.persistent = true
grails.plugins.springsecurity.rememberMe.persistentToken.domainClassName = 'com.thishood.domain.PersistentLogin'
grails.plugins.springsecurity.rememberMe.persistentToken.seriesLength = 24
grails.plugins.springsecurity.rememberMe.persistentToken.tokenLength = 24
//grails.plugins.springsecurity.atr.rememberMeClass = RememberMeAuthenticationToken


//Note: this is a workaround of HTTPS problem mentioned in TH-147
//if (Environment.current == Environment.TEST) {
//    grails.plugins.springsecurity.successHandler.ajaxSuccessUrl='https://dev.thishood.com/app/login/ajaxSuccess'
//    grails.plugins.springsecurity.failureHandler.ajaxAuthFailUrl='https://dev.thishood.com/app/login/authfail?ajax=true'
//}
//if (Environment.current == Environment.PRODUCTION) {
//    grails.plugins.springsecurity.successHandler.ajaxSuccessUrl='https://www.thishood.com/app/login/ajaxSuccess'
//    grails.plugins.springsecurity.failureHandler.ajaxAuthFailUrl='https://www.thishood.com/app/login/authfail?ajax=true'
//}


//grails.plugins.activemq.port=7892
grails.plugins.activemq.useJmx = true
grails.plugins.activemq.persistent = false

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
 * ThisHood --------------------------------------------------------------------------------------------
 * should be moved to a separate file using grails.config.locations
 */


//Facebook credentials
facebook.applicationId = '189679597723658'
facebook.applicationSecret = '2263e5fb9470b979dd8b450ebbf10965'
facebook.secure=true

if (Environment.current == Environment.DEVELOPMENT) {
    facebook.applicationId = '183520085019173'
    facebook.applicationSecret = 'fb0c825dc3768152a40a18d29d8a7c15'
}


com.thishood.queue.enable = true              //enable or not queue procession


//Image related
com.thishood.util.image.genericImage = "generic.jpg"
com.thishood.util.image.basePath = "/files/"

if (Environment.current == Environment.DEVELOPMENT) {
	com.thishood.util.image.uploadBaseDir = System.properties['base.dir'] + '/web-app/'
	com.thishood.imageMagic.path = "C:\\Program Files\\ImageMagick-6.6.9-Q16"

}
if ((Environment.current == Environment.TEST) || (Environment.current == Environment.PRODUCTION)) {
	com.thishood.util.image.uploadBaseDir = '/data/'
	com.thishood.imageMagic.path = "/usr/bin"
}

thishood.default.usergroup = 'City-Wide'
thishood.market.usergroup = "Market"
