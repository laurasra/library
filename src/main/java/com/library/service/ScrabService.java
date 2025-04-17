package com.library.service;

import com.library.model.Scrab;
import com.library.repository.ScrabRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class ScrabService {
    private final ScrabRepository scrabRepository;

    @Autowired
    public ScrabService(ScrabRepository scrabRepository) {
        this.scrabRepository = scrabRepository;
    }

    public String printSortedWordsByPoints(String lettersStr, String sequence, String language) {
        Scrab scrab = new Scrab(language);
        return scrab.printSortedWordsByPoints(lettersStr, sequence);
    }
}
