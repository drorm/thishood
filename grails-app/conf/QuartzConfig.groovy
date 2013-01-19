
quartz {
    autoStartup = true
    //todo vitaliy@22.02.10 for scalability we will have to make a cluster??
    jdbcStore = false
    waitForJobsToCompleteOnShutdown = true
}

/*
environments {
    test {
        quartz {
            autoStartup = false
        }
    }
}
*/
