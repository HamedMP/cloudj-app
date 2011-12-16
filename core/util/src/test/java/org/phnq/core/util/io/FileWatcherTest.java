/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.phnq.core.util.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
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
public class FileWatcherTest {
    
    public FileWatcherTest() {
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
    public void testAddFile() {
        FileWatcher fw = new FileWatcher();

        // Add null file
        try {
            fw.addFile(null);
            assertEquals(0, fw.getNumFiles());
        } catch (Throwable t) {
            fail("Adding a null file triggered an exception -- should be ignored.");
        }

        // Add non-existent file
        fw.addFile(new File("big_fat_mumbo_jumbo.txt"));
        assertEquals(0, fw.getNumFiles());

        // Add existing file
        try {
            File f = File.createTempFile("someFile", null);
            fw.addFile(f);
            assertEquals(1, fw.getNumFiles());
            f.delete();
        } catch (IOException ex) {
            fail("error: " + ex.getMessage());
        }
    }

    @Test
    public void testMark() {
        FileWatcher fw = new FileWatcher();
        try {
            File f = File.createTempFile("someFile", null);
            fw.addFile(f);

            // Modify file...
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(42);
            fos.close();

            assertEquals(true, fw.hasModificationsSinceLastMark());

            fw.mark();

            assertEquals(false, fw.hasModificationsSinceLastMark());
        } catch (IOException ex) {
            fail("error: " + ex.getMessage());
        }
    }
    
    @Test
    public void testGetNumFiles() {
        // testAddFile() exercises getNumFiles()
    }

    @Test
    public void testHasModificationsSinceLastMark() {
        // testMark() exercises hasModificationsSinceLastMark()
    }
}
