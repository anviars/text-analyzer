package com.chebotarev.textanalyzer.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SpellCheckerTest {
    private SpellChecker spellChecker;

    @BeforeEach
    void setUp() {
        CustomHashMap<String, Boolean> dictionary = new CustomHashMap<>();
        dictionary.put("hello", true);
        dictionary.put("world", true);
        dictionary.put("java", true);
        dictionary.put("programming", true);
        dictionary.put("test", true);
        dictionary.put("text", true);
        dictionary.put("analyzer", true);

        spellChecker = new SpellChecker(dictionary);
    }

    @Test
    void testIsCorrect() {
        assertTrue(spellChecker.isCorrect("hello"));
        assertTrue(spellChecker.isCorrect("WORLD"));
        assertFalse(spellChecker.isCorrect("helo"));
        assertFalse(spellChecker.isCorrect("javva"));
    }

    @Test
    void testGetSuggestions() {
        List<String> suggestions = spellChecker.getSuggestions("helo");
        assertTrue(suggestions.contains("hello"));
        assertFalse(suggestions.isEmpty());

        suggestions = spellChecker.getSuggestions("progamming");
        assertTrue(suggestions.contains("programming"));
        assertFalse(suggestions.isEmpty());
    }

    @Test
    void testEmptySuggestionsForCorrectWord() {
        List<String> suggestions = spellChecker.getSuggestions("java");
        assertTrue(suggestions.isEmpty());
    }

    @Test
    void testNoSuggestions() {
        List<String> suggestions = spellChecker.getSuggestions("xyz");
        assertTrue(suggestions.isEmpty());

        suggestions = spellChecker.getSuggestions("a");
        assertTrue(suggestions.isEmpty());
    }
}