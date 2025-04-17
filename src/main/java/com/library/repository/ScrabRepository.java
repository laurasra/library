package com.library.repository;

import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.TreeSet;

@Repository
public interface ScrabRepository {
    public Set<String> getDictionary(String language);
}
