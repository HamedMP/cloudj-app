package org.phnq.clients.lastfm;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.phnq.core.util.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author pgostovic
 */
public class LastFM {

    private static final String LAST_FM_ROOT_URL = "http://ws.audioscrobbler.com/2.0/";
    private static final Logger logger = LoggerFactory.getLogger(LastFM.class);
    private static String apiKey = null;
    private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    private static Cache cache = null;

    static {
        LastFM.dbf.setIgnoringElementContentWhitespace(true);
        LastFM.dbf.setCoalescing(true);
    }

    public static void setApiKey(String apiKey) {
        LastFM.apiKey = apiKey;
    }

    public static Cache getCache() {
        return cache;
    }

    public static void setCache(Cache cache) {
        LastFM.cache = cache;
    }
    
    static Response callMethod(String methodName, String... param) throws LastFMException {
        int paramLen = param.length;

        if (paramLen % 2 == 0) {
            Map<String, String> params = new HashMap<String, String>();

            for (int i = 0; i < paramLen; i += 2) {
                params.put(param[i], param[i + 1]);
            }

            return callMethod(methodName, params);
        } else {
            throw new LastFMException("The number of param names must equal the number of param values");
        }
    }

    static Response callMethod(String methodName, Map<String, String> params) throws LastFMException {
        StringBuilder sb = new StringBuilder();
        sb.append(LAST_FM_ROOT_URL).append("?");
        sb.append("method=").append(methodName);
        sb.append("&api_key=").append(LastFM.apiKey);

        if (params != null) {
            for (String k : params.keySet()) {
                String v = params.get(k);
                try {
                    sb.append("&").append(URLEncoder.encode(k, "UTF-8")).append("=").append(URLEncoder.encode(v, "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }

        try {
            long start = System.currentTimeMillis();

            String url = sb.toString();
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setUseCaches(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; en-us) AppleWebKit/533.18.1 (KHTML, like Gecko) Version/5.0.2 Safari/533.18.5");
            conn.connect();

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(conn.getInputStream());

            logger.debug("" + (System.currentTimeMillis() - start) + "ms - " + url);

            Response response = new Response(doc);

            return response;
        } catch (Exception ex) {
            throw new LastFMException(ex);
        }
    }

    static class Response {

        private Document doc;
        private XPath xpath;
        private Map<String, Object> cache;

        private Response(Document doc) {
            this.doc = doc;
            this.xpath = XPathFactory.newInstance().newXPath();
            this.cache = new HashMap<String, Object>();
        }

        public boolean isOk() {
            return getXPathString("/lfm/@status").equals("ok");
        }

        public boolean isFailed() {
            return getXPathString("/lfm/@status").equals("failed");
        }

        public String getXPathString(String path) {
            return (String) getXPath(path, XPathConstants.STRING);
        }

        public Node getXPathNode(String path) {
            return (Node) getXPath(path, XPathConstants.NODE);
        }

        public NodeList getXPathNodes(String path) {
            return (NodeList) getXPath(path, XPathConstants.NODESET);
        }

        private Object getXPath(String path, QName type) {
            try {
                XPathExpression expr = xpath.compile(path);
                return expr.evaluate(doc, type);
            } catch (XPathExpressionException ex) {
                return null;
            }
        }
    }
}
