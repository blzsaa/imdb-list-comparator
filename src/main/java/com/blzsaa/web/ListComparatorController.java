package com.blzsaa.web;

import com.blzsaa.service.ListReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Controller
@SessionAttributes({"movies"})
public class ListComparatorController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ListComparatorController.class);
  private static final String FRONT_PAGE = "welcome";

  private final ListReaderService listReaderService;

  @Autowired
  ListComparatorController(ListReaderService listReaderService) {
    LOGGER.debug("{} started", this.getClass());
    this.listReaderService = listReaderService;
  }

  @ModelAttribute("movies")
  public Map<String, List<String>> movies() {
    return new HashMap<>();
  }

  @RequestMapping("/")
  public String frontPage() {
    return FRONT_PAGE;
  }

  @PostMapping(value = "/uploadList")
  public String uploadList(
          @RequestParam("uploadingFiles") MultipartFile[] uploadingFiles,
          @RequestParam("list") String listName,
          @SessionAttribute("movies") Map<String, List<String>> movies,
          Model model) {
    movies.put(listName, listReaderService.readMovieTitlesFrom(uploadingFiles));
    for (Entry<String, List<String>> imdbList : movies.entrySet()) {
      model.addAttribute(imdbList.getKey(), imdbList.getValue());
    }
    return FRONT_PAGE;
  }
}
