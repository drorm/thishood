package com.thishood.admin

import grails.plugins.springsecurity.Secured
import com.thishood.domain.Upload
import com.thishood.UploadService
import com.thishood.UUIDGenerator
import org.apache.commons.io.FileUtils

/**
 * This controller has to be extended in case when migration procedure should be added and managed by admin (for instance on deploy new version)
 */
@Secured(["hasRole('ROLE_ADMIN')"])
class AdminMigrationController {

	def uploadService
	
	/**
	 * TH-180
	 */
    def migrateUploads = {
		def result =[]

		//don't do in transaction because operating here file-by-file
		def uploads = Upload.findAll()
		uploads.each {upload ->
			if (upload.ext != Upload.NOT_NEEDED) {
				File oldFile = new File(uploadService.getUploadDirectory(), upload.uid)
				if (oldFile.isFile()) {
					def (contentType, extension) = uploadService.getMediaData(oldFile.readBytes())
					def uid = UUIDGenerator.next() + extension
					def newFile = new File(uploadService.getUploadDirectory(), uid)
					if (oldFile.renameTo(newFile)) {
						upload.uid = uid
						upload.contentType = contentType
						upload.name = upload.name + "." + upload.ext
						upload.ext = Upload.NOT_NEEDED
						upload.save(flush: true, failOnError: true)

						if (uploadService.isImage(upload.id)) uploadService.resize(newFile)
					} else {
						result.add("Unable to rename `${upload.uid}` (id=${upload.id})")
					}
				} else {
					result.add("Upload `${upload.uid}` references to non-existent file (id=${upload.id})")
				}
			} else {
				log.info("Skiping [${upload.uid}]")
			}
		}
		render view:'migrateUploads', model: [errors:result]
	}
}
