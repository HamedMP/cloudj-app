package org.phnq.core.webapp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author pgostovic
 */
public class Context {

    private static final ThreadLocal<Context> contextThreadLocal = new ThreadLocal<Context>() {

        @Override
        protected Context initialValue() {
            return null;
        }
    };

    public static Context getCurrent() {
        return contextThreadLocal.get();
    }

    static void setCurrent(Context context) {
        if (context == null) {
            contextThreadLocal.remove();
        } else {
            contextThreadLocal.set(context);
        }
    }
    private String contextKey;
    private HttpServletRequest httpServletRequest;
    private HttpServletResponse httpServletResponse;

    public String getContextKey() {
        return contextKey;
    }

    void setContextKey(String contextKey) {
        this.contextKey = contextKey;
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }

    void setHttpServletResponse(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }

    public void destroy() {
    }
}
