package org.phnq.cloudj.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.phnq.clients.lastfm.LastFMException;
import org.phnq.clients.lastfm.LastFMSimilarTrack;
import org.phnq.clients.lastfm.LastFMTrack;
import org.phnq.clients.rdio.RdioClient;
import org.phnq.clients.rdio.RdioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pgostovic
 */
public class Track implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(Track.class);
    private String id = UUID.randomUUID().toString();
    private boolean validated = false;
    private String name;
    private String artist;
    private String image = null;
    private String imageSmall;
    private String imageMedium;
    private String imageLarge;
    private String imageExtraLarge;
    private String imageMega;
    private String playbackKey = null;
    private List<Track> similarTracks = null;
    private boolean fallbackToRdioImage = true;

    public Track() {
    }

    public Track(String name, String artist) {
        this.name = name;
        this.artist = artist;
    }

    Track(LastFMTrack track, boolean fallbackToRdioImage) {
        this.fallbackToRdioImage = fallbackToRdioImage;
        this.updateWithTrack(track);
    }

    Track(LastFMTrack track) {
        this(track, true);
    }

    Track(Track trackTO) {
        this.name = trackTO.getName();
        this.artist = trackTO.getArtist();
    }

    private void updateWithTrack(LastFMTrack track) {
        this.validated = true;
        this.name = track.getName();
        this.artist = track.getArtist().getName();
        this.imageSmall = track.getImageSmall();
        this.imageMedium = track.getImageMedium();
        this.imageLarge = track.getImageLarge();
        this.imageExtraLarge = track.getImageExtraLarge();
        this.imageMega = track.getImageMega();
        
        if (fallbackToRdioImage && this.getImageMega() == null) {
            logger.debug("No Last.fm images for " + name + "/" + artist + " -- fetching Rdio image.");

            RdioTrack rdioTrack = RdioClient.getTrack(artist, name);
            if (rdioTrack != null) {
                this.playbackKey = rdioTrack.getKey();
                this.image = rdioTrack.getIcon();
            }
        }
    }

    public boolean isValidated() {
        return validated;
    }

    public void validate() throws CloudjException {
        if (!validated) {
            try {
                updateWithTrack(LastFMTrack.get(name, artist));
            } catch (LastFMException ex) {
                throw new CloudjException("Error validating track: " + name + " -- " + artist);
            }
        }
    }
    
    public String getId() {
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public String getImageSmall() {
        if (imageSmall == null || imageSmall.equals("")) {
            return getImage();
        } else {
            return imageSmall;
        }
    }

    public String getImageMedium() {
        if (imageMedium == null || imageMedium.equals("")) {
            return getImageSmall();
        } else {
            return imageMedium;
        }
    }

    public String getImageLarge() {
        if (imageLarge == null || imageLarge.equals("")) {
            return getImageMedium();
        } else {
            return imageLarge;
        }
    }

    public String getImageExtraLarge() {
        if (imageExtraLarge == null || imageExtraLarge.equals("")) {
            return getImageLarge();
        } else {
            return imageExtraLarge;
        }
    }

    public String getImageMega() {
        if (imageMega == null || imageMega.equals("")) {
            return getImageExtraLarge();
        } else {
            return imageMega;
        }
    }

    @JsonIgnore
    public String getPlaybackKey() {
        if (playbackKey == null) {
            RdioTrack rdioTrack = RdioClient.getTrack(artist, name);
            if (rdioTrack == null) {
                playbackKey = "";
            } else {
                playbackKey = rdioTrack.getKey();
            }
        }

        return playbackKey;
    }

    @JsonIgnore
    public List<Track> getSimilarTracks() {
        if (similarTracks == null) {
            similarTracks = new ArrayList<Track>();
            try {
                List<LastFMSimilarTrack> sims = LastFMTrack.getSimilar(name, artist);
                for (LastFMSimilarTrack sim : sims) {
                    if (!artist.equals(sim.getArtist().getName())) {
                        similarTracks.add(new Track(sim));
                    }
                }
            } catch (LastFMException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        return similarTracks;
    }

    @Override
    public boolean equals(Object o) {
        try {
            Track t = (Track) o;
            return name.equals(t.name) && artist.equals(t.artist);
        } catch (ClassCastException ex) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 23 * hash + (this.artist != null ? this.artist.hashCode() : 0);
        return hash;
    }
}
