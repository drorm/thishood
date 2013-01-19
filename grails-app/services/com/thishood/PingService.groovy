package com.thishood

import com.thishood.domain.Membership
import com.thishood.domain.UserPing
import com.thishood.domain.User

class PingService {

    static transactional = true

    def membershipService
    def userGroupService

    def findActiveUsers(Long groupId) {
        List<Membership> memberships = membershipService.findAllByGroup(groupId)

        Calendar cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, -1)
        Date alive = cal.time

        def cr = UserPing.createCriteria()
        def users = cr.listDistinct {
            or {
                memberships.each {membership ->
                    eq("user.id", membership.user.id)
                }
            }
            gt("lastPing", alive)
        }
        users
    }

    UserPing getByUser(Long userId) {
        UserPing.find("from UserPing up where up.user.id = :userId", [userId: userId])
    }

    // todo vitaliy@04.03.11 probably need to refactor it because it's a potential bottleneck
    // when 1000 users will open ThisHood then every N seconds (where N=duration between poll)
    // it will be updated 1000 ping records
    // and what if 10000 users will open ThisHood? or more? ThisHood will spend a lot of time on updating this table
    // other point is that when user opens few tabs of Thishood, for instance 1 tab with group 'my block'
    // and other tab with group 'books in Albany'
    // as I understand this may cause problems when in both groups people will post messages concurrently
    // and I propose to update ping only when user clicks on 'M new messages' button (to see them)
    def update(Long userId, Date lastThread){
        def user = User.getOrFail(userId)

        UserPing ping = getByUser(userId)
        if (ping) {
            ping.lastPing = new Date()
            ping.lastThread = lastThread
            ping.save(failonError: true)
        } else {
            ping = new UserPing([
                    user: user,
                    lastPing: new Date(),
                    lastThread: lastThread
            ])
            if (ping.save()) {
                log.error("Can't save user ping")
            }
        }
    }

}
