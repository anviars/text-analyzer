package com.chebotarev.textanalyzer.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpellChecker {
    private final CustomHashMap<String, Boolean> dictionary;
    private static final String CYRILLIC_ALPHABET = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    private static final String LATIN_ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final String ALPHABET = CYRILLIC_ALPHABET + LATIN_ALPHABET;

    public SpellChecker(CustomHashMap<String, Boolean> dictionary) {
        this.dictionary = dictionary;
    }

    public boolean isCorrect(String word) {
        return dictionary.get(word.toLowerCase()) != null;
    }

    public List<String> getSuggestions(String word) {
        Set<String> suggestions = new HashSet<>();
        String lowerWord = word.toLowerCase();

        // Пропускаем правильные слова
        if (isCorrect(lowerWord)) {
            return new ArrayList<>();
        }

        // Генерируем предложения
        generateSuggestions(lowerWord, suggestions);

        return new ArrayList<>(suggestions);
    }

    private void generateSuggestions(String word, Set<String> suggestions) {
        // 1. Замены
        for (int i = 0; i < word.length(); i++) {
            for (char c : ALPHABET.toCharArray()) {
                String candidate = word.substring(0, i) + c + word.substring(i + 1);
                checkCandidate(candidate, suggestions);
            }
        }

        // 2. Вставки
        for (int i = 0; i <= word.length(); i++) {
            for (char c : ALPHABET.toCharArray()) {
                String candidate = word.substring(0, i) + c + word.substring(i);
                checkCandidate(candidate, suggestions);
            }
        }

        // 3. Удаления
        for (int i = 0; i < word.length(); i++) {
            String candidate = word.substring(0, i) + word.substring(i + 1);
            checkCandidate(candidate, suggestions);
        }

        // 4. Перестановка
        for (int i = 0; i < word.length() - 1; i++) {
            char[] chars = word.toCharArray();
            char temp = chars[i];
            chars[i] = chars[i + 1];
            chars[i + 1] = temp;
            String candidate = new String(chars);
            checkCandidate(candidate, suggestions);
        }
    }

    private void checkCandidate(String candidate, Set<String> suggestions) {
        if (candidate.length() >= 2 && dictionary.get(candidate) != null) {
            suggestions.add(candidate);
        }
    }
}

