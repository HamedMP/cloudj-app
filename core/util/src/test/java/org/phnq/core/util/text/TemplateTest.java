/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.phnq.core.util.text;

import java.util.HashMap;
import java.util.Map;
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
public class TemplateTest {
    
    public TemplateTest() {
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

    @Test
    public void testSingleParam() {
        Template template = new Template("Hello, my name is _{name}.");
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", "Bubba");
        assertEquals("Hello, my name is Bubba.", template.getParameterizedText(params));
    }

    @Test
    public void testSingleParamRepeated() {
        Template template = new Template("Hello, my name is _{name}. _{name} is my name.");
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", "Bubba");
        assertEquals("Hello, my name is Bubba. Bubba is my name.", template.getParameterizedText(params));
    }

    @Test
    public void testMultipleParams() {
        Template template = new Template("Hello, my name is _{name}. My email is _{email}.");
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", "Bubba");
        params.put("email", "bubba@bubba.com");
        assertEquals("Hello, my name is Bubba. My email is bubba@bubba.com.", template.getParameterizedText(params));
    }

    @Test
    public void testIsParameterizable() {
        Template template1 = new Template("Hello, my name is _{name}. My email is _{email}.");
        assertTrue(template1.isParameterizable());

        Template template2 = new Template("Hi there.");
        assertFalse(template2.isParameterizable());
    }
}
