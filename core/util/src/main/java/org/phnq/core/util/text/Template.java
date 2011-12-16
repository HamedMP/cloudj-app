package org.phnq.core.util.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pgostovic
 */
public class Template {

    private static final Logger logger = LoggerFactory.getLogger(Template.class);
    private List<String> comps;

    public Template(String text) {
        this.comps = new ArrayList<String>(10);
        parameterize(text);
    }

    public boolean isParameterizable() {
        return this.comps.size() > 1;
    }

    private void parameterize(String text) {
        Pattern p = Pattern.compile("_\\{([^}]*)}");
        Matcher m = p.matcher(text);

        int idx = 0;
        while (m.find()) {
            this.comps.add(text.substring(idx, m.start()));
            this.comps.add(m.group(1));
            idx = m.end();
        }
        this.comps.add(text.substring(idx));
    }

    public String getParameterizedText(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();

        boolean isParam = false;
        for (String comp : comps) {
            if (isParam) {
                String val = params.get(comp);
                sb.append(val == null ? "" : val);
            } else {
                sb.append(comp);
            }
            isParam = !isParam;
        }

        return sb.toString();
    }
}
