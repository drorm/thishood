package com.thishood.main

import com.thishood.domain.Upload
import com.thishood.domain.User
import grails.converters.JSON
import grails.plugins.springsecurity.Secured
import com.thishood.ThisHoodConstant
import com.thishood.image.ImageSize

@Secured(['isAuthenticated()'])
class UploadsController {

	def grailsApplication
	def springSecurityService
	def uploadService

	static allowedMethods = [upload: "POST", deleteUpload: "POST"]

	def manager = {
	}

	private getUploadsModel(uploads) {
		def result = []
		uploads.each{Upload upload ->
			//image or not processed on client side, but just to be sure
			def isImage = uploadService.isImage(upload.id)
			result.add([
					uploadId: upload.id,
					contentType: upload.contentType,
					dateCreated: upload.dateCreated,
					filename: upload.name,
					length: upload.length,
					imageSmallUrl: isImage ? uploadService.getImageUrl(upload.id, ImageSize.THUMB): "/images/content-type/unknown.png",
					imageLargeUrl: isImage ? uploadService.getImageUrl(upload.id, ImageSize.SMALL): "/images/content-type/unknown.png"
			])
		}
		result
	}

	def getUploads = {
		def result = []

		def user = springSecurityService.currentUser

		try {
			def uploads = uploadService.findAllByUser(user.id)
			result = getUploadsModel(uploads)
		} catch (any) {
			log.error("Can't get uploads for user [${user}]",any)
			result = [error: any.message]
		}

		render result as JSON
	}

	def upload = {
		def result = null

		User user = springSecurityService.currentUser

		def file = request.getFile("Filedata")

		try {
			uploadService.save(user.id, file)
			result = "ok"
		} catch (any) {
			log.error("User [${user.id}] can't upload a file", any)
			//todo explanation has to be displayed on UI
			result = "error"
		}

		render result
	}

	def deleteUpload = {
		def result = [:]

		User user = springSecurityService.currentUser
		Long uploadId = params.uploadId as Long

		try {
			uploadService.delete(user.id, uploadId)
			result = [ok: true]
		} catch (any) {
			log.error("User [${user}] can't delete upload", any)
			//todo explanation has to be displayed on UI
			result = [error: any.message]
		}

		render result as JSON
	}

	@Secured(['permitAll()'])
	def view = {
		request.setAttribute(ThisHoodConstant.ATTRIBUTE_ENABLE_CACHING, true)
		cache store:true, shared: true, neverExpires: true, auth: false

		def uid = params.id
		if (!uid) throw new IllegalArgumentException();

		def upload = uploadService.findByUid(uid)
		if (!upload) return error404();

		withCacheHeaders {
			lastModified {
				upload.dateCreated
			}
			etag {
				"${uid}:${upload.version}"
			}
			generate {
				def file = new File(grailsApplication.config.com.thishood.util.image.uploadBaseDir + grailsApplication.config.com.thishood.util.image.basePath, upload.uid)
				if (!file.isFile()) throw new IllegalArgumentException("Can't find [${upload.uid}] file to display in view")

				if (upload.contentType) response.contentType = upload.contentType
				if (upload.length) response.contentLength = (int) upload.length
				try {
					response.outputStream << new FileInputStream(file)
				} catch (any) {
					// this is workaround of TH-367
					// such exception can be raised if client closed connection
					// with reverse proxy we will have to return normal behavior
					log.warn("Error on streaming file [${file}]", any)
					return null
				}
			}
		}
	}

	def crop = {
		def user = springSecurityService.currentUser
		Long uploadId = params.uploadId as Long
		def upload = Upload.findByIdAndUser(uploadId, user)
		if (!upload) {
			throw new IllegalArgumentException("Can't find [${upload.uid}] upload.")
		}
        render view: "crop", model: [upload: upload]
	}

	def cropImage = {
		uploadService.crop(params)
		render "OK"
	}

}
