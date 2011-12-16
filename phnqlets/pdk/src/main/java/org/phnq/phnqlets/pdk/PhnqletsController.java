package org.phnq.phnqlets.pdk;

import flexjson.JSONSerializer;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.phnq.core.util.aggregation.TextAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.View;

/**
 *
 * @author pgostovic
 */
@Controller
public class PhnqletsController {

    private static final Logger logger = LoggerFactory.getLogger(PhnqletsController.class);
    private static byte[] bootBytes = null;
    private static byte[] bootBytesGzip = null;
    private static String bootAggKey = "";
    private static boolean phnqletsInitialized = false;
    private String[] bootFiles = {
        "jquery-check.js",
        "ext/jquery-1.6.4.min.js",
        "boot.js",
        "tmplt.js",
        "phnqlets.js"
    };

    private static void ensurePhnqletsInitialized(HttpServletRequest req) {
        if (!phnqletsInitialized) {
            Phnqlet.setWebappPath(req.getServletContext().getRealPath(""));
            phnqletsInitialized = true;
        }
    }

    @RequestMapping(value = "/boot", method = {
        RequestMethod.GET, RequestMethod.HEAD
    })
    public View getJavaScriptBootstrap() {
        return new View() {

            @Override
            public String getContentType() {
                return "text/javascript";
            }

            @Override
            public void render(Map<String, ?> map, HttpServletRequest req, HttpServletResponse res) throws Exception {
                TextAggregator textAgg = new TextAggregator();

                textAgg.append("window.phnq = window.phnq || {}; phnq.serverContextBase = \"" + getServerContextBase(req) + "\";");

                // Add the component js file resources to textAgg
                char[] buf = new char[1024];
                ClassLoader classLoader = getClass().getClassLoader();
                for (String jsFile : bootFiles) {
                    logger.info(":: " + jsFile);
                    StringBuilder jsBuf = new StringBuilder();
                    Reader in = new InputStreamReader(classLoader.getResourceAsStream("js/" + jsFile));
                    int b;
                    while ((b = in.read(buf)) != -1) {
                        jsBuf.append(buf, 0, b);
                    }
                    textAgg.append(jsBuf.toString());
                    in.close();
                }
                String key = textAgg.getAggregateKey();

                if (!key.equals(bootAggKey)) {
                    bootAggKey = key;
                    bootBytes = textAgg.getAggregateText().getBytes("UTF-8");
                }

                String ifNoneMatch = req.getHeader("If-None-Match");
                if (ifNoneMatch != null && ifNoneMatch.equals(bootAggKey)) {
                    res.setStatus(304);
                } else {
                    res.setContentType(getContentType());
                    res.setHeader("ETag", bootAggKey);
                    res.setHeader("Pragma", "cache");
                    res.setHeader("Cache-Control", "max-age=7200"); // 2 hours
                    res.setDateHeader("Expires", System.currentTimeMillis() + 7200000); // 2 hours from now
                    res.getOutputStream().write(bootBytes);
                }
            }
        };
    }

