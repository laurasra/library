package com.library.repository;

import org.springframework.stereotype.Repository;

import java.util.TreeSet;

@Repository
public interface ScrabRepository {
    public TreeSet<String> getDictionary(String language);
}
