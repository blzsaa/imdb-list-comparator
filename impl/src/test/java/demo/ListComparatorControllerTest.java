package demo;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

public class ListComparatorControllerTest {

  private ListComparatorController controller;
  @Mock
  private Model model;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    controller = new ListComparatorController();
  }

  @Test
  public void shouldFrontPageAppear() throws IOException, InterruptedException {
    //when
    String actual = controller.frontPage();

    //then
    assertThat(actual, is("welcome"));
  }

  @Test
  public void shouldUploadCsv() throws IOException, InterruptedException {
    //given
    String csv = "title" + System.lineSeparator() + "Title1" + System.lineSeparator() + "Title2";
    MultipartFile file = new MockMultipartFile("uploadedFile", "orig", "", csv.getBytes());
    MultipartFile[] files = new MultipartFile[]{file};

    //when
    String actual = controller.uploadEncodedFileFromFile(files, model);

    //then
    assertThat(actual, is("welcome"));
    verify(model).addAttribute("movies", Arrays.asList("Title1", "Title2"));
  }
}