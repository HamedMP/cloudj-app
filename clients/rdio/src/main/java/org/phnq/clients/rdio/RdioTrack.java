package org.phnq.clients.rdio;

import java.util.Map;

/**
 *
 * @author pgostovic
 */
public class RdioTrack {

    private String name;
    private String artist;
    private String album;
    private Integer duration;
    private String key;
    private String icon;
    private Boolean isAvailable;

    RdioTrack(Map m) {
        this.name = (String) m.get("name");
        this.artist = (String) m.get("artist");
        this.album = (String) m.get("album");
        this.duration = (Integer) m.get("duration");
        this.key = (String) m.get("key");
        this.icon = (String) m.get("icon");
        this.isAvailable = (Boolean) m.get("canStream");
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public Integer getDuration() {
        return duration;
    }

    public String getIcon() {
        return icon;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }
}
