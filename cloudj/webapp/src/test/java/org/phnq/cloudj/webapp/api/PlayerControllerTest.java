/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.phnq.cloudj.webapp.api;

import org.phnq.core.webapp.test.MockWebApplication;
import org.phnq.core.webapp.test.MockWebApplicationContextLoader;
import javax.servlet.ServletException;
import java.io.IOException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
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
@MockWebApplication(name = "player-controller")
public class PlayerControllerTest {

    @Autowired
    private DispatcherServlet servlet;

    public PlayerControllerTest() {
    }

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

    /**
     * Test of getPlaybackToken method, of class PlayerController.
     */
    @Test
    public void testGetPlaybackToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/player/playbackToken");
        request.addHeader("Referer", "http://localhost:8080/cloudj");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        String results = response.getContentAsString().trim();

        ObjectMapper mapper = new ObjectMapper();
        APIResponse resp = mapper.readValue(results, APIResponse.class);

        assertEquals(200, response.getStatus());
        assertNotNull(resp);
        assertNotNull(resp.getData());
    }

    @Test
    public void testGetPlaybackTokenNullReferer() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/player/playbackToken");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        String results = response.getContentAsString().trim();

        ObjectMapper mapper = new ObjectMapper();
        APIResponse resp = mapper.readValue(results, APIResponse.class);

        assertEquals(400, response.getStatus());
        assertNotNull(resp);
        assertNull(resp.getData());
    }
}
