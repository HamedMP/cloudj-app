/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.phnq.phnqlets.pdk;

import java.util.HashMap;
import java.io.File;
import java.util.Map;
import java.util.Set;
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
public class PhnqletTest {

    public PhnqletTest() {
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
     * Test of setWebappPath method, of class Phnqlet.
     */
//    @Test
//    public void testSetWebappPath() {
//        System.out.println("setWebappPath");
//        String path = "";
//        Phnqlet.setWebappPath(path);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of get method, of class Phnqlet.
//     */
    @Test
    public void testGet() throws Exception {

        Phnqlet p = Phnqlet.get("x.HelloWorld");
        assertNotNull(p);
    }

    @Test
    public void testGetNonExistent() throws Exception {

        Phnqlet p = Phnqlet.get("jhdjkahkdjahkjsdhk");
        assertNull(p);
    }

    /**
     * Test of getType method, of class Phnqlet.
     */
    @Test
    public void testGetType() throws Exception {
        Phnqlet p = Phnqlet.get("x.HelloWorld");
        assertNotNull(p);
        
        assertEquals("x.HelloWorld", p.getType());
    }

    @Test
    public void testImplicitDependencies() throws Exception {
        Phnqlet p = Phnqlet.get("x.WidgetWithinWidget");
        assertNotNull(p);
        assertEquals(1, p.getDependentPhnqlets().size());
        assertTrue(p.getDependentPhnqlets().contains(Phnqlet.get("x.HelloWorld")));
    }

    @Test
    public void testExplicitDependencies() throws Exception {
        Phnqlet p = Phnqlet.get("x.Events");
        assertNotNull(p);
        assertEquals(1, p.getDependentPhnqlets().size());
        assertTrue(p.getDependentPhnqlets().contains(Phnqlet.get("phnq.notify")));
    }
    
    @Test
    public void testDependsOnImplicit() throws Exception {
        Phnqlet p = Phnqlet.get("x.WidgetWithinWidget");
        assertNotNull(p);
        assertTrue(p.dependsOn(Phnqlet.get("x.HelloWorld")));
    }

    @Test
    public void testDependsOnExplicit() throws Exception {
        Phnqlet p = Phnqlet.get("x.Events");
        assertNotNull(p);
        assertTrue(p.dependsOn(Phnqlet.get("phnq.notify")));
    }
    
    /**
     * Test of getJs method, of class Phnqlet.
     */
    @Test
    public void testGetJs() throws Exception {
        assertNull(Phnqlet.get("x.HelloWorld").getJs());
        assertNotNull(Phnqlet.get("x.Events").getJs());
    }

    /**
     * Test of getCss method, of class Phnqlet.
     */
    @Test
    public void testGetCss() throws Exception {
        assertNull(Phnqlet.get("phnq.notify").getCss());
        assertNotNull(Phnqlet.get("x.Events").getCss());
    }

    /**
     * Test of getMarkup method, of class Phnqlet.
     */
    @Test
    public void testGetMarkup() throws Exception {
        assertNull(Phnqlet.get("phnq.notify").getMarkup());
        assertNotNull(Phnqlet.get("x.Events").getMarkup());
    }

    /**
     * Test of getShellWrappedMarkup method, of class Phnqlet.
     */
    @Test
    public void testGetShellWrappedMarkup() throws Exception {
        Phnqlet p = Phnqlet.get("x.Events");
        assertNotNull(p);
        
        String shellMarkup = p.getShellWrappedMarkup(new HashMap<String,String>(), "null");
        assertTrue("initLoadResult match", shellMarkup.indexOf("window.initLoadResult = null;")!=-1);
        assertTrue("phnqlet placeholder match", shellMarkup.indexOf("<span class='phnqlet' type='x.Events'></span>")!=-1);
    }
}
