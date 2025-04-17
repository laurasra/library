package com.library.model;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.antlr.v4.runtime.tree.Tree;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public class Scrab {
    private String language; //make an enum
    private Map<String, Integer> letterPointsMap;
    private TreeSet<String> dictionary;
    private final char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    public Scrab(String language) {
        this.language = language;
        this.letterPointsMap = new HashMap<>();
        String path = "src/main/resources/";
        String pointsFilename = path + "letters_"+ language +".json";
        String dictionaryFilename = path + "dictionary_" + language + ".txt";

        try {
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<HashMap<String,Integer>> typeRef = new TypeReference<>() {};

            this.letterPointsMap = mapper.readValue(new File(pointsFilename), typeRef);
        }catch(Exception e){
            System.out.println("Error reading letters file");
            System.out.println(e);
        }

        try {
            List<String> list = Files.readAllLines(new File(dictionaryFilename).toPath(), Charset.defaultCharset() );
            dictionary = new TreeSet<>(list);
        }catch (Exception e) {
            System.out.println("Error reading dictionary file");
            System.out.println(e);
        }
    }

    public String getLanguage() {
        return language;
    }

    public Integer getLetterPoint(String letter) {
        return Optional.ofNullable(
                getLetterPointsMap().get(letter.substring(0,1)))
                .orElse(0);
    }

    public Integer getLetterPoint(char c) {
        return getLetterPoint(Character.toString(c));
    }

    public Integer getWordPoints(String word) {
        Integer points = 0;
        for (char c : word.toCharArray()) {
            points += getLetterPoint(c);
        }
        return points;
    }

    public HashMap<String, Integer> getLetterPointsMap() {
        return (HashMap<String, Integer>) letterPointsMap;
    }

    public HashMap<String, Integer> getPossibleWords(String lettersStr) {
        return getPossibleWords(lettersStr, null);
    }

    /** returns possible words with points
     * @param lettersStr    letters in hand, can contain ? or *
     * @param sequence      sequence to plug on
     * @return
     */
    public HashMap<String, Integer> getPossibleWords(String lettersStr, String sequence) {
        lettersStr = formatWildcards(lettersStr).toUpperCase();
        HashMap<String, Integer> possibleWords = new HashMap<>();
        ArrayList<String> tokens = new ArrayList<>(Arrays
                .stream(lettersStr.split(""))
                .map(s -> Character.toString(s.charAt(0)))
                .toList());
        if (!ObjectUtils.isEmpty(sequence)) {
            tokens.add(sequence);
        }
        long startTimeCompose = System.nanoTime();
        Set<String> composeSet = compose(tokens);
        long endTimeCompose = System.nanoTime();
        System.out.println("Compose time (ms) : " + (endTimeCompose - startTimeCompose)/1_000_000.0);

        long startTimeReplaceWildcards = System.nanoTime();
        replaceWildcards(composeSet, lettersStr);
        long endTimeReplaceWildcards = System.nanoTime();
        System.out.println("Replace wildcards time (ms) : " + (endTimeReplaceWildcards - startTimeReplaceWildcards)/1_000_000.0);

        System.out.println(tokens);
        //System.out.println(composeSet);
        System.out.println("taille : " + composeSet.size());

        long startTimeFind = System.nanoTime();
        composeSet.stream()
                .filter(s -> dictionary.contains(s.toUpperCase()))
                .filter(s -> !StringUtils.hasLength(sequence) || (s.contains(sequence) && !s.equals(sequence)))
                .forEach(s -> possibleWords.put(s, getWordPoints(s)));
        long endTimeFind = System.nanoTime();
        System.out.println("Find time (ms) : " + (endTimeFind - startTimeFind)/1_000_000.0);
        System.out.println("nombre de mots possibles : " + possibleWords.size());
        return possibleWords;
    }

    public String printSortedWordsByPoints(String lettersStr, String sequence) {
        HashMap<String, Integer> wordPointsMap = getPossibleWords(lettersStr, sequence);
        Integer maxPoints = 0;
        for (String word : wordPointsMap.keySet()) {
            if (wordPointsMap.get(word) > maxPoints) {
                maxPoints = wordPointsMap.get(word);
            }
        }
        StringBuilder resultString = new StringBuilder();
        for (int i=maxPoints; i>=0; i--) {
            StringBuilder str = new StringBuilder();
            for (String word : wordPointsMap.keySet()) {
                if (wordPointsMap.get(word).equals(i)) {
                    str.append(word).append(" ");
                }
            }
            if (!str.isEmpty()) {
                String s = "mots à " + i + " points : " + str.toString().toUpperCase();
                resultString.append(s);
                resultString.append("\n");
                System.out.println(s);
            }
        }
        return resultString.toString();
    }

    /**
     * generate all compositions with given letters
     * @param letters
     * @return
     */
    public HashSet<String> compose(ArrayList<String> letters) {
        //System.out.println("compose " + letters);
        return compose(letters, new HashSet<>());
    }

    private HashSet<String> compose(ArrayList<String> tokens, HashSet<String> returnSet) {
        if (tokens.size() == 1) {
            returnSet.add(tokens.get(0));
            return returnSet;
        }
        for (int i = 0; i < tokens.size(); i++) {
            ArrayList<String> otherTokens = new ArrayList<>(tokens);
            otherTokens.remove(i);
            //System.out.println("other letters: " + otherLetters);
            HashSet<String> compose_n1 = compose(otherTokens);
            returnSet.addAll(compose_n1);

            ArrayList<String> temp = new ArrayList<>();
            for(String s : compose_n1) {
                temp.add(tokens.get(i) + s);
            }
            returnSet.addAll(temp);
        }
        return returnSet;
    }

    @Deprecated
    private Set<String> formatWildcards(TreeSet<String> composeSet) {
        TreeSet<String> wordsToAdd = new TreeSet<>();
        TreeSet<String> wordsToRemove = composeSet.stream()
                .filter(word -> word.contains("*"))
                .peek(word -> wordsToAdd.add(word.replace("*", "?")))
                .collect(Collectors.toCollection(TreeSet<String>::new));
        composeSet.removeAll(wordsToRemove);
        composeSet.addAll(wordsToAdd);
        return composeSet;
    }

    /**
     * replace all allowed wilcards with "?" before composition
     * @param letters
     * @return
     */
    private String formatWildcards(String letters) {
        // Todo add DEFAULT_WILDCARD and ALLOWED_WILDCARDS
        return letters.replace("*", "?");
    }

    private void replaceWildcards(Set<String> composeSet, String letters) {
        int wildcards = StringUtils.countOccurrencesOf(letters, "?");

        for (int i = 0; i < wildcards; i++) {
            TreeSet<String> wordsToRemove = new TreeSet<>();
            TreeSet<String> wordsToAdd = new TreeSet<>();
            composeSet.stream()
                    .filter(word -> word.contains("?"))
                    .peek(wordsToRemove::add)
                    .forEach(word -> {
                        for (char c : alphabet) {
                            wordsToAdd.add(word.replaceFirst("\\?", String.valueOf(c)));
                        }
                    });
            composeSet.removeAll(wordsToRemove);
            composeSet.addAll(wordsToAdd);
        }
    }
}

