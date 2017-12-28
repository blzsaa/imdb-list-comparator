package com.blzsaa.integration;

import com.blzsaa.ImdbListComparatorApplication;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.util.Strings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.blzsaa.ImdbListHelper.*;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {ImdbListComparatorApplication.class},
        webEnvironment = WebEnvironment.RANDOM_PORT
)
public class ImdbListComparatorIT {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Before
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void frontPageAppears() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    public void canUploadCsv() throws Exception {
        // given
        MockMultipartFile m1 = new MockMultipartFile("uploadingFiles", CSV.getBytes());

        // when
        ResultActions resultActions =
                mvc.perform(
                        fileUpload("/uploadList")
                                .file(m1)
                                .param("list", "movies1")
                                .sessionAttr("movies", new HashMap<>()));

        // then
        resultActions
                .andExpect(content().string(containsString(TITLE1)))
                .andExpect(content().string(containsString(TITLE2)))
                .andExpect(
                        model().attribute("movies", ImmutableMap.of("movies1", asList(TITLE1, TITLE2))))
                .andExpect(status().isOk());
    }

    @Test
    public void canUploadSecondCsv() throws Exception {
        // given

        String csv = Strings.join(HEADER, rowWithTitle("Title3"), rowWithTitle("Title4")).with(System.lineSeparator());

        MockMultipartFile m1 = new MockMultipartFile("uploadingFiles", csv.getBytes());

        Map<String, List<String>> previouslyUploadedList = new HashMap<>();
        previouslyUploadedList.put("movies1", asList(TITLE1, TITLE2));

        // when
        ResultActions resultActions =
                mvc.perform(
                        fileUpload("/uploadList")
                                .file(m1)
                                .param("list", "movies2")
                .sessionAttr("movies", previouslyUploadedList));

        // then
        Map<String, List<String>> expectedMap = new HashMap<>();
        expectedMap.put("movies1", asList(TITLE1, TITLE2));
        expectedMap.put("movies2", asList("Title3", "Title4"));

        resultActions
                .andExpect(content().string(containsString(TITLE1)))
                .andExpect(content().string(containsString(TITLE2)))
                .andExpect(model().attribute("movies", expectedMap))
                .andExpect(status().isOk());
    }
}
