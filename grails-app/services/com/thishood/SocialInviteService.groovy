package com.thishood

import com.thishood.domain.User
import com.thishood.domain.SocialInvite
import com.thishood.domain.SocialProvider

class SocialInviteService {

  static transactional = true
  def facebookGraphService

  void saveInvitedFriends(Long userId, SocialProvider provider, String... friendIds) {
    User user = User.getOrFail(userId)

    def jsonFriends = facebookGraphService.friends

    //obtain all to reduce DB roundtrips
    List<SocialInvite> persistedInvites = findAllByUser(userId, provider)

    friendIds.each { friendId ->
      SocialInvite foundInvite = persistedInvites.find { it.friendId = friendId}
      if(foundInvite) {
        log.error("User [${user}] invited already invited friend [${friendId}] from [${provider}]")
      } else {
        def jsonFriend = jsonFriends.data.find {it.id == friendId}
        SocialInvite invite = new SocialInvite(
                user: user,
                provider: provider,
                friendId: friendId,
                friendName: jsonFriend.name
        )
        invite.save(failOnError: true)
      }
    }

  }

  List<SocialInvite> findAllByUser(Long userId, SocialProvider provider) {
    User user = User.getOrFail(userId)

    SocialInvite.findAllByUserAndProvider(user, provider)
  }

  List<String> alreadyInvitedIds(Long userId, SocialProvider provider) {
    def result = []
    def jsonFriends = facebookGraphService.getFriends()
    if(jsonFriends) {
      def ids = jsonFriends.data.collect {it.id}
      def c = SocialInvite.createCriteria()
      def invitedFriends = c {
        projections {
          groupProperty("friendId")
          rowCount()
        }
        and {
          inList("friendId", ids)
        }
      }
      result = invitedFriends.collectAll {it[0]}
    }
    result
  }
}
