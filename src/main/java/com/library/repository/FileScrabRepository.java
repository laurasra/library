package com.library.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Repository
public class FileScrabRepository implements ScrabRepository{
    private static final String path = "src/main/resources/";

    @Override
    public TreeSet<String> getDictionary(String language) {
        String dictionaryFilename = path + "dictionary_" + language + ".txt";

        try {
            List<String> list = Files.readAllLines(new File(dictionaryFilename).toPath(), Charset.defaultCharset() );
            return new TreeSet<>(list);
        }catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public Map<String, Integer> getLetterPointsMap (String language) {
        String pointsFilename = path + "letters_"+ language +".json";

        try {
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<HashMap<String,Integer>> typeRef = new TypeReference<HashMap<String,Integer>>() {};

            return mapper.readValue(new File(pointsFilename), typeRef);
        }catch(Exception e){
            System.out.println(e);
        }
        return null;
    }
}
