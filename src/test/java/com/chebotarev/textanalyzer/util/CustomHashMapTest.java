package com.chebotarev.textanalyzer.util;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CustomHashMapTest {
    @Test
    void testHashMapDuplicates() {
        CustomHashMap<String, Integer> map = new CustomHashMap<>();
        map.put("hello", 1);
        map.put("hello", 2);
        map.put("hello", 3);
        map.put("hello", 4);
        map.put("hello", 5);

        List<CustomHashMap.Entry<String, Integer>> entries = map.entries();
        assertEquals(1, entries.size()); // Только одна запись
        assertEquals(5, entries.get(0).value); // С последним значением
    }

    @Test
    void testPutAndGet() {
        CustomHashMap<String, Integer> map = new CustomHashMap<>();

        // Проверка добавления и получения элементов
        map.put("key1", 1);
        map.put("key2", 2);
        map.put("key3", 3);

        assertEquals(1, map.get("key1"));
        assertEquals(2, map.get("key2"));
        assertEquals(3, map.get("key3"));
        assertNull(map.get("nonexistent"));
    }

    @Test
    void testCollisionHandling() {
        CustomHashMap<String, Integer> map = new CustomHashMap<>();

        // Создаем ключи с одинаковым хеш-кодом (для демонстрации коллизии)
        map.put("Aa", 1);
        map.put("BB", 2);

        assertEquals(1, map.get("Aa"));
        assertEquals(2, map.get("BB"));
    }

    @Test
    void testEntries() {
        CustomHashMap<String, Integer> map = new CustomHashMap<>();

        map.put("apple", 3);
        map.put("banana", 2);
        map.put("cherry", 1);

        List<CustomHashMap.Entry<String, Integer>> entries = map.entries();
        assertEquals(3, entries.size());

        // Проверяем наличие всех элементов
        boolean hasApple = false, hasBanana = false, hasCherry = false;
        for (var entry : entries) {
            if ("apple".equals(entry.key) && entry.value == 3) hasApple = true;
            if ("banana".equals(entry.key) && entry.value == 2) hasBanana = true;
            if ("cherry".equals(entry.key) && entry.value == 1) hasCherry = true;
        }

        assertTrue(hasApple);
        assertTrue(hasBanana);
        assertTrue(hasCherry);
    }

    @Test
    void testUpdateValue() {
        CustomHashMap<String, Integer> map = new CustomHashMap<>();

        map.put("key", 1);
        assertEquals(1, map.get("key"));

        map.put("key", 5); // Обновление значения
        assertEquals(5, map.get("key"));
    }
    @Test
    void testKeyCollisions() {
        // Создаём мапу с маленьким размером для провокации коллизий
        CustomHashMap<String, Integer> map = new CustomHashMap<>();
        map.put("Aa", 1);  // Хеш-код 2112
        map.put("BB", 2);  // Хеш-код 2112 - коллизия!

        assertEquals(1, map.get("Aa"));
        assertEquals(2, map.get("BB"));

        // Проверяем обновление значения при коллизии
        map.put("Aa", 3);
        assertEquals(3, map.get("Aa"));
        assertEquals(2, map.get("BB"));
    }
    @Test
    void testMapOverflow() {
        CustomHashMap<Integer, String> map = new CustomHashMap<>();
        int capacity = map.getCapacity();

        // Добавляем элементов больше, чем начальная ёмкость
        for (int i = 0; i < capacity * 2; i++) {
            map.put(i, "value" + i);
        }

        // Проверяем, что все элементы доступны
        for (int i = 0; i < capacity * 2; i++) {
            assertEquals("value" + i, map.get(i));
        }
    }
    @Test
    void testMapPerformance() {
        CustomHashMap<Integer, String> map = new CustomHashMap<>();
        int elements = 100_000;

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < elements; i++) {
            map.put(i, "value" + i);
        }
        long putTime = System.currentTimeMillis() - startTime;

        startTime = System.currentTimeMillis();
        for (int i = 0; i < elements; i++) {
            assertEquals("value" + i, map.get(i));
        }
        long getTime = System.currentTimeMillis() - startTime;

        System.out.printf("Map performance: %,d puts in %,d ms (%,d ops/ms)%n",
                elements, putTime, elements / Math.max(1, putTime));
        System.out.printf("Map performance: %,d gets in %,d ms (%,d ops/ms)%n",
                elements, getTime, elements / Math.max(1, getTime));

        assertTrue(putTime < 100, "Put operations too slow: " + putTime + "ms");
        assertTrue(getTime < 100, "Get operations too slow: " + getTime + "ms");
    }
    @Test
    void testNullKey() {
        CustomHashMap<String, Integer> map = new CustomHashMap<>();

        // Проверяем добавление и получение null-ключа
        map.put(null, 42);
        assertEquals(42, map.get(null));

        // Проверяем обновление значения
        map.put(null, 100);
        assertEquals(100, map.get(null));

        // Проверяем, что другие ключи не затронуты
        map.put("key", 1);
        assertEquals(1, map.get("key"));
        assertNull(map.get("non-existent"));
    }
}