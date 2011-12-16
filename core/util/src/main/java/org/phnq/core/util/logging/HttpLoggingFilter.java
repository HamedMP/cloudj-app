package org.phnq.core.util.logging;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pgostovic
 */
public class HttpLoggingFilter implements Filter {

    Logger logger = LoggerFactory.getLogger(HttpLoggingFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;

        StringBuilder buf = new StringBuilder();
        buf.append(httpReq.getRemoteHost());
        buf.append("\n================ REQUEST ================");
        buf.append("\n> ").append(httpReq.getMethod()).append(" ").append(httpReq.getRequestURI());

        String qs = httpReq.getQueryString();
        if (qs != null && qs.length() > 0) {
            buf.append("?").append(qs);
        }

        for (Enumeration en = httpReq.getHeaderNames(); en.hasMoreElements();) {
            String name = (String) en.nextElement();
            for (Enumeration en2 = httpReq.getHeaders(name); en2.hasMoreElements();) {
                String val = (String) en2.nextElement();
                buf.append("\n> ").append(name).append(": ").append(val);
            }

        }


        chain.doFilter(request, response);

        HttpServletResponseWrapper httpResp = new HttpServletResponseWrapper((HttpServletResponse) response);
        buf.append("\n---------------- RESPONSE ---------------");
        buf.append("\n< ").append(httpResp.getStatus());
        for (String name : httpResp.getHeaderNames()) {
            for (String val : httpResp.getHeaders(name)) {
                buf.append("\n< ").append(name).append(": ").append(val);
            }
        }
        buf.append("\n=========================================");

        logger.info(buf.toString());
    }

    public void destroy() {
    }
}
