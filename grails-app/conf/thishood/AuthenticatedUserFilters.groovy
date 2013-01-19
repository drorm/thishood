package thishood

/**
 * Adds automatically to model an authenticated user by key 'user'
 */
class AuthenticatedUserFilters {

  def springSecurityService

  def filters = {
    all(controller: '*', action: '*') {
      before = {

      }

      after = { model ->
        if (springSecurityService.isLoggedIn() && !model.user) {
          model.user = springSecurityService.currentUser
        }
      }

      afterView = {

      }
    }
  }
}
