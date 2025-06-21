package com.chebotarev.textanalyzer.service;


import com.chebotarev.textanalyzer.model.WordFrequency;
import com.chebotarev.textanalyzer.util.CustomHashMap;
import com.chebotarev.textanalyzer.util.SpellChecker;

import java.util.*;

// Сервис для анализа текста
public class TextAnalysisService {
    private final SpellChecker spellChecker;

    public TextAnalysisService(CustomHashMap<String, Boolean> dictionary) {
        this.spellChecker = new SpellChecker(dictionary);
    }

    public int countTotalWords(String text) {
        if (text == null || text.isEmpty()) return 0;
        int count = 0;
        StringTokenizer tokenizer = new StringTokenizer(text);
        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken()
                    .toLowerCase()
                    .replaceAll("[^a-zа-яё]", "");
            if (!word.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public CustomHashMap<String, List<String>> checkSpelling(String text) {
        CustomHashMap<String, List<String>> errors = new CustomHashMap<>();
        if (text == null || text.isEmpty()) return errors;

        String[] words = text.toLowerCase()
                .replaceAll("[^a-zа-яё\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .split("\\s+");

        for (String word : words) {
            if (!word.isEmpty() && !spellChecker.isCorrect(word)) {
                // Фильтруем короткие слова (длина <= 3)
                if (word.length() > 3) {
                    errors.put(word, spellChecker.getSuggestions(word));
                }
            }
        }
        return errors;
    }

    public List<Integer> findWordPositions(String text, String searchWord) {
        List<Integer> positions = new ArrayList<>();
        if (text == null || searchWord == null || searchWord.isEmpty()) {
            return positions;
        }

        String normalizedSearch = searchWord.toLowerCase().trim();
        String[] words = text.toLowerCase().split("\\s+");

        for (int i = 0; i < words.length; i++) {
            String cleanWord = words[i].replaceAll("[^a-zа-яё]", "");
            if (cleanWord.equals(normalizedSearch)) {
                positions.add(i);
            }
        }
        return positions;
    }
    // Подсчет частоты слов
    public CustomHashMap<String, Integer> countWords(String text) {
        CustomHashMap<String, Integer> frequencyMap = new CustomHashMap<>();
        if (text == null || text.isEmpty()) return frequencyMap;

        // Используем более эффективный способ разделения слов
        StringTokenizer tokenizer = new StringTokenizer(text);
        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken().toLowerCase()
                    .replaceAll("[^a-zа-яё0-9]", ""); // Разрешаем цифры в словах

            if (!word.isEmpty()) {
                Integer count = frequencyMap.get(word);
                frequencyMap.put(word, (count == null) ? 1 : count + 1);
            }
        }
        return frequencyMap;
    }

    // Сортировка по частоте (по убыванию)
    public List<WordFrequency> sortByFrequency(CustomHashMap<String, Integer> map) {
        List<WordFrequency> result = new ArrayList<>();
        List<CustomHashMap.Entry<String, Integer>> entries = map.entries();

        // Преобразование в список объектов WordFrequency
        for (CustomHashMap.Entry<String, Integer> entry : entries) {
            result.add(new WordFrequency(entry.key, entry.value));
        }

        // Сортировка по убыванию частоты
        Collections.sort(result, new Comparator<WordFrequency>() {
            @Override
            public int compare(WordFrequency w1, WordFrequency w2) {
                return w2.getFrequency() - w1.getFrequency();
            }
        });

        return result;
    }
}
