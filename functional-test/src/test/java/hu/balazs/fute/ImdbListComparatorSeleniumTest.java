package hu.balazs.fute;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import com.google.common.io.Resources;
import demo.ImdbListComparatorApplication;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
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
  private WebDriver driver;
  private File moviesCsv;

  @Before
  public void setUp() throws Exception {
    System.setProperty("phantomjs.binary.path", Resources.getResource("phantomjs.exe").getPath());

    driver = new PhantomJSDriver();

    moviesCsv = new File("movies");
    List<String> csv = Arrays.asList("Title", "Sideways", "Matrix");
    Files.write(moviesCsv.toPath(), csv);
  }

  @After
  public void tearDown() throws Exception {
    Files.delete(moviesCsv.toPath());
    driver.quit();
  }

  @Test
  public void shouldBeAbleToFilterTitles() throws Exception {
    //given
    driver.get("http://localhost:" + randomServerPort);
    uploadFile(moviesCsv);
    System.out.println(driver.getPageSource());
    //when
    driver.findElement(By.id("myInput")).sendKeys("idewa");

    //then
    String source = driver.getPageSource().replaceAll("\n", "");
    assertThat(source, containsString("<tr>        <td>Sideways</td>      </tr>"));
    assertThat(source,
        containsString("<tr style=\"display: none;\">        <td>Matrix</td>      </tr>"));
  }

  private void uploadFile(File moviesCsv1) {
    WebElement element = driver.findElement(By.id("fileInput"));
    element.sendKeys(moviesCsv1.getPath());
    driver.findElement(By.id("fileInputSubmit")).click();
  }

}