    @RequestMapping(value = "/load", method = {
        RequestMethod.GET, RequestMethod.HEAD
    })
    public @ResponseBody
    Map<String, Object> loadPhnqlets(@RequestParam("types") String types, @RequestHeader(value = "Referer", required = false) String referer, @RequestParam(value = "log", required = false) String logParam, HttpServletRequest req, HttpServletResponse res) {
        ensurePhnqletsInitialized(req);

        boolean devMode = logParam != null;

        boolean sameDomain = false;
        if (referer != null) {
            try {
                URI refererUri = new URI(referer);
                sameDomain = refererUri.getHost().equals(req.getServerName());
            } catch (URISyntaxException ex) {
                logger.warn("Error parsing referer uri", ex);
            }
        }

        String reqUri = req.getRequestURI();
        StringBuilder phnqletsBaseBuf = new StringBuilder();
        if (!sameDomain) {
            phnqletsBaseBuf.append(req.getScheme()).append("://").append(req.getServerName()).append(":").append(req.getServerPort());
        }

        phnqletsBaseBuf.append(req.getContextPath()).append("/phnqlets");

        String phnqletsBase = phnqletsBaseBuf.toString();

        // Fill out dependencies...
        Set<String> typesToLoad;
        List<Phnqlet> phnqletsToLoad;
        List<String> loadedTypes;

        String[] typeArray = types.split(",");
        boolean isStale = false;

        do {
            typesToLoad = new HashSet<String>();
            phnqletsToLoad = new ArrayList<Phnqlet>();
            loadedTypes = new ArrayList<String>();

            for (String type : typeArray) {
                try {
                    Phnqlet phnqlet = Phnqlet.get(type.trim());
                    if (phnqlet != null) {
                        for (Phnqlet dep : phnqlet.getDependentPhnqlets()) {
                            if (!typesToLoad.contains(dep.getType())) {
                                phnqletsToLoad.add(dep);
                                typesToLoad.add(dep.getType());
                            }
                        }

                        if (!typesToLoad.contains(phnqlet.getType())) {
                            phnqletsToLoad.add(phnqlet);
                            typesToLoad.add(phnqlet.getType());
                        }
                    } else {
                        logger.warn("Attempting to load non-existent phnqlet: " + type);
                    }
                } catch (Exception ex) {
                    logger.error("Error getting phnqlet", ex);
                }
            }

            isStale = false;
            for (Phnqlet phnqlet : phnqletsToLoad) {
                if (phnqlet.isStale()) {
                    logger.info("Found stale phnqlet: " + phnqlet.getType());
                    isStale = true;
                    try {
                        logger.info("Re-scanning phnqlets...");
                        Phnqlet.scan();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
                }
            }
        } while (isStale);

        TextAggregator jsAgg = new TextAggregator();
        jsAgg.setTrimLines(!devMode);

        TextAggregator cssAgg = new TextAggregator();
        cssAgg.setTrimLines(!devMode);

        Map<String, String> markupMap = new HashMap<String, String>();

        // Sort the phnqletsToLoad list so dependencies are loaded first
        List<Phnqlet> phnqletsToLoadSorted = new ArrayList<Phnqlet>(phnqletsToLoad.size());
        for (Phnqlet phnqlet : phnqletsToLoad) {
            boolean added = false;
            for (int i = 0; !added && i < phnqletsToLoadSorted.size(); i++) {
                Phnqlet sortedPhnqlet = phnqletsToLoadSorted.get(i);
                if (sortedPhnqlet.dependsOn(phnqlet)) {
                    phnqletsToLoadSorted.add(i, phnqlet);
                    added = true;
                }
            }
            if (!added) {
                phnqletsToLoadSorted.add(phnqlet);
            }
        }

        for (Phnqlet phnqlet : phnqletsToLoadSorted) {
            if (phnqlet.getJs() != null) {
                if (devMode) {
                    jsAgg.append("\n\n/********************** " + phnqlet.getType() + " **********************/\n");
                }

                jsAgg.append(phnqlet.getJs());
            }

            String css = phnqlet.getCss();
            if (css != null) {
                if (devMode) {
                    cssAgg.append("\n\n/********************** " + phnqlet.getType() + " **********************/\n");
                }

                cssAgg.append(css.replace("[PHNQLETS_BASE]", phnqletsBase));
            }

            String markup = phnqlet.getMarkup();
            if (markup != null) {
                markup = markup.replace("[PHNQLETS_BASE]", phnqletsBase);
                markupMap.put(phnqlet.getType(), markup);
            }

            loadedTypes.add(phnqlet.getType());
        }

        Map<String, Object> loadedPhnqlets = new HashMap<String, Object>();
        loadedPhnqlets.put("js", jsAgg.getAggregateText());
        loadedPhnqlets.put("css", cssAgg.getAggregateText());
        loadedPhnqlets.put("markup", markupMap);
        loadedPhnqlets.put("loadedTypes", loadedTypes);

        return loadedPhnqlets;
    }

    @RequestMapping(value = "/{type}/static/{path:.*}", method = {
        RequestMethod.GET, RequestMethod.HEAD
    })
    public void getPhnqletRelativeResource(@PathVariable String type, @PathVariable String path, HttpServletRequest req, HttpServletResponse res) {
        ensurePhnqletsInitialized(req);

        try {
            Phnqlet phnqlet = Phnqlet.get(type);
            if (phnqlet != null) {
                File resFile = new File(phnqlet.getDir() + File.separator + "static" + File.separator + path.replace('/', File.separatorChar));
                if (resFile.exists()) {
                    res.setHeader("ETag", new Long(resFile.lastModified()).toString());
                    res.setHeader("Pragma", "cache");
                    res.setHeader("Cache-Control", "max-age=7200"); // 2 hours
                    res.setDateHeader("Expires", System.currentTimeMillis() + 7200000); // 2 hours from now

                    OutputStream out = res.getOutputStream();
                    InputStream in = new BufferedInputStream(new FileInputStream(resFile));
                    byte[] buf = new byte[4096];
                    int b;
                    while ((b = in.read(buf)) != -1) {
                        out.write(buf, 0, b);
                    }
                    in.close();
                } else {
                    logger.debug("NO resFile: " + resFile.getPath());
                    res.setStatus(404);
                }
            } else {
                logger.debug("NO PHNQLET: " + type);
                res.setStatus(404);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @RequestMapping(value = "/{type:.*}", method = {
        RequestMethod.GET, RequestMethod.HEAD
    })
    public void getPhnqletInShell(@PathVariable String type, @RequestParam(value = "log", required = false) String logParam, HttpServletRequest req, HttpServletResponse res) {
        ensurePhnqletsInitialized(req);

        Map<String, Object> loadResult = this.loadPhnqlets(type, null, logParam, req, res);

        JSONSerializer jsonSer = new JSONSerializer();

        try {
            Phnqlet phnqlet = Phnqlet.get(type);
            if (phnqlet != null) {

                Map<String, String[]> mParams = req.getParameterMap();
                Map<String, String> params = new HashMap<String, String>();
                for (String k : mParams.keySet()) {
                    String[] mParam = mParams.get(k);
                    if (mParam.length > 0) {
                        params.put(k, mParam[0]);
                    }
                }
                params.put("serverContextBase", getServerContextBase(req));
                res.getWriter().print(phnqlet.getShellWrappedMarkup(params, jsonSer.deepSerialize(loadResult)));
            } else {
                logger.debug("NO PHNQLET: " + type);
                res.setStatus(404);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private String getServerContextBase(HttpServletRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append(req.getScheme()).append("://");
        sb.append(req.getServerName()).append(":");
        sb.append(req.getServerPort());
        sb.append(req.getServletContext().getContextPath());
        return sb.toString();
    }

    @RequestMapping(value = "/proxy", method = {
        RequestMethod.GET, RequestMethod.HEAD
    })
    public void getProxyResponse(@RequestParam("proxyDestUrl") String proxyDestUrl, HttpServletRequest req, HttpServletResponse res) {
        StringBuilder sb = new StringBuilder();
        sb.append(proxyDestUrl);

        char delim = '?';
        for (Enumeration en = req.getParameterNames(); en.hasMoreElements();) {
            String k = (String) en.nextElement();
            if (!k.equals("proxyDestUrl")) {
                try {
                    sb.append(delim).append(k).append("=").append(URLEncoder.encode(req.getParameter(k), "UTF-8"));
                    delim = '&';
                } catch (UnsupportedEncodingException ex) {
                    logger.warn(ex.getMessage(), ex);
                }
            }
        }
        try {
            URL url = new URL(sb.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; en-us) AppleWebKit/533.18.1 (KHTML, like Gecko) Version/5.0.2 Safari/533.18.5");
            OutputStream out = res.getOutputStream();
            byte[] buf = new byte[1024];
            InputStream in = conn.getInputStream();

            res.setContentType(conn.getContentType());

            int b;
            while ((b = in.read(buf)) != -1) {
                out.write(buf, 0, b);
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            res.setStatus(400);
        }
    }
}
