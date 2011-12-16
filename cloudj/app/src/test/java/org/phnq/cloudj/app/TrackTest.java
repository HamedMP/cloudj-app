/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.phnq.cloudj.app;

import org.phnq.clients.rdio.RdioClient;
import org.phnq.clients.lastfm.LastFM;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pgostovic
 */
public class TrackTest {
    
    public TrackTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        LastFM.setApiKey("fb37d830e70f04592f0fea5624eee845");
        RdioClient.init("gmdwc3cbujdnxzahzffz5fec", "8a6zfbSwC6");
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testIsValidated() {

        Track t = new Track("Alex", "Girls");
        assertFalse("Track is not validated", t.isValidated());
        try {
            t.validate();
            assertTrue("Track is validated", t.isValidated());
        } catch (CloudjException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testValidate() throws Exception {
        Track t = new Track("Alex", "Girls");
        assertFalse("Track is not validated", t.isValidated());
        try {
            t.validate();
            assertTrue("Track is validated", t.isValidated());

            assertNotNull("imageMega has a value", t.getImageMega());
        } catch (CloudjException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testGetId() {
        Track t = new Track("Alex", "Girls");
        assertNotNull("id has a value", t.getId());
    }

    @Test
    public void testGetPlaybackKey() {
        Track t = new Track("Alex", "Girls");
        assertNotNull("id has a value", t.getPlaybackKey());
    }

    @Test
    public void testGetSimilarTracks() {
        Track t = new Track("Alex", "Girls");
        assertTrue("Has some similar tracks", t.getSimilarTracks().size() > 3);
    }
}
