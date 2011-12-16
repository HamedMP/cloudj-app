package org.phnq.cloudj.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.phnq.clients.lastfm.LastFMArtist;
import org.phnq.clients.lastfm.LastFMException;
import org.phnq.clients.lastfm.LastFMTrack;
import org.phnq.clients.rdio.RdioClient;
import org.phnq.clients.rdio.RdioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cloudj {

    private static final Logger logger = LoggerFactory.getLogger(Cloudj.class);

    public static List<Track> getTracks() {
        return CloudjSession.getCurrent().getTracks();
    }

    public static void setTracks(List<Track> tracks) {
        clearTracks();
        List<Track> currentTracks = CloudjSession.getCurrent().getTracks();
        currentTracks.addAll(tracks);
    }

    public static Track getTrack(String id) throws CloudjException {
        for (Track track : getTracks()) {
            if (track.getId().equals(id)) {
                return track;
            }
        }

        return null;
    }

    public static void clearTracks() {
        CloudjSession.getCurrent().getTracks().clear();
        CloudjSession.getCurrent().getSimilarTrackPool().clear();
    }

    public static int getNumTracks() {
        return getTracks().size();
    }

    public static void appendTrack(Track track) {
        if (track != null) {
            List<Track> currentTracks = CloudjSession.getCurrent().getTracks();
            currentTracks.add(track);
        } else {
            logger.debug("appendTrack with track == null");
        }
    }

    public static void appendSimilarTrack() throws CloudjException {
        List<Track> currentTracks = CloudjSession.getCurrent().getTracks();
        if (currentTracks.isEmpty()) {
            throw new CloudjException("No seed tracks");
        }

        Set<Track> simTrackPool = CloudjSession.getCurrent().getSimilarTrackPool();
        List<Track> simTrackPoolCopy = new ArrayList<Track>(simTrackPool);
        simTrackPoolCopy.removeAll(currentTracks);

        boolean getFromPool = Math.random() < 0.8 && simTrackPoolCopy.size() > 10;

        Track nextTrackTO = null;
        if (getFromPool) {
            Collections.shuffle(simTrackPoolCopy);
            nextTrackTO = simTrackPoolCopy.get(0);
        } else {
            Track mostRecent = currentTracks.get(currentTracks.size() - 1);
            List<Track> sims = mostRecent.getSimilarTracks();
            if (sims.size() > 0) {
                List<Track> simsCopy = new ArrayList<Track>(sims);
                simsCopy.removeAll(currentTracks);
                Collections.shuffle(simsCopy);
                simTrackPool.addAll(sims);
                nextTrackTO = simsCopy.get(0);
            } else {
                try {
                    LastFMArtist lastFMArtist = LastFMArtist.get(mostRecent.getArtist());

                    List<Track> topTracks = new ArrayList<Track>();
                    for (LastFMTrack topLastFMTrack : lastFMArtist.getTopTracks()) {
                        Track track = new Track(topLastFMTrack);
                        if (!currentTracks.contains(track)) {
                            topTracks.add(track);
                        }
                    }

                    if (topTracks.size() > 0) {
                        Collections.shuffle(topTracks);
                        nextTrackTO = topTracks.get(0);
                    } else {
                        logger.warn("No similar tracks, no top tracks.");
                    }
                } catch (LastFMException ex) {
                    logger.debug(ex.getMessage(), ex);
                }
            }
        }

        if (nextTrackTO != null) {
            currentTracks.add(nextTrackTO);
        }
    }

    public static String getPlaybackToken(String domain) {
        return RdioClient.getPlaybackToken(domain);
    }

    public static String getPlaybackKeyForTrack(Track track) {
        RdioTrack rdioTrack = RdioClient.getTrack(track.getArtist(), track.getName());
        return rdioTrack == null ? null : rdioTrack.getKey();
    }

    public static Artist getArtist(String name) {
        if (name == null) {
            return null;
        }

        try {
            return new Artist(LastFMArtist.get(name));
        } catch (LastFMException ex) {
            logger.debug(ex.getMessage(), ex);
            return null;
        }
    }

    public static Track getRandomHypedTrack() {
        try {
            List<LastFMTrack> hypedTracks = LastFMTrack.getHypedTracks();
            Collections.shuffle(hypedTracks);
            return new Track(hypedTracks.get(0));
        } catch (LastFMException ex) {
            logger.debug(ex.getMessage(), ex);
            return null;
        }
    }

    public static List<Track> searchForTrack(String query) {
        List<Track> tracks = new ArrayList<Track>();

        if (query != null) {
            try {
                for (LastFMTrack lastFMTrack : LastFMTrack.search(query)) {
                    tracks.add(new Track(lastFMTrack, false));
                }
            } catch (LastFMException ex) {
                logger.debug(ex.getMessage(), ex);
            }
        }

        return tracks;
    }
}
