package com.blzsaa.web;

import com.blzsaa.service.ListReaderService;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.blzsaa.ImdbListHelper.TITLE1;
import static com.blzsaa.ImdbListHelper.TITLE2;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ListComparatorControllerTest {

  private ListComparatorController controller;
  @Mock
  private Model model;
  private MultipartFile[] files;
  @Mock
  private ListReaderService listReaderService;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    doReturn(ImmutableList.of(TITLE1, TITLE2))
            .when(listReaderService)
            .readMovieTitlesFrom(any(MultipartFile[].class));
    controller = new ListComparatorController(listReaderService);
  }

  @Test
  public void shouldFrontPageAppear() {
    // when
    String actual = controller.frontPage();

    // then
    assertThat(actual, is("welcome"));
  }

  @Test
  public void shouldUploadList_firstTime() {
    // when
    String actual = controller.uploadList(files, "list1", new HashMap<>(), model);

    // then
    assertThat(actual, is("welcome"));
    verify(model).addAttribute("list1", Arrays.asList(TITLE1, TITLE2));
  }

  @Test
  public void shouldOverwriteImdbListWithSameName() {
    // given
    HashMap<String, List<String>> movies = new HashMap<>();
    movies.put("list1", singletonList("oltTitle"));

    // when
    String actual = controller.uploadList(files, "list1", movies, model);

    // then
    assertThat(actual, is("welcome"));
    verify(model).addAttribute("list1", Arrays.asList(TITLE1, TITLE2));
  }

  @Test
  public void shouldNotOverwriteOtherImdbLists() {
    // given
    HashMap<String, List<String>> movies = new HashMap<>();
    movies.put("list1", singletonList("oltTITLE1"));
    movies.put("list2", singletonList("oltTITLE2"));
    movies.put("list3", singletonList("oltTitle3"));

    // when
    String actual = controller.uploadList(files, "list1", movies, model);

    // then
    assertThat(actual, is("welcome"));
    verify(model).addAttribute("list1", Arrays.asList(TITLE1, TITLE2));
    verify(model).addAttribute("list2", singletonList("oltTITLE2"));
    verify(model).addAttribute("list3", singletonList("oltTitle3"));
  }

  @Test
  public void shouldInitMoviesAsEmptyMap() {
    assertThat(controller.movies(), is(Collections.emptyMap()));
  }

}
