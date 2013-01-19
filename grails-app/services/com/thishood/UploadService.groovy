package com.thishood

import com.thishood.domain.Upload
import com.thishood.domain.User

import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.im4java.core.ConvertCmd
import org.im4java.core.IMOperation
import org.apache.tika.config.TikaConfig
import org.apache.tika.metadata.Metadata
import org.apache.tika.mime.MediaType
import org.apache.tika.mime.MimeType
import com.thishood.image.ImageSize

class UploadService {

	static transactional = true

	def grailsApplication
	def springSecurityService

	Upload findByUid(String uid) {
		//todo add permissions check
		Upload.findByUid(uid)
	}

	List<Upload> findAllByUser(Long userId) {
		User user = User.getOrFail(userId)

		Upload.findAllByUser(user)
	}

	def getMediaData(byte[] bytes) {
		def contentType = "application/octet-stream"
		def extension = "unknown"

		try {
			TikaConfig config = TikaConfig.defaultConfig
			MediaType mediaType = config.mimeRepository.detect(new ByteArrayInputStream(bytes), new Metadata())
			MimeType mimeType = config.mimeRepository.forName(mediaType.toString())
			extension = mimeType.extension
			contentType = mimeType.toString()
			//contentType = new Tika().detect(multipart.bytes)
		} catch (any) {
			log.error("Unable to detect content-type", any)
		}
		return [contentType, extension]
	}

	Upload save(Long userId, CommonsMultipartFile multipart) {
		User user = User.getOrFail(userId)
		String originalFilename = multipart.originalFilename

		def (contentType, extension) = getMediaData(multipart.bytes)

		String uid = UUIDGenerator.next() + extension

		Upload upload = new Upload(
			user: user,
			uid: uid,
			name: originalFilename,
			ext: Upload.NOT_NEEDED,
			contentType: contentType,
			length: 0L,
			dateCreated: new Date()
		)

		File diskFile = new File(getUploadDirectory(), upload.uid)
		multipart.transferTo(diskFile)
		upload.length = diskFile.length()

		upload.save(failOnError: true, flush: true)

		if (isImage(contentType)) resize(diskFile)

		upload
	}

	File getUploadDirectory() {
		new File(grailsApplication.config.com.thishood.util.image.uploadBaseDir, "/files/")
	}

	void delete(Long userId, Long uploadId) {
		User user = User.getOrFail(userId)
		Upload upload = Upload.findByIdAndUser(uploadId, user)
		if (!upload) throw new IllegalArgumentException("Invalid upload id [${uploadId}] or user id [${userId}]")

		// first try to delete a record
		upload.delete(failOnError: true, flush:true)

		// if we are here then looks like record successfully deleted
		File file = new File(getUploadDirectory(), upload.uid)
		if (file.isFile()) {
			if (!file.delete()) {
				log.error("Can't delete file [${file}]")
				//throw new IllegalArgumentException("Can't delete: " + upload.path)
			}
			if (isImage(upload.contentType)) {
				deleteImageFile(upload.uid)
			}
		} else {
			log.error("Unable to delete [${file}] because it's not a file")
		}
	}

	void deleteImageFile(String name) {
		deleteImageFile(name, ImageSize.THUMB)
		deleteImageFile(name, ImageSize.TINY)
		deleteImageFile(name, ImageSize.SMALL)
	}

	void deleteImageFile(String name, ImageSize size) {
		File file = new File(getUploadDirectory(), getImageName(name, size))

		if (file.isFile()) {
			if (!file.delete()) {
				log.error("Can't delete file [${file}]")
			}
		} else {
			log.error("Unable to delete [${file}] because it's not a file")
		}
	}

	boolean isImage(Long uploadId) {
		Upload upload = Upload.getOrFail(uploadId)

		isImage(upload.contentType)
	}

	boolean isImage(String contentType) {
		contentType.startsWith("image/")
	}

	private def resize(File file) {
		String filePath = file.absolutePath

		ConvertCmd cmd = new ConvertCmd()
		cmd.searchPath = grailsApplication.config.com.thishood.imageMagic.path

		//todo PARALELISE it!!!
		cmd.run(operationThumb(filePath))
		cmd.run(operationTiny(filePath))
		cmd.run(operationSmall(filePath))
	}

	private String getImageName(String name, ImageSize size) {
		String result
		switch (size) {
			case ImageSize.THUMB:
			case ImageSize.TINY:
			case ImageSize.SMALL:
				int lastDot = name.lastIndexOf('.')
				result = name.substring(0, lastDot) + "-"+size.name().toLowerCase() + name.substring(lastDot)
				break
			case ImageSize.ORIGINAL:
				result = name
				break
			default:
				throw new IllegalArgumentException("Unknown image size [${size}]")
		}
		result
	}

	String getImageUrl(Long uploadId, ImageSize size) {
		Upload upload = Upload.getOrFail(uploadId)

		grailsApplication.config.grails.serverURL + "/files/" + getImageName(upload.uid, size) + "?d=" + upload.dateCreated.getTime()
	}


	private def operationThumb = {String src ->
		IMOperation op = new IMOperation()
		op.addImage(src)
		op.thumbnail(ImageSize.THUMB.width, ImageSize.THUMB.height)
		op.strip()
		op.addImage(getImageName(src, ImageSize.THUMB))
	}

	private def operationTiny = {String src ->
		IMOperation op = new IMOperation()
		op.addImage(src)
		op.thumbnail(ImageSize.TINY.width, ImageSize.TINY.height)
		op.strip()
		op.addImage(getImageName(src, ImageSize.TINY))
	}

	private def operationSmall = {String src ->
		IMOperation op = new IMOperation()
		op.addImage(src)
		op.resize(ImageSize.SMALL.width, ImageSize.SMALL.height)
		op.strip()
		op.addImage(getImageName(src, ImageSize.SMALL))

	}

	def crop(params) {
		def upload = Upload.findByIdAndUser(params.uploadId, springSecurityService.currentUser)
		if (!upload) {
			def userId = springSecurityService.currentUser.id
			throw new IllegalArgumentException("Invalid upload id [${uploadId}] or user id [${userId}].")
		}
		if (!isImage(upload.contentType)) {
			throw new IllegalArgumentException("Upload id [${uploadId}] is not an image.")
		}
		def file = new File(grailsApplication.config.com.thishood.util.image.uploadBaseDir + grailsApplication.config.com.thishood.util.image.basePath, upload.uid)
		if (!file.isFile()) {
			throw new IllegalArgumentException("Can't find [${upload.uid}] file.")
		}
		upload.dateCreated = new Date()
		upload.save(failOnError: true, flush: true)
		String filePath = file.absolutePath
		ConvertCmd cmd = new ConvertCmd()
		cmd.searchPath = grailsApplication.config.com.thishood.imageMagic.path
		IMOperation op = new IMOperation()
		op.addImage(filePath)
		op.crop(params.w as Integer, params.h as Integer, params.x as Integer, params.y as Integer)
		op.addImage(filePath)
		cmd.run(op)
		resize(file)
	}

}