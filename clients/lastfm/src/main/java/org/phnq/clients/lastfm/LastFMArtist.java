package org.phnq.clients.lastfm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.phnq.core.util.cache.Cacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author pgostovic
 */
public class LastFMArtist extends LastFMEntity implements Cacheable {

    private static final Logger logger = LoggerFactory.getLogger(LastFMArtist.class);

    public static LastFMArtist get(String artistName) throws LastFMException {
        String cacheKey = getCacheKey(artistName);
        LastFMArtist artist = LastFM.getCache() == null ? null : (LastFMArtist) LastFM.getCache().get(cacheKey);
        if (artist == null) {
            LastFM.Response res = LastFM.callMethod("artist.getinfo", "artist", artistName, "autocorrect", "1");
            if (res.isOk()) {
                artist = new LastFMArtist((Element) res.getXPathNode("/lfm/artist"));
                if (LastFM.getCache() != null) {
                    LastFM.getCache().put(artist);
                }
            }
        }
        return artist;
    }

    private static String getCacheKey(String artistName) {
        return LastFMArtist.class.getName() + ":" + artistName;
    }
    private String name;
    private String mbid;
    private String url;
    private String imageSmall;
    private String imageMedium;
    private String imageLarge;
    private String imageExtraLarge;
    private String imageMega;
    private int numListeners;
    private int numPlays;
    private List<LastFMTag> topTags;
    private String bioSummary;
    private String bioContent;
    private List<LastFMTrack> topTracks;

    LastFMArtist(Element artistElmnt) {
        super(artistElmnt);

        this.name = getXPathString("name");
        if (this.name == null || this.name.trim().length() == 0) {
            this.name = artistElmnt.getFirstChild().getNodeValue();
        } else {
            this.mbid = getXPathString("mbid");
            this.url = getXPathString("url");
            this.imageSmall = getXPathString("image[@size='small']");
            this.imageMedium = getXPathString("image[@size='medium']");
            this.imageLarge = getXPathString("image[@size='large']");
            this.imageExtraLarge = getXPathString("image[@size='extralarge']");
            this.imageMega = getXPathString("image[@size='mega']");
            this.numListeners = getXPathInt("stats/listeners");
            this.numPlays = getXPathInt("stats/playcount");
            this.bioSummary = getXPathString("bio/summary");
            this.bioContent = getXPathString("bio/content");

            NodeList tagNodes = getXPathNodes("tags/tag");
            if (tagNodes != null) {
                this.topTags = new ArrayList<LastFMTag>(tagNodes.getLength());
                for (int i = 0; i < tagNodes.getLength(); i++) {
                    this.topTags.add(new LastFMTag((Element) tagNodes.item(i)));
                }
            } else {
                this.topTags = new ArrayList<LastFMTag>(0);
            }

            this.topTracks = null;
        }
    }

    public String getName() {
        return name;
    }

    public String getImageExtraLarge() {
        return imageExtraLarge;
    }

    public String getImageLarge() {
        return imageLarge;
    }

    public String getImageMedium() {
        return imageMedium;
    }

    public String getImageMega() {
        return imageMega;
    }

    public String getImageSmall() {
        return imageSmall;
    }

    public String getMbid() {
        return mbid;
    }

    public int getNumListeners() {
        return numListeners;
    }

    public int getNumPlays() {
        return numPlays;
    }

    public String getUrl() {
        return url;
    }

    public List<LastFMTag> getTopTags() {
        return topTags;
    }

    public String getBioContent() {
        return bioContent;
    }

    public String getBioSummary() {
        return bioSummary;
    }

    public List<LastFMTrack> getTopTracks() {
        return getTopTracks(10);
    }

    public List<LastFMTrack> getTopTracks(int limit) {
        if (topTracks == null) {
            try {
                LastFM.Response res = LastFM.callMethod("artist.gettoptracks", "artist", name, "limit", "" + limit);
                if (res.isOk()) {
                    NodeList trackNodes = res.getXPathNodes("/lfm/toptracks/track");
                    if (trackNodes != null) {
                        topTracks = new ArrayList<LastFMTrack>(trackNodes.getLength());
                        for (int i = 0; i < trackNodes.getLength(); i++) {
                            topTracks.add(new LastFMTrack((Element) trackNodes.item(i)));
                        }
                    }
                } else {
                    topTracks = new ArrayList<LastFMTrack>(0);
                }
            } catch (LastFMException ex) {
                logger.debug(ex.getMessage(), ex);
                topTracks = new ArrayList<LastFMTrack>(0);
            }
        }

        return topTracks;
    }

    public String getKey() {
        return getCacheKey(this.getName());
    }

    public long getCacheTime() {
        return 24 * 60 * 60 * 1000; // 1 day
    }
}
