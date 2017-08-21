package hu.balazs.fute;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import demo.ImdbListComparatorApplication;
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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ImdbListComparatorApplication.class},
    webEnvironment = WebEnvironment.RANDOM_PORT)
public class ImdbListComparatorTest {

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
    String csv = "title" + System.lineSeparator() + "Title1" + System.lineSeparator() + "Title2";
    MockMultipartFile m1 = new MockMultipartFile("uploadingFiles", csv.getBytes());

    // when
    ResultActions resultActions = mvc.perform(fileUpload("/").file(m1));

    // then
    resultActions
        .andExpect(content().string(containsString("Title1")))
        .andExpect(content().string(containsString("Title2")))
        .andExpect(
            model().attribute("movies", hasItems("Title1","Title2")))
        .andExpect(status().isOk());
  }

}
