package org.phnq.phnqlets.pdk;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.phnq.core.util.io.FileWatcher;
import org.phnq.core.util.text.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author pgostovic
 */
public class Phnqlet {

    private static final String PHNQLETS_BASE_PATH = "/WEB-INF/phnqlets".replace('/', File.separatorChar);
    public static final String PHNQLETS_NS = "http://phnq.org/phnqlets";
    private static final Logger logger = LoggerFactory.getLogger(Phnqlet.class);
    private static String webappPath = null;
    private static Map<String, Phnqlet> phnqlets = null;
    private static Template phnqletShellTemplate;

    static {
        try {
            StringBuilder sb = new StringBuilder();

            BufferedReader br = new BufferedReader(new InputStreamReader(Phnqlet.class.getResourceAsStream("/xhtml/phnqlet_shell.xhtml")));
            char[] buf = new char[256];
            int b;
            while ((b = br.read(buf)) != -1) {
                sb.append(buf, 0, b);
            }
            phnqletShellTemplate = new Template(sb.toString());
        } catch (Exception ex) {
            phnqletShellTemplate = null;
            logger.warn("Unable to create phnqletShellTemplate", ex);
        }
    }

    public static void setWebappPath(String path) {
        webappPath = path;
    }

    public static Phnqlet get(String type) throws PhnqletsException {
        if (phnqlets == null) {
            scan();
        }

        Phnqlet phnqlet = phnqlets.get(type);

        // Maybe a new phnqlet was added. Scan one time...
        if (phnqlet == null) {
            scan();
            phnqlet = phnqlets.get(type);
        }

        return phnqlet;
    }

    public static void scan() throws PhnqletsException {
        logger.info("Scanning for phnqlets...");

        phnqlets = new HashMap<String, Phnqlet>();

        // Add loose phnqlets installed in the webapp...
        if (webappPath != null) {
            addPhnqlets(new File(webappPath, PHNQLETS_BASE_PATH));
        } else {
            addPhnqlets(new File("target/classes/phnqlets".replace('/', File.separatorChar)));
        }

        // Add phnqlets embedded in jar files...
        try {
            File embedTmp = File.createTempFile("phnq", null);
            embedTmp.delete();
            embedTmp.mkdir();
            embedTmp.deleteOnExit();

            copyEmbeddedPhnqletsToTmpDir(embedTmp);

            addPhnqlets(embedTmp);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private static void copyEmbeddedPhnqletsToTmpDir(File dir) {
        byte[] buf = new byte[4096];

        /*
         * Go through the entries in all jar files on the classpath. Entries
         * whose path starts with "phnqlets/" are copied to a temp directory.
         * That temp directory is then scanned for phnqlets.
         */
        if (Phnqlet.class.getClassLoader() instanceof URLClassLoader) {
            URLClassLoader classLoader = (URLClassLoader) Phnqlet.class.getClassLoader();
            for (URL url : classLoader.getURLs()) {
                try {
                    File libFile = new File(url.toURI());
                    if (libFile.isFile() && libFile.getName().endsWith(".jar")) {
                        try {
                            JarFile jarFile = new JarFile(libFile);
                            for (Enumeration<JarEntry> en = jarFile.entries(); en.hasMoreElements();) {
                                JarEntry jarEntry = en.nextElement();
                                if (jarEntry.getName().startsWith("phnqlets/")) {
                                    File f = new File(dir, jarEntry.getName());
                                    if (jarEntry.isDirectory()) {
                                        f.mkdirs();
                                    } else {
                                        FileOutputStream out = new FileOutputStream(f);
                                        InputStream in = jarFile.getInputStream(jarEntry);

                                        int b;
                                        while ((b = in.read(buf)) != -1) {
                                            out.write(buf, 0, b);
                                        }

                                        out.close();
                                        in.close();
                                    }
                                }
                            }
                        } catch (IOException ex) {
                            logger.error(ex.getMessage(), ex);
                        }
                    }
                } catch (URISyntaxException ex) {
                    logger.warn(ex.getMessage(), ex);
                }
            }
        }
    }

    private static void addPhnqlets(File dir) {
        if (!dir.exists()) {
            logger.warn("Phnqlets directory does not exist: " + dir.getAbsolutePath());
            return;
        }

        if (!dir.isDirectory()) {
            logger.warn("Phnqlets directory is not a directory: " + dir.getAbsolutePath());
            return;
        }
        
        Phnqlet phnqlet = new Phnqlet(dir);
        if (phnqlet.isPhnqlet()) {
            String type = phnqlet.getType();
            if (phnqlets.containsKey(type)) {
                Phnqlet preExisting = phnqlets.get(type);
                logger.warn("Duplicate phnqlet type detected: " + dir.getPath() + " -- type already exists: " + preExisting.dir.getPath());
            } else {
                logger.info("Discovered phnqlet: " + type);
                phnqlets.put(type, phnqlet);
            }
        }

        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                addPhnqlets(f);
            }
        }
    }
    private String type;
    private String title;
    private String description;
    private String keywords;
    private File dir;
    private FileWatcher fileWatcher;
    private String js;
    private String css;
    private String markup;
    private Set<String> dependentTypes;
    private Set<Phnqlet> dependentPhnqlets;
    private Template dataUrlTmplt;

