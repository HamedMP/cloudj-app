package org.phnq.phnqlets.pdk;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pgostovic
 */
public class JSONPFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(JSONPFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;

        String jsoncallback = request.getParameter("jsoncallback");

        /*
         * Existence of the jsoncallback parameter triggers JSONP functionality.
         * Requests without the jsoncallback parameter go through as usual.
         */
        if (jsoncallback != null) {

            /*
             * The request method is GET by default, but can be overridden by
             * setting the jsonmethod parameter.
             */
            final String method = request.getParameter("jsonmethod") == null ? "GET" : request.getParameter("jsonmethod").toUpperCase();

            /*
             * The body type can be either 'json' or 'url-enc'. The default
             * is 'json'.
             */
            final String contentType = request.getParameter("jsonbodytype");

            /*
             * The request body is specified by setting the 'jsonbody' parameter.
             */
            String body = request.getParameter("jsonbody");
            final byte[] bodyBytes = body == null ? null : body.getBytes("UTF-8");

            final Map<String, String> headers = new HashMap<String, String>();
            for (Enumeration<String> en = httpReq.getHeaderNames(); en.hasMoreElements();) {
                String name = en.nextElement();
                headers.put(name, httpReq.getHeader(name));
            }

            if (bodyBytes != null) {
                if (contentType == null || contentType.equals("json")) {
                    headers.put("content-type", "application/json; charset=utf-8");
                    headers.put("accept", "application/json, text/javascript, */*; q=0.01");
                } else if (contentType.equals("url-enc")) {
                    // TODO...
                }
                headers.put("content-length", String.valueOf(bodyBytes.length));
            }

            HttpServletRequestWrapper httpReqWrapper = new HttpServletRequestWrapper(httpReq) {

                @Override
                public String getMethod() {
                    return method;
                }

                @Override
                public BufferedReader getReader() throws IOException {
                    return new BufferedReader(new InputStreamReader(getInputStream()));
                }

                @Override
                public ServletInputStream getInputStream() throws IOException {

                    if (bodyBytes == null) {
                        return super.getInputStream();
                    } else {
                        final ByteArrayInputStream bais = new ByteArrayInputStream(bodyBytes);

                        ServletInputStream sis = new ServletInputStream() {

                            @Override
                            public int read() throws IOException {
                                return bais.read();
                            }

                            @Override
                            public void close() throws IOException {
                                super.close();
                                bais.close();
                            }
                        };

                        return sis;
                    }
                }

                @Override
                public String getHeader(String name) {
                    return headers.get(name);
                }

                @Override
                public Enumeration<String> getHeaderNames() {
                    final Iterator<String> names = headers.keySet().iterator();

                    return new Enumeration<String>() {

                        @Override
                        public boolean hasMoreElements() {
                            return names.hasNext();
                        }

                        @Override
                        public String nextElement() {
                            return names.next();
                        }
                    };
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    Vector<String> vals = new Vector<String>();
                    vals.add(headers.get(name));
                    return vals.elements();
                }
            };

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            final ServletOutputStream sos = new ServletOutputStream() {

                @Override
                public void write(int i) throws IOException {
                    baos.write(i);
                }

                @Override
                public void write(byte[] bytes) throws IOException {
                    baos.write(bytes);
                }

                @Override
                public void write(byte[] bytes, int i, int i1) throws IOException {
                    baos.write(bytes, i, i1);
                }
            };

            final PrintWriter writer = new PrintWriter(sos, true);

            HttpServletResponseWrapper httpRes = new HttpServletResponseWrapper((HttpServletResponse) response) {

                @Override
                public ServletOutputStream getOutputStream() throws IOException {
                    return sos;
                }

                @Override
                public PrintWriter getWriter() throws IOException {
                    return writer;
                }
            };

            writer.print(jsoncallback + "({data:");
            writer.flush();

            try {
                chain.doFilter(httpReqWrapper, httpRes);
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }

            writer.println(", status:" + httpRes.getStatus() + "});");

            // Force 200 status
            httpRes.setStatus(HttpServletResponse.SC_OK);
            httpRes.setContentType("text/javascript");

            response.getOutputStream().write(baos.toByteArray());
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }
}
