package com.blzsaa.fute;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import com.blzsaa.ImdbListComparatorApplication;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ImdbListComparatorApplication.class},
    webEnvironment = WebEnvironment.RANDOM_PORT)
public class ImdbListComparatorSeleniumIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImdbListComparatorSeleniumIT.class);
  private static final String PHANTOMJS_BINARY_PATH = "phantomjs.binary.path";
  private static WebDriver driver;
  private static File moviesCsv;
  @LocalServerPort
  private int randomServerPort;

  @BeforeClass
  public static void beforeClass() throws Exception {
    if (System.getProperty("phantomjs.binary.path") == null) {
      setBinaryPath();
    }
    driver = new PhantomJSDriver();

    moviesCsv = createFileWithContent("movies", "Sideways", "Matrix");
  }

  @AfterClass
  public static void afterClass() throws Exception {
    Files.delete(moviesCsv.toPath());
    driver.quit();
  }


  @After
  public void tearDown() throws Exception {
    driver.manage().deleteAllCookies();
  }

  @Test
  public void shouldOnlyShowTableAndFilterWhenImdbListIsUploaded() throws Exception {
    //given
    driver.get("http://localhost:" + randomServerPort);
    assertSourceContains(
        "<table id=\"movieTable1\" style=\"display: none;\">");
    uploadCsvToAList(moviesCsv, 1);

    //when
    driver.findElement(By.id("myInput")).sendKeys("idewa");

    //then
    assertSourceContains("<table id=\"movieTable1\">");
  }


  @Test
  public void shouldBeAbleToFilterTitles() throws Exception {
    //given
    driver.get("http://localhost:" + randomServerPort);
    uploadCsvToAList(moviesCsv, 1);

    //when
    driver.findElement(By.id("myInput")).sendKeys("idewa");

    //then
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
      //given
      driver.get("http://localhost:" + randomServerPort);
      uploadCsvToAList(moviesCsv, 1);
      list2 = createFileWithContent("name2", "Asd", "Sideways");
      uploadCsvToAList(list2, 2);

      //when
      driver.findElement(By.id("myInput")).sendKeys("idewa");

      //then
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
    return "<tr><td>"+title+"</td></tr>";
  }


  private static void setBinaryPath() {
    List<File> driverBinaries = new ArrayList<>(collectBinaries());
    verifyBinary(driverBinaries);
    String phantomJsPath = driverBinaries.get(0).getAbsolutePath();
    LOGGER.debug("Setting {} to {}", PHANTOMJS_BINARY_PATH, phantomJsPath);
    System.setProperty(PHANTOMJS_BINARY_PATH, phantomJsPath);
  }

  private static void verifyBinary(Collection<File> driverBinaries) {
    if (driverBinaries.isEmpty()) {
      throw new IllegalStateException(
          "No phantomjs.exe found, try mvn clean install to automatically download it!");
    }
  }

  private static Collection<File> collectBinaries() {
    return FileUtils.listFiles(new File("."), new IOFileFilter() {
      @Override
      public boolean accept(File file) {
        return "phantomjs.exe".equals(file.getName());
      }

      @Override
      public boolean accept(File dir, String name) {
        return "phantomjs.exe".equals(name);
      }
    }, TrueFileFilter.INSTANCE);
  }

  private static void uploadCsvToAList(File csv, int listIndex) throws InterruptedException {
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

  private static void assertSourceContains(String stringToContain)
      throws InterruptedException {
    assertThat(StringUtils.deleteWhitespace(driver.getPageSource()),
        containsString(StringUtils.deleteWhitespace(stringToContain)));
  }

  private static void assertSourceContainsTwice(String stringToContain) {
    String actual = StringUtils.deleteWhitespace(driver.getPageSource());
    String substring = StringUtils.deleteWhitespace(stringToContain);
    assertThat(StringUtils.countMatches(actual, substring), is(2));
  }
}
