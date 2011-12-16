package org.phnq.core.util.aggregation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextAggregator {

    private static final Logger logger = LoggerFactory.getLogger(TextAggregator.class);
    private List<TextComponent> components;
    private boolean trimLines = true;
    private File baseDir = null;

    public TextAggregator() {
        this(null);
    }

    public TextAggregator(File baseDir) {
        this.components = new ArrayList<TextComponent>(20);
        this.baseDir = baseDir;
    }

    public boolean isEmpty() {
        return this.components.isEmpty();
    }

    public boolean getTrimLines() {
        return this.trimLines;
    }

    public void setTrimLines(boolean trimLines) {
        this.trimLines = trimLines;
    }

    public void append(File f) {
        if (f.exists()) {
            this.components.add(new FileTextComponent(f, this.baseDir));
        } else {
            logger.warn("appending file that does not exist: {}", f.getPath());
        }
    }

    public void append(String s) {
        this.components.add(new StringTextComponent(s));
    }

    public String getAggregateKey() {
        StringBuilder sb = new StringBuilder();

        for (TextComponent comp : components) {
            sb.append(comp.getKey());
            sb.append(",");
        }

        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            byte[] bytes = md5.digest(sb.toString().getBytes("UTF-8"));
            return String.format("%x", new BigInteger(1, bytes));
        } catch (Exception ex) {
            return null;
        }
    }

    public String getAggregateText() {
        return getAggregateText(null);
    }

    public String getAggregateText(LineFilter lineFilter) {
        StringBuilder sb = new StringBuilder();

        try {
            for (TextComponent comp : components) {
                BufferedReader in = new BufferedReader(comp.getContentReader());
                String line;
                while ((line = in.readLine()) != null) {
                    if (lineFilter != null) {
                        line = lineFilter.doFilter(line, comp.getContext());
                    }

                    if (trimLines) {
                        line = line.trim();
                    }

                    if (line.length() > 0) {
                        sb.append(line);
                        sb.append("\n");
                    }
                }
            }
        } catch (IOException ex) {
            logger.error(ex.toString(), ex);
        }

        return sb.toString();
    }

    public static String collapsePath(String path) {
        Deque<String> comps = new ArrayDeque<String>();
        for (String comp : path.split("/")) {
            if (comp.equals("..")) {
                comps.pop();
            } else {
                if (!comp.equals(".")) {
                    comps.push(comp);
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Iterator<String> it = comps.descendingIterator(); it.hasNext();) {
            String comp = it.next();
            sb.append(comp);
            sb.append("/");
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    public static interface LineFilter {

        public String doFilter(String line, String context);
    }

    private interface TextComponent {

        public String getContext();

        public String getKey();

        public Reader getContentReader();
    }

    private class FileTextComponent implements TextComponent {

        private File file;
        private String context;

        public FileTextComponent(File file, File baseDir) {
            this.file = file;
            this.context = "";

            if (baseDir != null) {
                String basePath = baseDir.getPath();
                String fileDirPath = file.getParent();
                if (fileDirPath.startsWith(fileDirPath)) {
                    this.context = fileDirPath.substring(basePath.length());
                }
            }
        }

        @Override
        public String getContext() {
            return this.context;
        }

        @Override
        public String getKey() {
            if (this.file.exists()) {
                return this.file.getPath() + "|" + this.file.lastModified();
            } else {
                logger.warn("appending file that does not exist: {}", this.file.getPath());
                return "";
            }
        }

        @Override
        public Reader getContentReader() {
            try {
                return new BufferedReader(new FileReader(this.file));
            } catch (FileNotFoundException ex) {
                logger.warn("appending file that does not exist: {}", this.file.getPath());
                return new StringReader("");
            }
        }
    }

    private class StringTextComponent implements TextComponent {

        private String str;

        public StringTextComponent(String str) {
            this.str = str;
        }

        @Override
        public String getContext() {
            return "";
        }

        @Override
        public String getKey() {
            return this.str;
        }

        @Override
        public Reader getContentReader() {
            return new StringReader(this.str);
        }
    }
}
