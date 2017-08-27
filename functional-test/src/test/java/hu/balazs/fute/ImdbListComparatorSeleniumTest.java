package hu.balazs.fute;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import com.google.common.io.Resources;
import hu.balazs.ImdbListComparatorApplication;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ImdbListComparatorApplication.class},
    webEnvironment = WebEnvironment.RANDOM_PORT)
public class ImdbListComparatorSeleniumTest {

  private static WebDriver driver;
  private static File moviesCsv;
  @LocalServerPort
  private int randomServerPort;


  @BeforeClass
  public static void beforeClass() throws IOException {
    System.setProperty("phantomjs.binary.path", Resources.getResource("phantomjs.exe").getPath());
    driver = new PhantomJSDriver();

    moviesCsv = createFileWithContent("movies", "Sideways", "Matrix");
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

  @AfterClass
  public static void afterClass() throws Exception {
    Files.delete(moviesCsv.toPath());
    driver.quit();
  }

  private static void assertSourceContains(String source, String stringToContain) {
    assertThat(StringUtils.deleteWhitespace(source),
        containsString(StringUtils.deleteWhitespace(stringToContain)));
  }

  private static void assertSourceContains(String source, String stringToContain, int times) {
    String actual = StringUtils.deleteWhitespace(source);
    String substring = StringUtils.deleteWhitespace(stringToContain);
    assertThat(StringUtils.countMatches(actual, substring), is(2));
  }

  @Test
  public void shouldOnlyShowTableAndFilterWhenImdbListIsUploaded() throws Exception {
    //given
    driver.get("http://localhost:" + randomServerPort);
    assertSourceContains(driver.getPageSource(),
        "<table id=\"movieTable1\" style=\"display: none;\">");
    uploadCsvToAList(moviesCsv, 1);

    //when
    driver.findElement(By.id("myInput")).sendKeys("idewa");

    //then
    assertSourceContains(driver.getPageSource(), "<table id=\"movieTable1\">");
  }

  @Test
  public void shouldBeAbleToFilterTitles() throws Exception {
    //given
    driver.get("http://localhost:" + randomServerPort);
    uploadCsvToAList(moviesCsv, 1);

    //when
    driver.findElement(By.id("myInput")).sendKeys("idewa");

    //then
    assertSourceContains(driver.getPageSource(), "<tr><td>Sideways</td></tr>");
    assertSourceContains(driver.getPageSource(), hiddenMovie("Matrix"));
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

      assertSourceContains(driver.getPageSource(), "<tr><td>Sideways</td></tr>", 2);
      assertSourceContains(driver.getPageSource(), hiddenMovie("Matrix"));
      assertSourceContains(driver.getPageSource(), hiddenMovie("Asd"));
    } finally {
      if (list2 != null) {
        Files.delete(list2.toPath());
      }
    }
  }

  private String hiddenMovie(String title) {
    return "<tr style=\"display: none;\"><td>" + title + "</td></tr>";
  }

  private void uploadCsvToAList(File csv, int listIndex) {
    WebElement element = driver.findElement(By.id("fileInput" + listIndex));
    element.sendKeys(csv.getPath());
    driver.findElement(By.id("fileInputSubmit" + listIndex)).click();
  }
}
