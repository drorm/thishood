package com.thishood.identicon;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * This servlet generates <i>identicon</i> (visual identifier) images ranging
 * from 16x16 to 64x64 in size.
 * <p/>
 * <h5>Supported Image Formats</h5>
 * <p>
 * Currently only PNG is supported because <code>javax.imageio</code> package
 * does not come with built-in GIF encoder and PNG is the only remaining
 * reasonable format.
 * </p>
 * <h5>Initialization Parameters:</h5>
 * <blockquote>
 * <dl>
 * <dt>salt</dt>
 * <dd>salt used to generate identicon code with. must be fairly long (Required)</dd>
 * <dt>cacheProvider</dt>
 * <dd>full class path to <code>IdenticonCache</code> implementation. (Optional)</dd>
 * </dl>
 * </blockquote>
 * <h5>Request ParametersP</h5>
 * <blockquote>
 * <dl>
 * <dt>code</dt>
 * <dd>identicon code to render.(Required)</dd>
 * <dt>size</dt>
 * <dd>identicon size in pixels. If missing, a 100x100 pixels identicon is
 * returned. Minimum size is 16 and maximum is 256. (Optional)</dd>
 * </dl>
 * </blockquote>
 *
 * see more on <a href='https://github.com/vmorarian/identicon'>identicon</a>
 */
public class IdenticonServlet extends HttpServlet {

    private static final long serialVersionUID = -3507466186902317988L;

    private static final Log log = LogFactory.getLog(IdenticonServlet.class);

    private static final String INIT_PARAM_VERSION = "version";
    private static final String INIT_PARAM_SALT = "salt";
    private static final String INIT_PARAM_CACHE_PROVIDER = "cacheProvider";

    private static final String PARAM_SIZE = "size";
    private static final String PARAM_CODE = "code";

    private static final String IMAGE_FORMAT = "PNG";
    private static final String IMAGE_MIMETYPE = "image/png";

    private static final int DEFAULT_IDENTICON_SIZE = 100;
    private static final int MINIMUM_IDENTICON_SIZE = 16;
    private static final int MAXIMUM_IDENTICON_SIZE = 256;


    private static final long DEFAULT_EXPIRES = TimeUnit.DAYS.toMillis(1);

    private int version = 1;

    private IdenticonRenderer renderer = new NineBlockIdenticonRenderer2();

    private IdenticonCache cache;

    /**
     * Should be in milliseconds
     */
    private long expires = DEFAULT_EXPIRES;

    private String salt;

    @Override
    public void init(ServletConfig cfg) throws ServletException {
        super.init(cfg);

        // Since identicons cache expiration is very long, version is
        // used in ETag to force identicons to be updated as needed.
        // Change version whenever rendering codes changes result in
        // visual changes.
        if (cfg.getInitParameter(INIT_PARAM_VERSION) != null) {
            this.version = Integer.parseInt(cfg.getInitParameter(INIT_PARAM_VERSION));
        }

        String saltParam = cfg.getInitParameter(INIT_PARAM_SALT);
        if (StringUtils.isNotBlank(saltParam)) {
            salt = saltParam;
        } else {
            throw new ServletException(INIT_PARAM_SALT + " init parameter not set");
        }

        String cacheProvider = cfg.getInitParameter(INIT_PARAM_CACHE_PROVIDER);
        if (cacheProvider != null) {
            try {
                Class cacheClass = Class.forName(cacheProvider);
                this.cache = (IdenticonCache) cacheClass.newInstance();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String codeParam = request.getParameter(PARAM_CODE);
        if (StringUtils.isEmpty(codeParam)) {
            throw new ServletException(PARAM_CODE + " parameter is not set");
        }
        int code = codeParam.hashCode();

        String sizeParam = request.getParameter(PARAM_SIZE);
        int size = getIdenticonSize(sizeParam);

        String computedETag = computeETag(code, size, version);
        String requestETag = request.getHeader("If-None-Match");

        if (requestETag != null && requestETag.equals(computedETag)) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            byte[] imageBytes = null;

            // retrieve image bytes from either cache or renderer
            if (cache == null || (imageBytes = cache.get(computedETag)) == null) {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                RenderedImage image = renderer.render(code, size);
                ImageIO.write(image, IMAGE_FORMAT, byteOut);
                imageBytes = byteOut.toByteArray();
                if (cache != null) {
                    cache.add(computedETag, imageBytes);
                }
            }

            // set ETag and, if code was provided, Expires header
            response.setHeader("ETag", computedETag);
            if (StringUtils.isNotEmpty(codeParam)) {
                long expires = System.currentTimeMillis() + this.expires;
                response.addDateHeader("Expires", expires);
            }

            // return image bytes to requester
            response.setContentType(IMAGE_MIMETYPE);
            response.setContentLength(imageBytes.length);
            response.getOutputStream().write(imageBytes);
        }
    }

    public static String computeETag(int code, int size, int version) {
        StringBuilder s = new StringBuilder("W/\"");
        s.append(Integer.toHexString(code));
        s.append('@');
        s.append(size);
        s.append('v');
        s.append(version);
        s.append('\"');
        return s.toString();
    }

    public static int getIdenticonSize(String param) {
        int size = DEFAULT_IDENTICON_SIZE;
        try {
            String sizeParam = param;
            if (sizeParam != null) {
                size = Integer.parseInt(sizeParam);
                if (size < MINIMUM_IDENTICON_SIZE) {
                    size = MINIMUM_IDENTICON_SIZE;
                } else if (size > MAXIMUM_IDENTICON_SIZE) {
                    size = MAXIMUM_IDENTICON_SIZE;
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return size;
    }


}
