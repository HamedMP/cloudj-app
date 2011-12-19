package org.phnq.cloudj.webapp.ui;

import java.util.List;
import junit.framework.TestCase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author pgostovic
 */
public class MainTest extends TestCase {
    
    private static final String BASE_URL = "http://localhost:54321";

    public MainTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testMakePlaylistFromSearch()
    {
        final WebDriver driver = new FirefoxDriver();
        driver.get(BASE_URL+"/phnqlets/cloudj.Main");

        WebElement queryField = driver.findElement(By.className("queryField"));
        queryField.sendKeys("Soothe Me Yuck");

        WebElement searchButton = driver.findElement(By.className("searchButton"));
        searchButton.click();

        (new WebDriverWait(driver, 30)).until(new ExpectedCondition<Boolean>() {

            public Boolean apply(WebDriver d) {
                return driver.findElements(By.className("cloudj.SearchResult")).size() > 0;
            }
        });
        
        WebElement resultElmnt = driver.findElements(By.className("cloudj.SearchResult")).get(0);
        resultElmnt.click();

        (new WebDriverWait(driver, 60)).until(new ExpectedCondition<Boolean>() {

            public Boolean apply(WebDriver d) {
                WebElement artistInfoElmnt = driver.findElements(By.className("cloudj.ArtistInfo")).get(0);
                return artistInfoElmnt.getAttribute("class").indexOf("progress") == -1;
            }
        });
        
        List<WebElement> trackElmnts = driver.findElements(By.className("cloudj.Track"));
        assertTrue("At least 10 tracks", trackElmnts.size() >= 10);

        driver.quit();
    }

    public void testMakePlaylistFromRandomTrack()
    {
        final WebDriver driver = new FirefoxDriver();
        driver.get(BASE_URL+"/phnqlets/cloudj.Main");

        WebElement addRandomButton = driver.findElement(By.className("addRandomButton"));
        addRandomButton.click();
        
        (new WebDriverWait(driver, 60)).until(new ExpectedCondition<Boolean>() {

            public Boolean apply(WebDriver d) {
                WebElement artistInfoElmnt = driver.findElements(By.className("cloudj.ArtistInfo")).get(0);
                return artistInfoElmnt.getAttribute("class").indexOf("progress") == -1;
            }
        });
        
        List<WebElement> trackElmnts = driver.findElements(By.className("cloudj.Track"));
        assertTrue("At least 10 tracks", trackElmnts.size() >= 10);

        driver.quit();
    }
    
//    public void testMakePlaylistAndPlayTrack()
//    {
//        final WebDriver driver = new FirefoxDriver();
//        driver.get(BASE_URL+"/phnqlets/cloudj.Main");
//
//        WebElement addRandomButton = driver.findElement(By.className("addRandomButton"));
//        addRandomButton.click();
//        
//        (new WebDriverWait(driver, 60)).until(new ExpectedCondition<Boolean>() {
//
//            public Boolean apply(WebDriver d) {
//                WebElement artistInfoElmnt = driver.findElements(By.className("cloudj.ArtistInfo")).get(0);
//                return artistInfoElmnt.getAttribute("class").indexOf("progress") == -1;
//            }
//        });
//        
//        final List<WebElement> trackElmnts = driver.findElements(By.className("cloudj.Track"));
//        assertTrue("At least 10 tracks", trackElmnts.size() >= 10);
//
//        WebElement firstTrackElmnt = trackElmnts.get(0);
//        firstTrackElmnt.click();
//        
//        trackElmnts.remove(0);
//
//        (new WebDriverWait(driver, 60)).until(new ExpectedCondition<Boolean>() {
//
//            public Boolean apply(WebDriver d) {
//                for(WebElement trackElmnt : trackElmnts)
//                {
//                    if(trackElmnt.getAttribute("class").indexOf("playing") != -1)
//                        return true;
//                }
//                return false;
//            }
//        });
//        
//        driver.quit();
//    }
    
    public void testSearchNoResults() {
        final WebDriver driver = new FirefoxDriver();
        driver.get(BASE_URL+"/phnqlets/cloudj.Main");

        WebElement queryField = driver.findElement(By.className("queryField"));
        queryField.sendKeys("adsjkhajksdakjsdhkajshdkahsdkjahdkadjkh");

        WebElement searchButton = driver.findElement(By.className("searchButton"));
        searchButton.click();

        (new WebDriverWait(driver, 30)).until(new ExpectedCondition<Boolean>() {

            public Boolean apply(WebDriver d) {
                WebElement artistInfoElmnt = driver.findElements(By.className("cloudj.ArtistInfo")).get(0);
                return artistInfoElmnt.getAttribute("class").indexOf("progress") == -1;
            }
        });

        WebElement errMsgElmnt = driver.findElements(By.className("errorMsg")).get(0);
        assertTrue("Error message is shown", errMsgElmnt.getAttribute("style").contains("block"));

        driver.quit();
    }
}
