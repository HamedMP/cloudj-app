package org.phnq.cloudj.webapp.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.phnq.cloudj.app.Cloudj;
import org.phnq.cloudj.app.Track;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author pgostovic
 */
@Controller
@RequestMapping("/tracks")
public class TracksController {

    @RequestMapping(value = "", method = {RequestMethod.GET, RequestMethod.HEAD})
    public @ResponseBody
    APIResponse getTracks(HttpServletResponse resp) {
        return new APIResponse(resp) {

            @Override
            protected Object getResponseData() throws Throwable {
                return Cloudj.getTracks();
            }
        };
    }

    @RequestMapping(value = "", method = {RequestMethod.POST})
    public @ResponseBody
    APIResponse setTracks(@RequestBody final Track[] tracks, HttpServletResponse resp) {
        return new TracksAPIResponse(resp) {

            @Override
            protected Object getResponseData() throws Throwable {
                Cloudj.setTracks(Arrays.asList(tracks));
                this.setTracks(Cloudj.getTracks());
                return null;
            }
        };
    }

    @RequestMapping(value = "/search/{query:.*}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public @ResponseBody
    APIResponse searchTracks(@PathVariable("query") final String query, HttpServletResponse resp) {
        return new APIResponse(resp) {

            @Override
            protected Object getResponseData() throws Throwable {
                return Cloudj.searchForTrack(query);
            }
        };
    }

    @RequestMapping(value = "/sortOrder", method = {RequestMethod.POST})
    public @ResponseBody
    APIResponse setTracksSortorder(@RequestBody final String[] trackIds, HttpServletResponse resp) {
        return new APIResponse(resp) {

            @Override
            protected Object getResponseData() throws Throwable {
                List<Track> tracks = Cloudj.getTracks();
                Map<String, Track> tracksById = new HashMap<String, Track>();
                for (Track track : tracks) {
                    tracksById.put(track.getId(), track);
                }
                tracks.clear();

                for (String trackId : trackIds) {
                    tracks.add(tracksById.get(trackId));
                }
                return null;
            }
        };
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public @ResponseBody
    APIResponse getTrack(@PathVariable("id") final String id, final HttpServletResponse resp) {
        return new APIResponse(resp) {

            @Override
            protected Object getResponseData() throws Throwable {
                Track track = Cloudj.getTrack(id);
                if(track == null)
                    resp.setStatus(404);
                
                return track;
            }
        };
    }

    @RequestMapping(value = "/{id}/playbackKey", method = {RequestMethod.GET, RequestMethod.HEAD})
    public @ResponseBody
    APIResponse getPlaybackKey(@PathVariable("id") final String id, HttpServletResponse resp) {
        return new APIResponse(resp) {

            @Override
            protected Object getResponseData() throws Throwable {
                return Cloudj.getTrack(id).getPlaybackKey();
            }
        };
    }

    @RequestMapping(value = "/clear", method = {RequestMethod.POST})
    public @ResponseBody
    APIResponse clearTracks(HttpServletResponse resp) {
        return new TracksAPIResponse(resp) {

            @Override
            protected Object getResponseData() throws Throwable {
                Cloudj.clearTracks();
                this.setTracks(Cloudj.getTracks());
                return null;
            }
        };
    }

    @RequestMapping(value = "/append", method = {RequestMethod.POST})
    public @ResponseBody
    APIResponse appendTrack(@RequestBody final Track track, @RequestParam(required=false) final Boolean reset, HttpServletResponse resp) {
        return new TracksAPIResponse(resp) {
            
            @Override
            protected Object getResponseData() throws Throwable {
                if (reset != null && reset) {
                    Cloudj.clearTracks();
                }
                
                track.validate();

                Cloudj.appendTrack(track);
                this.setTracks(Cloudj.getTracks());
                return track;
            }
        };
    }

    @RequestMapping(value = "/appendSimilar", method = {RequestMethod.POST})
    public @ResponseBody
    APIResponse appendSimilarTrack(HttpServletResponse resp) {
        return new TracksAPIResponse(resp) {

            @Override
            protected Object getResponseData() throws Throwable {
                Cloudj.appendSimilarTrack();
                this.setTracks(Cloudj.getTracks());
                return null;
            }
        };
    }

    @RequestMapping(value = "/seedRandomHyped", method = {RequestMethod.POST})
    public @ResponseBody
    APIResponse seedRandomHypedTrack(HttpServletResponse resp) {
        return new TracksAPIResponse(resp) {

            @Override
            protected Object getResponseData() throws Throwable {
                Cloudj.clearTracks();
                Cloudj.appendTrack(Cloudj.getRandomHypedTrack());
                this.setTracks(Cloudj.getTracks());
                return null;
            }
        };
    }

    @RequestMapping(value = "/randomHyped", method = {RequestMethod.GET, RequestMethod.HEAD})
    public @ResponseBody
    APIResponse getRandomHypedTrack(HttpServletResponse resp) {
        return new APIResponse(resp) {

            @Override
            protected Object getResponseData() throws Throwable {
                return Cloudj.getRandomHypedTrack();
            }
        };
    }
}
