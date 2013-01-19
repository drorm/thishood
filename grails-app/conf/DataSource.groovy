dataSource {
    pooled = true
    dbCreate = "update"
    driverClassName = "com.mysql.jdbc.Driver"
    url = "jdbc:mysql://localhost/thishood"
    username = "thishood"
    password = "xxxxxxxxxxxxxxxx"
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings
environments {
    development {
        dataSource {
			loggingSql = true
            pooled = true
            dbCreate = "update"
            driverClassName = "com.mysql.jdbc.Driver"
            url = "jdbc:mysql://localhost/thishood"
            username = "thishood"
            password = "xxxxxxxxxxxxxxxx"
        }
    }

    test {
        dataSource {
            pooled = true
            dbCreate = ""
            driverClassName = "com.mysql.jdbc.Driver"
            url = "jdbc:mysql://localhost/thishood"
            username = "thishood"
            password = "XXXXXXXXXXXXXXXXXX"
        }
    }
    production {
        dataSource {
            pooled = true
            dbCreate = ""
            driverClassName = "com.mysql.jdbc.Driver"
            url = "jdbc:mysql://localhost/thishood"
            username = "thishood"
            password = "XXXXXXXXXXXXXX"
        }
    }
}

