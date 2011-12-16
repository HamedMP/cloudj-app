package org.phnq.cloudj.app;

import junit.framework.TestCase;
import org.phnq.clients.lastfm.LastFM;
import org.phnq.clients.rdio.RdioClient;

/**
 *
 * @author pgostovic
 */
public class CloudjTest extends TestCase {

    public CloudjTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        CloudjSession.setTestMode(true);
        LastFM.setApiKey("fb37d830e70f04592f0fea5624eee845");
        RdioClient.init("gmdwc3cbujdnxzahzffz5fec", "8a6zfbSwC6");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGetTracks() {
        assertEquals(0, Cloudj.getTracks().size());
        Cloudj.appendTrack(new Track("Alex", "Girls"));
        assertEquals(1, Cloudj.getTracks().size());
    }
    
    public void testAppendTrack() {
        Cloudj.clearTracks();
        Cloudj.appendTrack(null);
        assertEquals(0, Cloudj.getTracks().size());
        Cloudj.appendTrack(new Track("Alex", "Girls"));
        assertEquals(1, Cloudj.getTracks().size());
    }


    public void testAppendSimilarTrack() {
        Cloudj.clearTracks();
        Cloudj.appendTrack(new Track("Alex", "Girls"));
        for(int i=0; i<10; i++)
        {
            try {
                Cloudj.appendSimilarTrack();
            } catch (CloudjException ex) {
                fail(ex.getMessage());
            }
        }
        assertEquals(11, Cloudj.getTracks().size());
    }

    /**
     * Test of getPlaybackToken method, of class Cloudj.
     */
    public void testGetPlaybackToken() {
        assertNotNull("Playback token is not null", Cloudj.getPlaybackToken("localhost"));
        assertNull("Playback token is not null", Cloudj.getPlaybackToken(null));
    }

    /**
     * Test of getPlaybackKeyForTrack method, of class Cloudj.
     */
    public void testGetPlaybackKeyForTrack() {
        Track t = new Track("Soothe Me", "Yuck");
        assertNotNull("Playback key for track is not null", Cloudj.getPlaybackKeyForTrack(t));
    }

    public void testGetPlaybackKeyForNonExistentTrack() {
        Track t = new Track("kjashkjasdhkashdjkad", "kjahsdjkahsdkaskahskjadkas");
        assertNull("Playback key for non-existent track is null", Cloudj.getPlaybackKeyForTrack(t));
    }

    /**
     * Test of getArtist method, of class Cloudj.
     */
    public void testGetArtist() {
        assertNotNull("artist is not null", Cloudj.getArtist("Radiohead"));
    }

    public void testGetNonExistentArtist() {
        assertNull("non-existent artist is null", Cloudj.getArtist("kasjkhasdjkakjsdhkjashdkjahsdjkash"));
        assertNull("null artist name makes artist null", Cloudj.getArtist(null));
    }

    /**
     * Test of getRandomHypedTrack method, of class Cloudj.
     */
    public void testGetRandomHypedTrack() {
        assertNotNull("random hyped track is not null", Cloudj.getRandomHypedTrack());
    }

    /**
     * Test of searchForTrack method, of class Cloudj.
     */
    public void testSearchForTrack() {

        boolean found = false;
        for (Track t : Cloudj.searchForTrack("Fake Plastic Trees")) {
            if (t.getArtist().equals("Radiohead") && t.getName().equals("Fake Plastic Trees")) {
                found = true;
            }
        }
        assertTrue(found);
        
        assertEquals(0, Cloudj.searchForTrack("djasdkjaskjdakjsdkjahdkahsdkjashkjdhaksjdhkajshdkjahsdkjahsk").size());
        
        assertEquals(0, Cloudj.searchForTrack(null).size());
    }
}
