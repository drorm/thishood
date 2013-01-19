package com.thishood.domain

import grails.converters.JSON

class GroupResource {

	UserGroup userGroup
	Boolean forAllBlocks
	String title
	String description
	Date createdTime
	Date editedTime
	Long editedUserId
	String uploadsJson

	static constraints = {
		userGroup nullable:true
		forAllBlocks nullable:true
		title nullable:false, blank:false
		description nullable:false, blank:false
		editedUserId nullable:false, min:0L
		uploadsJson nullable:true
	}

	static mapping = {
		description type:"text"
		uploadsJson type:"text"
	}

	static transients = ["uploads"]

	private uploads = null

	def getUploads() {
		if (!uploads && uploadsJson) {
			def uploadIds = JSON.parse(uploadsJson)
			if (uploadIds != null && uploadIds.size() > 0) {
				uploads = Upload.getAll(uploadIds)
			}
		}
		uploads.findAll { it != null }
	}

	private toList(String value) {
		return [value]
	}

	private toList(value) {
		value ?: []
	}

	def springSecurityService

	def setUploads(uploadIds) {
		if (uploadIds) {
			uploadIds = toList(uploadIds)
			def user = springSecurityService.currentUser
			def uploads = []
			uploadIds.each {
				def uploadId = ConvertUtils.toLong(it)
				if (Upload.findByIdAndUser(uploadId, user)) {
					uploads.add(uploadId)
				}
			}
			uploadsJson = uploads as JSON
		} else {
			uploadsJson = null
		}
		uploads = null
	}

}