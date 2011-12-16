package org.phnq.core.webapp;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pgostovic
 */
public class ContextFilter implements Filter {

    private final static Logger logger = LoggerFactory.getLogger(ContextFilter.class);
    private static final String INIT_PARAM_CONTEXT_CLASS_NAME = "context-class";
    private static final String INIT_PARAM_CONTEXT_COOKIE_NAME = "context-cookie";
    private Class<Context> contextClass = Context.class;
    private String contextCookieName = null;

    public void init(FilterConfig config) throws ServletException {
        String contextClassName = config.getInitParameter(INIT_PARAM_CONTEXT_CLASS_NAME);
        if (contextClassName != null) {
            try {
                this.contextClass = (Class<Context>) Class.forName(contextClassName);
            } catch (ClassNotFoundException ex) {
                logger.error("Configured context class not found: " + contextClassName, ex);
            }
        }
        logger.info("ContextFilter Initialized -- context class: " + contextClass.getCanonicalName());

        this.contextCookieName = config.getInitParameter(INIT_PARAM_CONTEXT_COOKIE_NAME);
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
        try {
            HttpServletRequest httpReq = (HttpServletRequest) req;
            HttpServletResponse httpRes = (HttpServletResponse) res;

            Context context = contextClass.newInstance();
            context.setHttpServletRequest(httpReq);
            context.setHttpServletResponse(httpRes);

            if (this.contextCookieName != null) {
                String contextCookieVal = null;

                Cookie[] cookies = httpReq.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().equals(this.contextCookieName)) {
                            contextCookieVal = cookie.getValue();
                        }
                    }
                }

                if (contextCookieVal == null) {
                    contextCookieVal = UUID.randomUUID().toString();
                    httpRes.addCookie(new Cookie(contextCookieName, contextCookieVal));
                }

                context.setContextKey(contextCookieVal);
            }

            Context.setCurrent(context);
            filterChain.doFilter(req, res);
            context.destroy();
        } catch (InstantiationException ex) {
            logger.error("Could not instantiate context: " + contextClass.getCanonicalName(), ex);
        } catch (IllegalAccessException ex) {
            logger.error("Could not instantiate context: " + contextClass.getCanonicalName(), ex);
        } finally {
            Context.setCurrent(null);
        }
    }

    public void destroy() {
    }
}
