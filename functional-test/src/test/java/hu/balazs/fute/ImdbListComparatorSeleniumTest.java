package hu.balazs.fute;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import com.google.common.io.Resources;
import demo.ImdbListComparatorApplication;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
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

  @LocalServerPort
  private int randomServerPort;
  private static WebDriver driver;
  private static File moviesCsv;

  private static void assertSourceContains(String source, String stringToContain) {
    assertThat(StringUtils.deleteWhitespace(source),
        containsString(StringUtils.deleteWhitespace(stringToContain)));
  }

  @BeforeClass
  public static void beforeClass() throws IOException {
    System.setProperty("phantomjs.binary.path", Resources.getResource("phantomjs.exe").getPath());
    driver = new PhantomJSDriver();

    moviesCsv = new File("movies");
    List<String> csv = Arrays.asList("Title", "Sideways", "Matrix");
    Files.write(moviesCsv.toPath(), csv);
  }

  @AfterClass
  public static void afterClass() throws Exception {
    Files.delete(moviesCsv.toPath());
    driver.quit();
  }

  @Test
  public void shouldOnlyShowTableAndFilterWhenImdbListIsUploaded() throws Exception {
    //given
    driver.get("http://localhost:" + randomServerPort);
    assertSourceContains(driver.getPageSource(),
        "<div id=\"imdbListDiv\" style=\"display: none;\">");
    uploadFile(moviesCsv);

    //when
    driver.findElement(By.id("myInput")).sendKeys("idewa");

    //then
    assertSourceContains(driver.getPageSource(), "<div id=\"imdbListDiv\">");
  }

  @Test
  public void shouldBeAbleToFilterTitles() throws Exception {
    //given
    driver.get("http://localhost:" + randomServerPort);
    uploadFile(moviesCsv);

    //when
    driver.findElement(By.id("myInput")).sendKeys("idewa");

    //then
    assertSourceContains(driver.getPageSource(), "<tr><td>Sideways</td></tr>");
    assertSourceContains(driver.getPageSource(),
        "<tr style=\"display: none;\"><td>Matrix</td></tr>");
  }

  private void uploadFile(File moviesCsv1) {
    WebElement element = driver.findElement(By.id("fileInput"));
    element.sendKeys(moviesCsv1.getPath());
    driver.findElement(By.id("fileInputSubmit")).click();
  }

}
