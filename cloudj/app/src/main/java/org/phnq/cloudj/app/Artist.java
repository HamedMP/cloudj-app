package org.phnq.cloudj.app;

import org.phnq.clients.lastfm.LastFMArtist;

/**
 *
 * @author pgostovic
 */
public class Artist {

    private String name;
    private String bio;
    private String bioSummary;
    private String imageSmall;
    private String imageMedium;
    private String imageLarge;
    private String imageExtraLarge;
    private String imageMega;
    
    public Artist()
    {
    }

    Artist(LastFMArtist artist) {
        this.name = artist.getName();
        this.bio = artist.getBioContent();
        this.bioSummary = artist.getBioSummary();
        this.imageSmall = artist.getImageSmall();
        this.imageMedium = artist.getImageMedium();
        this.imageLarge = artist.getImageLarge();
        this.imageExtraLarge = artist.getImageExtraLarge();
        this.imageMega = artist.getImageMega();
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getBioSummary() {
        return bioSummary;
    }

    public void setBioSummary(String bioSummary) {
        this.bioSummary = bioSummary;
    }

    public String getImageExtraLarge() {
        return imageExtraLarge;
    }

    public void setImageExtraLarge(String imageExtraLarge) {
        this.imageExtraLarge = imageExtraLarge;
    }

    public String getImageLarge() {
        return imageLarge;
    }

    public void setImageLarge(String imageLarge) {
        this.imageLarge = imageLarge;
    }

    public String getImageMedium() {
        return imageMedium;
    }

    public void setImageMedium(String imageMedium) {
        this.imageMedium = imageMedium;
    }

    public String getImageMega() {
        return imageMega;
    }

    public void setImageMega(String imageMega) {
        this.imageMega = imageMega;
    }

    public String getImageSmall() {
        return imageSmall;
    }

    public void setImageSmall(String imageSmall) {
        this.imageSmall = imageSmall;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
