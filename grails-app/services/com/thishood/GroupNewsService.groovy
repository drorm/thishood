package com.thishood

import com.thishood.domain.GroupNews;
import com.thishood.domain.UserGroup;

class GroupNewsService {
	static transactional = true

	def addGroupNews(Long groupId, String news) {
		def userGroup = UserGroup.getOrFail(groupId)
		def groupNews = new GroupNews()

		groupNews.content = news
		groupNews.dateCreated = new Date()
		groupNews.userGroup = userGroup
		groupNews.save()
		
		groupNews
	}

	GroupNews getLatestNews(Long groupId) {
		def group = UserGroup.getOrFail(groupId)
		def news = GroupNews.findAllByUserGroup(group, [offset: 0, sort: 'dateCreated', order: 'desc', max: 1])
		news ? news[0] : null
	}

	void deleteByGroup(Long groupId) {
		UserGroup group = UserGroup.getOrFail(groupId)

		GroupNews.findAll("from GroupNews gn where gn.userGroup = :group", [group: group]).each { GroupNews news ->
			news.delete(flush:true)
		}
	}
}
