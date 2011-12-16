package org.phnq.core.util.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author pgostovic
 */
public class HttpUtil {

    public static String getString(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; en-us) AppleWebKit/533.18.1 (KHTML, like Gecko) Version/5.0.2 Safari/533.18.5");
        StringBuilder sb = new StringBuilder();
        char[] buff = new char[1024];
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        int b;
        while ((b = reader.read(buff)) != -1) {
            sb.append(buff, 0, b);
        }
        return sb.toString();
    }
}
