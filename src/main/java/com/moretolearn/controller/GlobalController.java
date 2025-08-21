package com.moretolearn.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class GlobalController {

    private final Random random = new Random();

    @GetMapping("/api/test")
    public String testApi() {
        if (random.nextBoolean()) {
            throw new RuntimeException("Random failure!");
        }
        return "Success!";
    }
}
