class UrlMappings {

    static mappings = {
        "/communityMetric/$action/$id/$date"(controller:"communityMetric") {
            constraints {
                // apply constraints here
            }
        }
        "/$controller/$action?/$id?" {
            constraints {
                // apply constraints here
            }
        }

        "/"(controller: "home", action: "index")
        "500"(view: '/error')
        //"500"(controller: 'error', action: 'index')
    }
}
