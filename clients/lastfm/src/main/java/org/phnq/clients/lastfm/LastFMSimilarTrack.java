package org.phnq.clients.lastfm;

import org.w3c.dom.Element;

/**
 *
 * @author pgostovic
 */
public class LastFMSimilarTrack extends LastFMTrack {

    private float match;

    public LastFMSimilarTrack(Element element) {
        super(element);
        this.match = getXPathFloat("match");
    }

    public float getMatch() {
        return match;
    }
}
