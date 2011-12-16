package org.phnq.cloudj.webapp.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.phnq.cloudj.app.CloudjSession;
import org.phnq.cloudj.app.Track;
import org.phnq.core.webapp.test.MockWebApplication;
import org.phnq.core.webapp.test.MockWebApplicationContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.DispatcherServlet;

/**
 *
 * @author pgostovic
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:api-config.xml", loader = MockWebApplicationContextLoader.class)
@MockWebApplication(name = "tracks-controller")
public class TracksControllerTest {

    @Autowired
    private DispatcherServlet servlet;

    public TracksControllerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        CloudjSession.setTestMode(true);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        clearTracks();
    }

    @After
    public void tearDown() throws Exception {
        clearTracks();
    }

    /**
     * Test of getTracks method, of class TracksController.
     */
    @Test
    public void testGetTracks() throws Exception {
        assertEquals(0, getNumTracks());

        appendTrack(new Track("Alex", "Girls"));

        List<Track> tracks = getTracks();
        assertEquals(1, tracks.size());

        Track track = tracks.get(0);

        assertEquals("Girls", track.getArtist());
        assertEquals("Alex", track.getName());
    }

    /**
     * Test of setTracks method, of class TracksController.
     */
    @Test
    public void testSetTracks() throws Exception {
        assertEquals(0, getNumTracks());
        
        Track[] tracks = {new Track("Alex", "Girls"), new Track("Soothe Me", "Yuck")};

        ObjectMapper mapper = new ObjectMapper();

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/tracks");
        request.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        request.addHeader("Content-Type", "application/json; charset=UTF-8");

        request.setContent(mapper.writeValueAsBytes(tracks));

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(200, response.getStatus());

        TracksAPIResponse resp = mapper.readValue(response.getContentAsString(), TracksAPIResponse.class);

        assertNotNull(resp);
        assertNull(resp.getData());
        assertTrue(resp.getTracks().size() > 0);
        
        Track t0 = resp.getTracks().get(0);
        assertEquals("Alex", t0.getName());
        assertEquals("Girls", t0.getArtist());

        Track t1 = resp.getTracks().get(1);
        assertEquals("Soothe Me", t1.getName());
        assertEquals("Yuck", t1.getArtist());
    }
    
    /**
     * Test of searchTracks method, of class TracksController.
     */
    @Test
    public void testSearchTracks() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/tracks/search/Radiohead");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(200, response.getStatus());
        ObjectMapper mapper = new ObjectMapper();
        APIResponse resp = mapper.readValue(response.getContentAsString(), APIResponse.class);

        assertNotNull(resp);
        assertNotNull(resp.getData());

        List<Track> tracks = mapper.convertValue(resp.getData(), new TypeReference<List<Track>>() {
        });

        assertNotNull(tracks);
        assertTrue("Some search results returned", tracks.size() > 0);
    }

    /**
     * Test of setTracksSortorder method, of class TracksController.
     */
    @Test
    public void testSetTracksSortorder() throws Exception {
        appendTrack(new Track("Soothe Me", "Yuck"));
        appendTrack(new Track("Start A War", "The National"));
        appendTrack(new Track("My Chariot", "The Depreciation Guild"));
        appendTrack(new Track("Stay Alive", "The Pains Of Being Pure At Heart"));

        List<String> ids = new ArrayList<String>();
        for(Track t : getTracks())
        {
            ids.add(t.getId());
        }
        
        Collections.shuffle(ids);
        
        ObjectMapper mapper = new ObjectMapper();

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/tracks/sortOrder");
        request.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        request.addHeader("Content-Type", "application/json; charset=UTF-8");

        request.setContent(mapper.writeValueAsBytes(ids.toArray(new String[ids.size()])));
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(200, response.getStatus());

        APIResponse resp = mapper.readValue(response.getContentAsString(), APIResponse.class);

        assertNotNull(resp);
        assertNull(resp.getData());
        
        List<Track> tracks = getTracks();
        for(int i=0; i<tracks.size(); i++)
        {
            assertEquals(ids.get(i), tracks.get(i).getId());
        }
    }

    /**
     * Test of getTrack method, of class TracksController.
     */
    @Test
    public void testGetTrack() throws Exception{
        appendTrack(new Track("Soothe Me", "Yuck"));
        
        Track track = getTracks().get(0);
        
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/tracks/"+track.getId());
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(200, response.getStatus());
        ObjectMapper mapper = new ObjectMapper();
        APIResponse resp = mapper.readValue(response.getContentAsString(), APIResponse.class);

        assertNotNull(resp);
        assertNotNull(resp.getData());

        Track t = mapper.convertValue(resp.getData(), Track.class);

        assertNotNull(t);
        assertEquals("Soothe Me", t.getName());
        assertEquals("Yuck", t.getArtist());
    }

    /**
     * Test of getTrack method, of class TracksController.
     */
    @Test
    public void testGetNonExistentTrack() throws Exception{
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/tracks/kjskjskjskjksjkssksjksjksjskjsksj");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(404, response.getStatus());
    }
    /**
     * Test of getPlaybackKey method, of class TracksController.
     */
    @Test
    public void testGetPlaybackKey() throws Exception {
        appendTrack(new Track("Soothe Me", "Yuck"));
        
        Track track = getTracks().get(0);
        
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/tracks/"+track.getId()+"/playbackKey");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(200, response.getStatus());
        ObjectMapper mapper = new ObjectMapper();
        APIResponse resp = mapper.readValue(response.getContentAsString(), APIResponse.class);

        assertNotNull(resp);
        assertNotNull(resp.getData());

        String playbackKey = mapper.convertValue(resp.getData(), String.class);

        assertNotNull(playbackKey);
    }

    /**
     * Test of clearTracks method, of class TracksController.
     */
    @Test
    public void testClearTracks() throws Exception {
        appendTrack(new Track("Soothe Me", "Yuck"));
        appendTrack(new Track("Start A War", "The National"));
        appendTrack(new Track("My Chariot", "The Depreciation Guild"));
        appendTrack(new Track("Stay Alive", "The Pains Of Being Pure At Heart"));
        assertEquals(4, getNumTracks());
        clearTracks();
        assertEquals(0, getNumTracks());
    }

    /**
     * Test of appendSimilarTrack method, of class TracksController.
     */
    @Test
    public void testAppendNonExistentTrack() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/tracks/append");
        request.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        request.addHeader("Content-Type", "application/json; charset=UTF-8");
        request.setContent(mapper.writeValueAsBytes(new Track("kjhkasdhkjasdhkahdsjk", "akhaskdhaksdhkajsdhkajdhk")));

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);
        
        assertEquals(400, response.getStatus());
    }

    /**
     * Test of appendSimilarTrack method, of class TracksController.
     */
    @Test
    public void testAppendSimilarTrack() throws Exception {
        appendTrack(new Track("Soothe Me", "Yuck"));
        
        ObjectMapper mapper = new ObjectMapper();

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/tracks/appendSimilar");
        request.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        request.addHeader("Content-Type", "application/json; charset=UTF-8");

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(200, response.getStatus());

        TracksAPIResponse resp = mapper.readValue(response.getContentAsString(), TracksAPIResponse.class);

        assertNotNull(resp);
        assertNull(resp.getData());
        assertEquals(2, resp.getTracks().size());
    }

    /**
     * Test of seedRandomHypedTrack method, of class TracksController.
     */
    @Test
    public void testSeedRandomHypedTrack() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/tracks/seedRandomHyped");
        request.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        request.addHeader("Content-Type", "application/json; charset=UTF-8");

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(200, response.getStatus());

        TracksAPIResponse resp = mapper.readValue(response.getContentAsString(), TracksAPIResponse.class);

        assertNotNull(resp);
        assertNull(resp.getData());
        assertEquals(1, resp.getTracks().size());
    }

    /**
     * Test of getRandomHypedTrack method, of class TracksController.
     */
    @Test
    public void testGetRandomHypedTrack() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/tracks/randomHyped");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(200, response.getStatus());
        ObjectMapper mapper = new ObjectMapper();
        APIResponse resp = mapper.readValue(response.getContentAsString(), APIResponse.class);

        assertNotNull(resp);
        assertNotNull(resp.getData());

        Track track = mapper.convertValue(resp.getData(), Track.class);

        assertNotNull(track);
    }

    private void clearTracks() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/tracks/clear");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(200, response.getStatus());

        ObjectMapper mapper = new ObjectMapper();
        TracksAPIResponse resp = mapper.readValue(response.getContentAsString(), TracksAPIResponse.class);

        assertNotNull(resp);
        assertNull(resp.getData());
        assertEquals(0, resp.getTracks().size());
    }

    private List<Track> getTracks() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/tracks");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(200, response.getStatus());
        ObjectMapper mapper = new ObjectMapper();
        APIResponse resp = mapper.readValue(response.getContentAsString(), APIResponse.class);

        assertNotNull(resp);
        assertNotNull(resp.getData());

        List<Track> tracks = mapper.convertValue(resp.getData(), new TypeReference<List<Track>>() {
        });

        assertNotNull(tracks);

        return tracks;
    }

    private int getNumTracks() throws ServletException, IOException {
        return getTracks().size();
    }

    private List<Track> appendTrack(Track track) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/tracks/append");
        request.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        request.addHeader("Content-Type", "application/json; charset=UTF-8");

        request.setContent(mapper.writeValueAsBytes(track));

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(200, response.getStatus());

        TracksAPIResponse resp = mapper.readValue(response.getContentAsString(), TracksAPIResponse.class);

        assertNotNull(resp);
        assertNotNull(resp.getData());
        assertTrue(resp.getTracks().size() > 0);

        return resp.getTracks();
    }
}
