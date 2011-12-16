package org.phnq.cloudj.webapp.api;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.phnq.cloudj.app.Track;

/**
 *
 * @author pgostovic
 */
public class TracksAPIResponse extends APIResponse {

    TracksAPIResponse()
    {
        super();
    }
    
    TracksAPIResponse(HttpServletResponse resp)
    {
        super(resp);
    }

    private List<Track> tracks;

    public List<Track> getTracks() {
        return tracks;
    }

    protected void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }
}
