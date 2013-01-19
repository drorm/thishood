package com.thishood

import org.springframework.beans.factory.InitializingBean


class ForeignContentService implements InitializingBean{

    static transactional = false

	def springcacheService

	def resources = [:]

	public static String RSS_ALBANY = "http://albany.patch.com/articles.rss"

    void fetch(String url) {
		def xmlFeed = new XmlParser().parse(url);

		def maxItems = xmlFeed.channel.item.size()
		maxItems = maxItems > 5 ? 5 : maxItems
		def items = []
		(0..<maxItems).each {
			def item = xmlFeed.channel.item.get(it);
			items << item
		}

		resources.(url) = [new Date(), items]
    }

	def get(String url) {
		def content = resources.(url)
		if (!content) {
			content = [new Date(), []]
		}
		content
	}

	void afterPropertiesSet() {
		try {
			fetch(RSS_ALBANY)
		} catch (any) {
			log.error("Unable to fetch data", any)
		}
	}


}
