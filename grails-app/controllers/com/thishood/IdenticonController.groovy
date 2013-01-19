package com.thishood

import grails.plugins.springsecurity.Secured
import java.awt.image.RenderedImage

import javax.imageio.ImageIO
import com.thishood.identicon.NineBlockIdenticonRenderer2

@Secured(["permitAll()"])
/**
 * Simplified version of {@link com.thishood.identicon.IdenticonServlet} made specially for Grails with support of caching
 * @see com.thishood.identicon.IdenticonServlet
 */
class IdenticonController {

	def renderer = new NineBlockIdenticonRenderer2()
	def size = 100

    def view = {
		request.setAttribute(ThisHoodConstant.ATTRIBUTE_ENABLE_CACHING, true)
		cache store:true, shared: true, neverExpires: true, auth: false

		String codeParam = params.id
		if (!codeParam) throw new IllegalArgumentException("Code is not set")
		int code = codeParam.hashCode()

		withCacheHeaders {
			lastModified {
				new Date(ThisHoodConstant.TIMELINE)
			}
			etag {
				"${codeParam.encodeAsMD5()}"
			}
			generate {
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				RenderedImage image = renderer.render(code, size);
				ImageIO.write(image, "PNG", byteOut);
				byte[] imageBytes = byteOut.toByteArray();

				response.contentType = "image/png"
				response.contentLength = imageBytes.length
				response.outputStream << imageBytes
			}
		}
	}

}
