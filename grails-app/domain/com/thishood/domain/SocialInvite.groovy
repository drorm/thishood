package com.thishood.domain

class SocialInvite {
  User user
  SocialProvider provider
  String friendName
  String friendId
  //
  Date dateCreated
  Date lastUpdated

  static constraints = {
    user(nullable: false)
    provider(nullable: false)
    friendId(nullable: false)
    friendName(nullable: true)
  }
}
