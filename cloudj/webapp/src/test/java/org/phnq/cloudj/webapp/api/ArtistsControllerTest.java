/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.phnq.cloudj.webapp.api;

import org.phnq.cloudj.app.Artist;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import javax.servlet.ServletException;
import java.io.IOException;
import org.phnq.core.webapp.test.MockWebApplication;
import org.springframework.test.context.ContextConfiguration;
import org.phnq.core.webapp.test.MockWebApplicationContextLoader;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.runner.RunWith;
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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:api-config.xml", loader = MockWebApplicationContextLoader.class)
@MockWebApplication(name = "artists-controller")
public class ArtistsControllerTest {

    @Autowired
    private DispatcherServlet servlet;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    
    @Test
    public void testGetKnownArtist() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/artists/Radiohead");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(200, response.getStatus());

        ObjectMapper mapper = new ObjectMapper();
        APIResponse resp = mapper.readValue(response.getContentAsString(), APIResponse.class);
        
        
        assertNotNull(resp);
        assertNotNull(resp.getData());
        
        Artist artistTO = mapper.convertValue(resp.getData(), Artist.class);
        
        assertEquals("Radiohead", artistTO.getName());
        assertNotNull(artistTO.getBio());
        assertNotNull(artistTO.getBioSummary());
    }

    @Test
    public void testGetKnownArtistAutocorrect() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/artists/Radiohed"); // missing "a"
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(200, response.getStatus());

        ObjectMapper mapper = new ObjectMapper();
        APIResponse resp = mapper.readValue(response.getContentAsString(), APIResponse.class);
        
        
        assertNotNull(resp);
        assertNotNull(resp.getData());
        
        Artist artistTO = mapper.convertValue(resp.getData(), Artist.class);
        
        assertEquals("Radiohead", artistTO.getName());
        assertNotNull(artistTO.getBio());
        assertNotNull(artistTO.getBioSummary());
    }
    
    @Test
    public void testGetKnownArtistWithAmpersand() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/artists/Crosby, Stills, Nash & Young");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(200, response.getStatus());

        ObjectMapper mapper = new ObjectMapper();
        APIResponse resp = mapper.readValue(response.getContentAsString(), APIResponse.class);
        
        
        assertNotNull(resp);
        assertNotNull(resp.getData());
        
        Artist artistTO = mapper.convertValue(resp.getData(), Artist.class);
        
        assertEquals("Crosby, Stills, Nash & Young", artistTO.getName());
        assertNotNull(artistTO.getBio());
        assertNotNull(artistTO.getBioSummary());
    }
    
    @Test
    public void testNonExistentArtist() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/artists/jalksdjkaljsdlkajsdlkajsldjaklsd");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(404, response.getStatus());

        ObjectMapper mapper = new ObjectMapper();
        APIResponse resp = mapper.readValue(response.getContentAsString(), APIResponse.class);
        
        assertNotNull(resp);
        assertNull(resp.getData());
    }

}
