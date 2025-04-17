package com.library.controller;

import com.library.model.User;
import com.library.service.ScrabService;
import com.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/scrab")
public class ScrabController {
    @Autowired
    private ScrabService scrabService;

    @GetMapping
    public String solve(@RequestParam String letters,
                                          @RequestParam Optional<String> sequence,
                                          @RequestParam Optional<String> language) {
        return scrabService.printSortedWordsByPoints(letters, sequence.orElse(null), language.orElse("fr"));
    }
}