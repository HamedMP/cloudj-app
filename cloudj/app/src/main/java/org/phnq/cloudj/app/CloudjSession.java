package org.phnq.cloudj.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.phnq.core.util.cache.Cache;
import org.phnq.core.util.cache.Cacheable;

/**
 *
 * @author pgostovic
 */
public class CloudjSession implements Cacheable {

    public static Cache sessionCache = new Cache();
    
    private static boolean testMode = false;
    private static CloudjSession testSession = null;
    
    public static void setTestMode(boolean testMode)
    {
        CloudjSession.testMode = testMode;
    }

    public static CloudjSession getCurrent() {
        if(testMode)
        {
            if(testSession == null)
                testSession = new CloudjSession(UUID.randomUUID().toString());
            
            return testSession;
        }
        else
        {
            String contextKey = CloudjContext.getCurrent().getContextKey();

            CloudjSession session = (CloudjSession) sessionCache.get(contextKey);
            if (session == null) {
                session = new CloudjSession(contextKey);
                sessionCache.put(session);
            }
            return session;
        }
    }
    private String cacheKey;
    private List<Track> tracks;
    private Set<Track> similarTrackPool;

    private CloudjSession(String cacheKey) {
        this.cacheKey = cacheKey;
        this.tracks = new ArrayList<Track>();
        this.similarTrackPool = new HashSet<Track>();
    }

    public String getKey() {
        return cacheKey;
    }

    public long getCacheTime() {
        return 60 * 60 * 1000; // 1 hour
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public Set<Track> getSimilarTrackPool() {
        return similarTrackPool;
    }
}
