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
public class LastFMTrack extends LastFMEntity implements Cacheable {

    private static final Logger logger = LoggerFactory.getLogger(LastFMTrack.class);

    public static List<LastFMTrack> search(String query) throws LastFMException {
        List<LastFMTrack> tracks = new ArrayList<LastFMTrack>();
        LastFM.Response res = LastFM.callMethod("track.search", "track", query, "limit", "10");
        if (res.isOk()) {
            NodeList trackNodes = res.getXPathNodes("/lfm/results/trackmatches/track");
            for (int i = 0; i < trackNodes.getLength(); i++) {
                tracks.add(new LastFMTrack((Element) trackNodes.item(i)));
            }
        }
        return tracks;
    }

    public static LastFMTrack get(String trackName, String artistName) throws LastFMException {
        String cacheKey = getCacheKey(artistName);
        LastFMTrack track = LastFM.getCache() == null ? null : (LastFMTrack) LastFM.getCache().get(cacheKey);
        if (track == null) {
            LastFM.Response res = LastFM.callMethod("track.getinfo", "artist", artistName, "track", trackName, "autocorrect", "1");
            if (res.isOk()) {
                track = new LastFMTrack((Element) res.getXPathNode("/lfm/track"));
                if (LastFM.getCache() != null) {
                    LastFM.getCache().put(track);
                }
            }
        }
        return track;
    }

    private static String getCacheKey(String trackName) {
        return LastFMTrack.class.getName() + ":" + trackName;
    }

    public static List<LastFMTrack> getHypedTracks() throws LastFMException {
        List<LastFMTrack> hypedTracks = new ArrayList<LastFMTrack>();
        LastFM.Response res = LastFM.callMethod("chart.gettoptracks");
        if (res.isOk()) {
            NodeList trackElmnts = res.getXPathNodes("/lfm/tracks/track");
            for (int i = 0; i < trackElmnts.getLength(); i++) {
                hypedTracks.add(new LastFMTrack((Element) trackElmnts.item(i)));
            }
        } else {
            logger.debug("Response was no ok");
        }
        return hypedTracks;
    }
    
    private String name;
    private LastFMArtist artist;
    private String url;
    private int duration;
    private int numPlays;
    private int numListeners;
    private String imageSmall;
    private String imageMedium;
    private String imageLarge;
    private String imageExtraLarge;
    private String imageMega;
    private List<LastFMSimilarTrack> similar;
    private List<LastFMTag> topTags;

    public LastFMTrack(Element element) {
        super(element);

        this.name = getXPathString("name");
        this.url = getXPathString("url");
        this.duration = getXPathInt("duration");
        this.numPlays = getXPathInt("playcount");
        this.numListeners = getXPathInt("listeners");

        String imgPathPrefix = getXPathNode("album") == null ? "" : "album/";
        this.imageSmall = getXPathString(imgPathPrefix + "image[@size='small']");
        this.imageMedium = getXPathString(imgPathPrefix + "image[@size='medium']");
        this.imageLarge = getXPathString(imgPathPrefix + "image[@size='large']");
        this.imageExtraLarge = getXPathString(imgPathPrefix + "image[@size='extralarge']");
        this.imageMega = getXPathString(imgPathPrefix + "image[@size='mega']");

        this.artist = new LastFMArtist((Element) getXPathNode("artist"));
        this.similar = null;

        NodeList tagNodes = getXPathNodes("toptags/tag");
        if (tagNodes != null) {
            this.topTags = new ArrayList<LastFMTag>(tagNodes.getLength());
            for (int i = 0; i < tagNodes.getLength(); i++) {
                this.topTags.add(new LastFMTag((Element) tagNodes.item(i)));
            }
        } else {
            this.topTags = new ArrayList<LastFMTag>(0);
        }
    }

    public int getDuration() {
        return duration;
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

    public String getName() {
        return name;
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

    public LastFMArtist getArtist() {
        return artist;
    }

    public List<LastFMSimilarTrack> getSimilar() {
        return getSimilar(10);
    }

    public List<LastFMSimilarTrack> getSimilar(int limit) {
        if (similar == null) {
            try {
                similar = getSimilar(getName(), artist.getName(), limit);
            } catch (LastFMException ex) {
                similar = new ArrayList<LastFMSimilarTrack>(0);
            }
        }

        return similar;
    }

    public static List<LastFMSimilarTrack> getSimilar(String trackName, String artistName) throws LastFMException {
        return getSimilar(trackName, artistName, 10);
    }

    public static List<LastFMSimilarTrack> getSimilar(String trackName, String artistName, int limit) throws LastFMException {
        List<LastFMSimilarTrack> similar;
        LastFM.Response res = LastFM.callMethod("track.getsimilar", "artist", artistName, "track", trackName, "limit", "" + limit);
        if (res.isOk()) {
            NodeList trackNodes = res.getXPathNodes("/lfm/similartracks/track");
            if (trackNodes != null) {
                similar = new ArrayList<LastFMSimilarTrack>(trackNodes.getLength());
                for (int i = 0; i < trackNodes.getLength(); i++) {
                    similar.add(new LastFMSimilarTrack((Element) trackNodes.item(i)));
                }
            } else {
                similar = new ArrayList<LastFMSimilarTrack>(0);
            }
        } else {
            similar = new ArrayList<LastFMSimilarTrack>(0);
        }
        return similar;
    }

    public List<LastFMTag> getTopTags() {
        return topTags;
    }

    public String getKey() {
        return getCacheKey(this.getName());
    }

    public long getCacheTime() {
        return 24 * 60 * 60 * 1000; // 1 day
    }
}