    private Phnqlet(File dir) {
        this.type = dir.getName();
        this.dir = dir;
        this.fileWatcher = new FileWatcher();
        this.js = null;
        this.css = null;
        this.markup = null;
        this.dependentTypes = new HashSet<String>();
        this.dependentPhnqlets = null;
        this.dataUrlTmplt = null;

        for (File f : dir.listFiles()) {
            if (f.isFile()) {
                fileWatcher.addFile(f);

                String filename = f.getName();
                int lastDotIdx = filename.lastIndexOf(".");
                if (lastDotIdx != -1) {
                    String ext = filename.substring(lastDotIdx + 1).trim();
                    if (ext.equals("js")) {
                        this.js = fileToString(f, false);
                    } else if (ext.equals("css")) {
                        this.css = fileToString(f, true);
                    } else if (ext.equals("xhtml")) {
                        this.markup = fileToString(f, false);
                    }
                }
            }
        }

        processJs();
        processCss();
        processMarkup();
    }

    private String fileToString(File f, boolean isCss) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line.replaceAll("_TYPE_", isCss ? type.replace(".", "\\\\.") : type)).append("\n");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
        return sb.toString();
    }

    private void processJs() {
        if (this.js == null) {
            return;
        }

        Pattern p = Pattern.compile("\\s*/\\*\\s*#require\\s*([^\\s]*)\\s*\\*/\\s*");

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("phnq.exec(\"").append(type).append("\", function(log, $, jQuery){\n");
            String line;
            BufferedReader reader = new BufferedReader(new StringReader(this.js));
            while ((line = reader.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.find()) {
                    this.dependentTypes.add(m.group(1));
                } else {
                    sb.append(line).append("\n");
                }

            }
            sb.append("if(typeof(phnqlet) == \"object\") {");
            sb.append("phnq.phnqlets.create(\"").append(type).append("\", phnqlet);");
            sb.append("}");
            sb.append("\n});\n");

            this.js = sb.toString();
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private void processCss() {
        if (this.css == null) {
            return;
        }

        Pattern p = Pattern.compile("url\\(\"?([^\\)\"]*)\"?\\)");
        Matcher m = p.matcher(css);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "url(" + fixPath(m.group(1)) + ")");
        }
        m.appendTail(sb);

        this.css = sb.toString();
    }

    private void processMarkup() {
        if (this.markup == null) {
            return;
        }

        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
            docBuilderFactory.setIgnoringComments(true);
            docBuilderFactory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new ByteArrayInputStream(this.markup.getBytes("UTF-8")));

            Element docElmnt = doc.getDocumentElement();

            // title for shell
            this.title = docElmnt.getAttributeNS(PHNQLETS_NS, "title");
            if (this.title == null || this.title.trim().length() == 0) {
                this.title = type;
            }
            docElmnt.removeAttributeNS(PHNQLETS_NS, "title");

            // description for shell
            this.description = docElmnt.getAttributeNS(PHNQLETS_NS, "description");
            docElmnt.removeAttributeNS(PHNQLETS_NS, "description");

            // keywords for shell
            this.keywords = docElmnt.getAttributeNS(PHNQLETS_NS, "keywords");
            docElmnt.removeAttributeNS(PHNQLETS_NS, "keywords");
            
            // Data binding
            String dataAttr = docElmnt.getAttributeNS(PHNQLETS_NS, "data");
            if (dataAttr.trim().length() > 0) {
                dataUrlTmplt = new Template(dataAttr);
            }

            // Add phnqlet type as css class
            String classAttr = (type + " " + docElmnt.getAttribute("class")).trim();
            docElmnt.setAttribute("class", classAttr);

            // Make the element hidden at first
            String styleAttr = "display:none;" + docElmnt.getAttribute("style");
            docElmnt.setAttribute("style", styleAttr);

            // Replace phnqlet placeholders and determine dependencies.
            processEmbeddedPhnqlets(doc);

            // Find embedded static resource references and fix paths.
            processEmbeddedStaticResources(doc);

            // Serialize processed document.
            StringWriter sw = new StringWriter();
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(sw);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            serializer.transform(domSource, streamResult);

            this.markup = sw.toString().replaceAll("xmlns:[^=]*=\"[^\"]*\"", "");
        } catch (Exception ex) {
            logger.error("Error processing markup: " + this.getType(), ex);
        }
    }

    private void processEmbeddedPhnqlets(Document doc) {
        NodeList nodes = doc.getElementsByTagNameNS(PHNQLETS_NS, "phnqlet");

        List<Element> phnqletElmnts = new ArrayList<Element>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            Element phnqletElmnt = (Element) nodes.item(i);
            phnqletElmnts.add(phnqletElmnt);
        }

        for (Element phnqletElmnt : phnqletElmnts) {
            String depType = phnqletElmnt.getAttribute("type");
            this.dependentTypes.add(depType);

            Element placeholderElmnt = doc.createElement("span");
            placeholderElmnt.setAttribute("class", "phnqlet");
            NamedNodeMap attrs = phnqletElmnt.getAttributes();
            for (int j = 0; j < attrs.getLength(); j++) {
                Attr attr = (Attr) attrs.item(j);
                placeholderElmnt.setAttribute(attr.getName(), attr.getValue());
            }

            List<Node> childNodesList = new ArrayList<Node>();
            while (phnqletElmnt.hasChildNodes()) {
                childNodesList.add(phnqletElmnt.removeChild(phnqletElmnt.getFirstChild()));
            }

            phnqletElmnt.getParentNode().replaceChild(placeholderElmnt, phnqletElmnt);

            for (Node n : childNodesList) {
                placeholderElmnt.appendChild(n);
            }
        }
    }

    private void processEmbeddedStaticResources(Document doc) {
        NodeList imgElmnts = doc.getElementsByTagName("img");
        for (int i = 0; i < imgElmnts.getLength(); i++) {
            Element imgElmnt = (Element) imgElmnts.item(i);
            imgElmnt.setAttribute("src", fixPath(imgElmnt.getAttribute("src")));
        }
    }

    private String fixPath(String path) {
        if (path.startsWith("http:") || path.startsWith("https:")) {
            return path;
        } else {
            return "[PHNQLETS_BASE]/" + type + "/" + path;
        }
    }

    private boolean isPhnqlet() {
        return this.js != null || this.css != null || this.markup != null;
    }

    public String getType() {
        return this.type;
    }

    public Set<Phnqlet> getDependentPhnqlets() {
        if (this.dependentPhnqlets == null) {
            dependentPhnqlets = new HashSet<Phnqlet>();
            for (String depType : dependentTypes) {
                try {
                    Phnqlet depPhnqlet = Phnqlet.get(depType);
                    if (depPhnqlet != null) {
                        dependentPhnqlets.add(depPhnqlet);
                        dependentPhnqlets.addAll(depPhnqlet.getDependentPhnqlets());
                    }
                } catch (PhnqletsException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
        return this.dependentPhnqlets;
    }

    public boolean dependsOn(Phnqlet w) {
        return getDependentPhnqlets().contains(w);
    }

    public File getDir() {
        return dir;
    }

    public String getJs() {
        return js;
    }

    public String getCss() {
        return css;
    }

    public String getMarkup() {
        return markup;
    }

    public boolean isStale() {
        return this.fileWatcher.hasModificationsSinceLastMark();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        try {
            Phnqlet p = (Phnqlet) o;
            return getType().equals(p.getType());
        } catch (ClassCastException ex) {
            return false;
        }
    }

    String getShellWrappedMarkup(Map<String, String> params, String loadResult) {
        params.put("type", this.getType());

        StringBuilder sb = new StringBuilder();
        sb.append("<span class='phnqlet'");
        for (String k : params.keySet()) {
            sb.append(" ");
            sb.append(k);
            sb.append("='");
            sb.append(params.get(k));
            sb.append("'");
        }
        sb.append("></span>");

        Map<String, String> shellParams = new HashMap<String, String>(params);
        shellParams.put("title", this.title);
        shellParams.put("description", this.description);
        shellParams.put("keywords", this.keywords);
        shellParams.put("body", sb.toString());
        shellParams.put("loadResult", loadResult);

        return phnqletShellTemplate.getParameterizedText(shellParams);
    }
}
