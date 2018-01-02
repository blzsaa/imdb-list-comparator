package com.blzsaa.integration;

import com.blzsaa.ImdbListComparatorApplication;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ImdbListComparatorApplication.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
public class ImdbListComparatorSeleniumIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImdbListComparatorSeleniumIT.class);
    private static WebDriver driver;
    private static File moviesCsv;
    @LocalServerPort
    private int randomServerPort;

    @BeforeClass
    public static void beforeClass() throws Exception {
        final ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setBinary("/path/to/google-chrome-stable");
        chromeOptions.addArguments("--headless");
        chromeOptions.addArguments("--disable-gpu");

        final DesiredCapabilities dc = new DesiredCapabilities();
        dc.setJavascriptEnabled(true);
        dc.setCapability(
                ChromeOptions.CAPABILITY, chromeOptions
        );

        driver = new ChromeDriver(dc);

        moviesCsv = createFileWithContent("movies", "Sideways", "Matrix");
    }

    @AfterClass
    public static void afterClass() throws Exception {
        Files.delete(moviesCsv.toPath());
        driver.quit();
    }

    private static void uploadCsvToAList(File csv, int listIndex) {
        WebElement element = driver.findElement(By.id("fileInput" + listIndex));

        element.sendKeys(csv.getAbsolutePath());
        driver.findElement(By.id("fileInputSubmit" + listIndex)).click();
    }

    private static File createFileWithContent(String name, String... movieTitles) throws IOException {
        File file = new File(name);
        List<String> titles = Stream.of(movieTitles).collect(Collectors.toList());
        List<String> csv = new ArrayList<>();
        csv.add("Title");
        csv.addAll(titles);

        Files.write(file.toPath(), csv);
        return file;
    }

    private static void assertSourceContains(String stringToContain) {
        assertThat(StringUtils.deleteWhitespace(driver.getPageSource()),
                containsString(StringUtils.deleteWhitespace(stringToContain)));
    }

    private static void assertSourceContainsTwice(String stringToContain) {
        String actual = StringUtils.deleteWhitespace(driver.getPageSource());
        String substring = StringUtils.deleteWhitespace(stringToContain);
        assertThat(StringUtils.countMatches(actual, substring), is(2));
    }

    @After
    public void tearDown() {
        driver.manage().deleteAllCookies();
    }

    @Test
    public void shouldOnlyShowTableAndFilterWhenImdbListIsUploaded() {
        // given
        driver.get("http://localhost:" + randomServerPort);
        assertSourceContains("<table id=\"movieTable1\" style=\"display: none;\">");
        uploadCsvToAList(moviesCsv, 1);

        // when
        driver.findElement(By.id("myInput")).sendKeys("idewa");

        // then
        assertSourceContains("<table id=\"movieTable1\">");
    }

    @Test
    public void shouldBeAbleToFilterTitles() {
        // given
        driver.get("http://localhost:" + randomServerPort);
        uploadCsvToAList(moviesCsv, 1);

        // when
        driver.findElement(By.id("myInput")).sendKeys("idewa");

        // then
        assertSourceContains("<tr><td>Sideways</td></tr>");
        assertSourceContains(hiddenMovie("Matrix"));
    }

    private String hiddenMovie(String title) {
        return "<tr style=\"display: none;\"><td>" + title + "</td></tr>";
    }

    @Test
    public void shouldBeAbleToUploadMultipleCsvAndFilterThemAtTheSameTime() throws Exception {
        File list2 = null;
        try {
            // given
            driver.get("http://localhost:" + randomServerPort);
            uploadCsvToAList(moviesCsv, 1);
            list2 = createFileWithContent("name2", "Asd", "Sideways");
            uploadCsvToAList(list2, 2);

            // when
            driver.findElement(By.id("myInput")).sendKeys("idewa");

            // then
            assertSourceContainsTwice(shownMovie("Sideways"));
            assertSourceContains(hiddenMovie("Matrix"));
            assertSourceContains(hiddenMovie("Asd"));
        } finally {
            if (list2 != null) {
                Files.delete(list2.toPath());
            }
        }
    }

    private String shownMovie(String title) {
        return "<tr><td>" + title + "</td></tr>";
    }
}
