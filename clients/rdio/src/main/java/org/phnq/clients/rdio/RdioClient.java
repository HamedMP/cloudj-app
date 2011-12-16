package org.phnq.clients.rdio;

import com.rdio.simple.Parameters;
import com.rdio.simple.Rdio;
import flexjson.JSONDeserializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pgostovic
 */
public class RdioClient {

    private static final Logger logger = LoggerFactory.getLogger(RdioClient.class);
    private static Rdio rdio = null;

    public static void init(String key, String secret) {
        rdio = new Rdio(key, secret);
    }

    private static Rdio getRdio() {
        if (rdio == null) {
            throw new RuntimeException("RdioClient not initialized. Use RdioClient.init().");
        }

        return rdio;
    }

    public static String getPlaybackToken(String domain) {
        if (domain == null) {
            logger.debug("domain was null");
            return null;
        }

        try
        {
            String json = call("getPlaybackToken", Parameters.build("domain", domain));
            HashMap m = (HashMap) new JSONDeserializer().deserialize(json);
            return (String) m.get("result");
        }
        catch(IOException ex)
        {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    public static RdioTrack getTrack(String artistName, String trackName) {
        for (Object obj : search(artistName + " " + trackName, "Track", 10)) {
            RdioTrack track = (RdioTrack) obj;
            if (track.getIsAvailable() && track.getArtist().equalsIgnoreCase(artistName) && track.getName().equalsIgnoreCase(trackName)) {
                return track;
            }
        }
        return null;
    }

    public static List search(String query, String types, int count) {
        try
        {
            String json = call("search", Parameters.build("query", query).and("types", types).and("never_or", "true").and("count", "" + count));

            Map m = (Map) ((Map) new JSONDeserializer().deserialize(json)).get("result");
            List<Map> resultMaps = (List) m.get("results");

            List results = new ArrayList(resultMaps.size());

            for (Map result : resultMaps) {
                Object wrapper = wrapByType(result);
                if (wrapper != null) {
                    results.add(wrapper);
                }
            }

            return results;
        }
        catch(IOException ex)
        {
            logger.error(ex.getMessage(), ex);
            return new ArrayList(0);
        }
    }

    private static String call(String method, Parameters params) throws IOException {
        long start = System.currentTimeMillis();

        String respBody = getRdio().call(method, params);

        logger.debug("" + (System.currentTimeMillis() - start) + "ms - " + method + "?" + params.toPercentEncoded());

        return respBody;
    }

    private static Object wrapByType(Map result) {
        String type = (String) result.get("type");
        if (type.equals("t")) {
            return new RdioTrack(result);
        }

        return null;
    }
}
