package hu.balazs.web;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

public class ListComparatorControllerTest {

  private static final String CSV =
      "title" + System.lineSeparator() + "Title1" + System.lineSeparator() + "Title2";
  private ListComparatorController controller;
  @Mock
  private Model model;
  private MultipartFile[] files;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    controller = new ListComparatorController();
    files = new MultipartFile[]{new MockMultipartFile("uploadedFile", "orig", "", CSV.getBytes())};
  }

  @Test
  public void shouldFrontPageAppear() throws IOException, InterruptedException {
    //when
    String actual = controller.frontPage();

    //then
    assertThat(actual, is("welcome"));
  }

  @Test
  public void shouldUploadList_firstTime() throws IOException, InterruptedException {
    //when
    String actual = controller.uploadList(files, "list1", new HashMap<>(), model);

    //then
    assertThat(actual, is("welcome"));
    verify(model).addAttribute("list1", Arrays.asList("Title1", "Title2"));
  }

  @Test
  public void shouldOverwriteImdbListWithSameName() throws IOException, InterruptedException {
    //given
    HashMap<String, List<String>> movies = new HashMap<>();
    movies.put("list1", singletonList("oltTitle"));

    //when
    String actual = controller.uploadList(files, "list1", movies, model);

    //then
    assertThat(actual, is("welcome"));
    verify(model).addAttribute("list1", Arrays.asList("Title1", "Title2"));
  }

  @Test
  public void shouldNotOverwriteOtherImdbLists() throws IOException, InterruptedException {
    //given
    HashMap<String, List<String>> movies = new HashMap<>();
    movies.put("list1", singletonList("oltTitle1"));
    movies.put("list2", singletonList("oltTitle2"));
    movies.put("list3", singletonList("oltTitle3"));

    //when
    String actual = controller.uploadList(files, "list1", movies, model);

    //then
    assertThat(actual, is("welcome"));
    verify(model).addAttribute("list1", Arrays.asList("Title1", "Title2"));
    verify(model).addAttribute("list2", singletonList("oltTitle2"));
    verify(model).addAttribute("list3", singletonList("oltTitle3"));
  }

  @Test
  public void shouldInitMoviesAsEmptyMap() throws Exception {
    assertThat(controller.movies(), is(Collections.emptyMap()));
  }

  @Test
  public void shouldThrowListControllerExceptionWhenIOExceptionOccurs() throws Exception {
    //given
    MultipartFile multipartFile = mock(MultipartFile.class);
    IOException toBeThrown = new IOException();
    doThrow(toBeThrown).when(multipartFile).getBytes();

    // when
    Throwable thrown = catchThrowable(() -> controller
        .uploadList(new MultipartFile[]{multipartFile}, "list1", new HashMap<>(), model));

    //then
    Assertions.assertThat(thrown).isInstanceOf(ListControllerException.class).hasCause(toBeThrown);
  }
}