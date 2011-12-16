package org.phnq.clients.lastfm;

import org.w3c.dom.Element;

/**
 *
 * @author pgostovic
 */
public class LastFMTag extends LastFMEntity {

    private String name;
    private String url;

    LastFMTag(Element element) {
        super(element);

        this.name = getXPathString("name");
        this.url = getXPathString("url");
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
