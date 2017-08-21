package demo;

import java.io.IOException;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ListComparatorController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ListComparatorController.class);

  @Autowired
  ListComparatorController() {
    LOGGER.debug("{} started", this.getClass());
  }

  @RequestMapping("/")
  public String frontPage() {
    return "welcome";
  }

  @RequestMapping(value = "/", method = RequestMethod.POST)
  public String uploadEncodedFileFromFile(
      @RequestParam("uploadingFiles") MultipartFile[] uploadingFiles, Model model)
      throws IOException{
    for (MultipartFile uploadedFile : uploadingFiles) {
      String content = new String(uploadedFile.getBytes());
      LOGGER.debug("content was: {}", content);

      String withoutHeader = content.substring(content.indexOf(System.lineSeparator())+System.lineSeparator().length());
      model.addAttribute("movies", Arrays.asList(withoutHeader.split(System.lineSeparator())));
    }
    return "welcome";
  }

}
