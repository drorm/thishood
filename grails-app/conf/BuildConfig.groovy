grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        mavenLocal()
        mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
        mavenRepo "http://maven.cedarsoft.com/content/repositories/thirdparty/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        runtime 'mysql:mysql-connector-java:5.1.13'
        compile 'c3p0:c3p0:0.9.1.2',
				'im4java:im4java:1.1.0',
				'jfree:jfreechart:1.0.13'
        //compile 'org.hibernate:hibernate-c3p0:3.3.1.GA'
        compile ('org.apache.tika:tika-parsers:0.9') {
			excludes (
					[group: 'org.apache.geronimo.specs'],
					[group: 'org.ccil.cowan.tagsoup'],
					[group: 'de.l3s.boilerpipe'],
					[group: 'rome'],
					[group: 'stax', name: 'stax-api'],
					[group: 'xml-apis', name: 'xml-apis'],
					[group: 'org.apache.xmlbeans', name: 'xmlbeans'],
					[group: 'org.bouncycastle'],
					[group: 'org.apache.pdfbox'],
					[group: 'edu.ucar']
			)
		}
        // enable it in case if email sending will fail via ActiveMQ (in container)
        //runtime 'org.springframework:org.springframework.test:3.0.0.RELEASE'
    }
}
