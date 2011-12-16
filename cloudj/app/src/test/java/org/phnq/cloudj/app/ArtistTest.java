package org.phnq.cloudj.app;

import org.phnq.clients.rdio.RdioClient;
import org.phnq.clients.lastfm.LastFM;
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
public class ArtistTest {
    
    public ArtistTest() {
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
    public void testGetBio() {
        Artist a = Cloudj.getArtist("Radiohead");
        assertNotNull(a);
        assertNotNull(a.getBio());
        assertNotNull(a.getBioSummary());
        assertTrue(a.getBio().length() > a.getBioSummary().length());
    }

    @Test
    public void testGetName() {
        Artist a = Cloudj.getArtist("Radiohead");
        assertEquals("Radiohead", a.getName());
    }

    @Test
    public void testAutocorrect() {
        Artist a = Cloudj.getArtist("Radiohed");
        assertEquals("Radiohead", a.getName());
    }
}
