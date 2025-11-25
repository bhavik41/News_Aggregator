package com.example.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.model.News;
import com.example.service.HomeService;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    @Autowired
    private HomeService homeService;

    @GetMapping
   public Map<String, List<News>> getHomeNews() {
        return homeService.getNewsGroupedBySection();
    }
}
