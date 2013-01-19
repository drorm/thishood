import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import com.thishood.security.RedirectableAjaxAwareAuthenticationSuccessHandler
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

// Place your Spring DSL code here
beans = {

	dataSource(ComboPooledDataSource) { bean ->
		bean.destroyMethod = 'close'
		user = CH.config.dataSource.username
		password = CH.config.dataSource.password
		driverClass = CH.config.dataSource.driverClassName
		jdbcUrl = CH.config.dataSource.url

		//
		// Settings:
		// http://www.mchange.com/projects/c3p0/index.html
		// http://community.jboss.org/wiki/HowToconfiguretheC3P0connectionpool
		//

		//force connections to renew after 2 hours
		maxConnectionAge = 2 * 60 * 60
		//get rid too many of idle connections after 30 minutes
		maxIdleTimeExcessConnections = 30 * 60
		//table for connections testing
		automaticTestTable = '_c3p0_test'
		//configure pool size
		minPoolSize = 10
		maxPoolSize = 30
		//maximum number of prepared statements pooled
		maxStatements = 20 * 10
		//maximum number of prepared statements pooled for one connection
		maxStatementsPerConnection = 50 + 2
		//connection test settings
		idleConnectionTestPeriod = 15 * 60 //15 mins
		testConnectionOnCheckin = true
	}

	authenticationSuccessHandler(RedirectableAjaxAwareAuthenticationSuccessHandler) {
      def conf = SpringSecurityUtils.securityConfig

		requestCache = ref('requestCache')
		defaultTargetUrl = conf.successHandler.defaultTargetUrl // '/'
		alwaysUseDefaultTargetUrl = conf.successHandler.alwaysUseDefault // false
		targetUrlParameter = conf.successHandler.targetUrlParameter // 'spring-security-redirect'
		ajaxSuccessUrl = conf.successHandler.ajaxSuccessUrl // '/login/ajaxSuccess'
		useReferer = conf.successHandler.useReferer // false
		redirectStrategy = ref('redirectStrategy')
   }
/*
    this should be customized for case when activemq will be out of container

    //
    jmsConnectionFactory(org.apache.activemq.pool.PooledConnectionFactory) {bean ->
        bean.destroyMethod = "stop"
        connectionFactory(org.apache.activemq.ActiveMQConnectionFactory) {
            brokerURL = "tcp://localhost:61616"
        }
    }
    //or
    jmsfactory(org.apache.activemq.pool.PooledConnectionFactory) { bean ->
         bean.destroyMethod = "stop"
         connectionFactory = { org.apache.activemq.ActiveMQConnectionFactory cf ->
           brokerURL = "tcp://192.168.70.201:61616"
         }
      }
*/

}
