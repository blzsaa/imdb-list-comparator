package com.blzsaa.service;

import com.blzsaa.ImdbListComparatorException;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.blzsaa.ImdbListHelper.*;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class ListReaderServiceTest {
    private ListReaderService listReaderService;

    @Before
    public void setUp() {
        listReaderService = new ListReaderService();
    }

    @Test
    public void shouldRemoveEverythingButTheTitleOfImdbLists() {
        // given
        MultipartFile[] files =
                new MultipartFile[]{new MockMultipartFile("uploadedFile", "orig", "", CSV.getBytes())};

        // when
        List<String> actual = listReaderService.readMovieTitlesFrom(files);

        // then
        Assertions.assertThat(actual).containsExactly(TITLE1, TITLE2);
    }

    @Test
    public void shouldThrowListControllerExceptionWhenIOExceptionOccurs() throws Exception {
        // given
        MultipartFile multipartFile = mock(MultipartFile.class);
        IOException toBeThrown = new IOException();
        doThrow(toBeThrown).when(multipartFile).getInputStream();

        // when
        Throwable thrown =
                catchThrowable(
                        () -> listReaderService.readMovieTitlesFrom(new MultipartFile[]{multipartFile}));

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(ImdbListComparatorException.class)
                .hasCause(toBeThrown);
    }
}
