package org.phnq.cloudj.webapp.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.phnq.clients.lastfm.LastFM;
import org.phnq.clients.rdio.RdioClient;
import org.phnq.cloudj.app.CloudjSession;
import org.phnq.core.util.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author pgostovic
 */
public class APIContext implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(APIContext.class);
    private Cache cache = new Cache();
    private String sessionCachePath;
    private File sessionCacheFile;

    public void setLastfmAPIKey(String lastfmAPIKey) {
        LastFM.setApiKey(lastfmAPIKey);
    }

    public void setRdioAPIKey(String rdioAPIKey) {
        String[] rdioAPIKeyComps = rdioAPIKey.split(",");
        if (rdioAPIKeyComps.length == 2) {
            RdioClient.init(rdioAPIKeyComps[0].trim(), rdioAPIKeyComps[1].trim());
        } else {
            logger.warn("rdioAPIKey not set properly. Must be CONSUMER_KEY,CONSUMER_SECRET");
        }
    }

    public void setSessionCache(String sessionCachePath) {
        this.sessionCachePath = sessionCachePath;
    }

    public void cleanup() {
        if (sessionCacheFile != null) {
            try {
                sessionCacheFile.createNewFile();
                ObjectOutputStream dos = new ObjectOutputStream(new FileOutputStream(sessionCacheFile));
                CloudjSession.sessionCache.pruneStaleEntries();
                dos.writeObject(CloudjSession.sessionCache);
                dos.close();
            } catch (IOException ex) {
                logger.error("Unable to serialize the session cache", ex);
            }
        }
    }

    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        WebApplicationContext wac = (WebApplicationContext) ac;

        CloudjSession.sessionCache = cache;
        try {
            sessionCacheFile = new File(wac.getServletContext().getRealPath(sessionCachePath));
            if (sessionCacheFile.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(sessionCacheFile));
                CloudjSession.sessionCache = (Cache) ois.readObject();
                CloudjSession.sessionCache.pruneStaleEntries();
            }
        } catch (Exception ex) {
            logger.error("Unable to deserialize the session cache", ex);
        } finally {
            if (!sessionCacheFile.delete()) {
                logger.warn("Unable to delete corrupted session cache file. Please manually delete " + sessionCacheFile.getAbsolutePath());
            }

        }
    }
}
