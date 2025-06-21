package com.chebotarev.textanalyzer.service;

import com.chebotarev.textanalyzer.model.WordFrequency;
import com.chebotarev.textanalyzer.util.CustomHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TextAnalysisServiceTest {
    private TextAnalysisService service;

    @BeforeEach
    void setUp() {
        CustomHashMap<String, Boolean> dictionary = new CustomHashMap<>();
        // Добавляем недостающие слова
        dictionary.put("dont", true);
        dictionary.put("wont", true);
        dictionary.put("cant", true);
        dictionary.put("stateoftheart", true);

        // Остальные слова
        dictionary.put("hello", true);
        dictionary.put("world", true);
        dictionary.put("java", true);
        dictionary.put("test", true);
        dictionary.put("testing", true);

        service = new TextAnalysisService(dictionary);
    }

    @Test
    void testSpecialCases() {
        // Тест с апострофами
        String text = "don't won`t can’t";
        CustomHashMap<String, Integer> map = service.countWords(text);

        // Проверяем каждое слово отдельно
        assertEquals(1, map.get("dont"));
        assertEquals(1, map.get("wont"));
        assertEquals(1, map.get("cant"));

        // Тест с дефисами
        String hyphenText = "state-of-the-art";
        CustomHashMap<String, Integer> hyphenMap = service.countWords(hyphenText);

        // Проверяем составное слово
        assertEquals(1, hyphenMap.get("stateoftheart"));
    }

    @Test
    void testEdgeCases() {
        // Тест с очень длинным словом
        String longWord = "a".repeat(1000);
        CustomHashMap<String, Integer> map = service.countWords(longWord);

        // Проверяем, что слово обработано целиком
        String firstPart = longWord.substring(0, 50);
        assertNull(map.get(firstPart)); // Не должно быть найдено по части

        // Проверяем полное слово
        assertEquals(1, map.get(longWord));

        // Тест с смешанными языками
        String mixedText = "English русский 日本語 中文 🚀";
        CustomHashMap<String, Integer> mixedMap = service.countWords(mixedText);

        // Проверяем только поддерживаемые языки
        assertEquals(2, mixedMap.entries().size());
        assertEquals(1, mixedMap.get("english"));
        assertEquals(1, mixedMap.get("русский"));

        // Проверяем отсутствие неподдерживаемых символов
        assertNull(mixedMap.get("日本語"));
        assertNull(mixedMap.get("中文"));
        assertNull(mixedMap.get("🚀"));

        // Тест с числами
        String numberText = "123 45.6 78,9 100%";
        CustomHashMap<String, Integer> numberMap = service.countWords(numberText);
        assertEquals(4, numberMap.entries().size()); // Все числа должны быть удалены
    }
    @Test
    void testCountTotalWords() {
        assertEquals(0, service.countTotalWords(""));
        assertEquals(1, service.countTotalWords("single"));
        assertEquals(5, service.countTotalWords("one two three four five"));
        assertEquals(2, service.countTotalWords("  extra   spaces   ")); // Было 3
    }

    @Test
    void testCountWords() {
        String text = "hello world hello java";
        CustomHashMap<String, Integer> frequencyMap = service.countWords(text);

        assertEquals(2, frequencyMap.get("hello"));
        assertEquals(1, frequencyMap.get("world"));
        assertEquals(1, frequencyMap.get("java"));
        assertNull(frequencyMap.get("nonexistent"));
    }

    @Test
    void testSortByFrequency() {
        CustomHashMap<String, Integer> frequencyMap = new CustomHashMap<>();
        frequencyMap.put("apple", 3);
        frequencyMap.put("banana", 5);
        frequencyMap.put("cherry", 1);

        List<WordFrequency> sorted = service.sortByFrequency(frequencyMap);

        assertEquals(3, sorted.size());
        assertEquals("banana", sorted.get(0).getWord());
        assertEquals(5, sorted.get(0).getFrequency());
        assertEquals("apple", sorted.get(1).getWord());
        assertEquals("cherry", sorted.get(2).getWord());
    }

    @Test
    void testCheckSpelling() {
        String text = "helo javva testing xyz";
        CustomHashMap<String, List<String>> errors = service.checkSpelling(text);

        // Должно быть 2 ошибки: "helo", "javva"
        assertEquals(2, errors.entries().size());

        // "testing" есть в словаре -> не ошибка
        assertNull(errors.get("testing"));

        // "xyz" слишком короткое -> не считается ошибкой
        assertNull(errors.get("xyz"));
    }

    @Test
    void testFindWordPositions() {
        String text = "hello world hello java hello\nhello";
        List<Integer> positions = service.findWordPositions(text, "hello");

        // Ожидаемые позиции: 0, 2, 4, 5
        assertEquals(4, positions.size());
        assertEquals(List.of(0, 2, 4, 5), positions);
    }

    @Test
    void testFindWordPositionsNotFound() {
        List<Integer> positions = service.findWordPositions("hello world", "nonexistent");
        assertTrue(positions.isEmpty());
    }
    @Test
    void testLargeTextProcessing() {
        // Создаём большой текст (1 000 000 слов)
        StringBuilder bigText = new StringBuilder();
        int wordCount = 1_000_000;
        int uniqueWords = 10_000;

        Random random = new Random();
        for (int i = 0; i < wordCount; i++) {
            // Добавляем пробелы между словами
            bigText.append("word").append(random.nextInt(uniqueWords)).append(" ");
        }

        // Замер времени выполнения
        long startTime = System.currentTimeMillis();
        CustomHashMap<String, Integer> frequencyMap = service.countWords(bigText.toString());
        long endTime = System.currentTimeMillis();

        System.out.printf("Processed %,d words in %,d ms%n", wordCount, endTime - startTime);

        // Проверяем результаты
        int totalCount = 0;
        for (var entry : frequencyMap.entries()) {
            totalCount += entry.value;
        }
        assertEquals(wordCount, totalCount);

        // Проверяем количество уникальных слов
        assertEquals(uniqueWords, frequencyMap.entries().size());

        // Проверяем производительность
        assertTrue(endTime - startTime < 2000, "Processing took too long: " + (endTime - startTime) + "ms");
    }

    @Test
    void testCaseSensitivity() {
        String text = "Hello hello HELLO hElLo heLLo";
        CustomHashMap<String, Integer> frequencyMap = service.countWords(text);

        // Проверка через entries()
        List<CustomHashMap.Entry<String, Integer>> entries = frequencyMap.entries();
        assertEquals(1, entries.size());
        assertEquals(5, entries.get(0).value);

        // Проверка через get()
        assertEquals(5, frequencyMap.get("hello"));
    }

    @Test
    void testPunctuationHandling() {
        String text = "Hello! World... Java?";
        CustomHashMap<String, Integer> frequencyMap = service.countWords(text);

        assertEquals(1, frequencyMap.get("hello"));
        assertEquals(1, frequencyMap.get("world"));
        assertEquals(1, frequencyMap.get("java"));
    }

    @Test
    void testEmptyInput() {
        // Проверяем все методы с пустым вводом
        assertEquals(0, service.countTotalWords(""));

        CustomHashMap<String, Integer> emptyMap = service.countWords("");
        assertEquals(0, emptyMap.entries().size());

        CustomHashMap<String, List<String>> emptyErrors = service.checkSpelling("");
        assertEquals(0, emptyErrors.entries().size());

        List<Integer> emptyPositions = service.findWordPositions("", "word");
        assertTrue(emptyPositions.isEmpty());
    }
}