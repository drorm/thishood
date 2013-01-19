package thishood

import com.thishood.ThisHoodConstant

/**
 * TH-170 Browser caches Ajax' loaded pages and it causes inconsistency in application behavior
 * Solution -  don't let browser cache pages
 *
 * Note: this is temporary solution
 */
class CacheContentFilters {

    def filters = {
        all(controller: '*', action: '*') {
            before = {

            }
            after = {
				if (!request.getAttribute(ThisHoodConstant.ATTRIBUTE_ENABLE_CACHING)) {
					response.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate')
					response.setDateHeader('Expires', (new Date() - 1).time)
					response.setHeader('Pragma', 'no-cache, no-store')
				}
            }
            afterView = {

            }
        }
    }

}
