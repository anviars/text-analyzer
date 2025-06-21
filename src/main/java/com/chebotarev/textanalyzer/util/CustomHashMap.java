package com.chebotarev.textanalyzer.util;

import java.util.ArrayList;
import java.util.List;

public class CustomHashMap<K, V> {
    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    private Entry<K, V>[] buckets;
    private int size = 0;
    private int threshold;

    @SuppressWarnings("unchecked")
    public CustomHashMap() {
        buckets = (Entry<K, V>[]) new Entry[DEFAULT_CAPACITY];
        threshold = (int) (DEFAULT_CAPACITY * LOAD_FACTOR);
    }

    public static class Entry<K, V> {
        public K key;
        public V value;
        public Entry<K, V> next;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public V get(K key) {
        int index = getIndex(key, buckets.length);
        Entry<K, V> current = buckets[index];

        while (current != null) {
            if (key == null && current.key == null) {
                return current.value;
            }
            if (key != null && key.equals(current.key)) {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }

    public void put(K key, V value) {
        // Проверяем необходимость ресайза перед добавлением
        if (size >= threshold) {
            resize();
        }

        int index = getIndex(key, buckets.length);
        Entry<K, V> newEntry = new Entry<>(key, value);

        // Случай: пустой бакет
        if (buckets[index] == null) {
            buckets[index] = newEntry;
            size++;
            return;
        }

        // Поиск существующей записи или конца цепочки
        Entry<K, V> current = buckets[index];
        Entry<K, V> prev = null;

        while (current != null) {
            // Проверка на совпадение ключа
            if ((key == null && current.key == null) ||
                    (key != null && key.equals(current.key))) {

                // Обновление существующего значения
                current.value = value;
                return;
            }
            prev = current;
            current = current.next;
        }

        // Добавление новой записи в конец цепочки
        prev.next = newEntry;
        size++;
    }
    public void clear() {
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = null;
        }
        size = 0;
    }
    public boolean isEmpty() {
        return size == 0;
    }
    private void resize() {
        int newCapacity = buckets.length * 2;
        @SuppressWarnings("unchecked")
        Entry<K, V>[] newBuckets = (Entry<K, V>[]) new Entry[newCapacity];
        threshold = (int) (newCapacity * LOAD_FACTOR);

        // Перехеширование всех элементов
        for (int i = 0; i < buckets.length; i++) {
            Entry<K, V> current = buckets[i];
            while (current != null) {
                Entry<K, V> next = current.next;

                // Вычисляем новый индекс
                int newIndex = getIndex(current.key, newCapacity);

                // Вставляем в начало цепочки нового бакета
                current.next = newBuckets[newIndex];
                newBuckets[newIndex] = current;

                current = next;
            }
        }
        buckets = newBuckets;
    }

    private int getIndex(K key, int capacity) {
        if (key == null) return 0;
        return Math.abs(key.hashCode()) % capacity;
    }

    public int size() {
        return size;
    }

    public int getCapacity() {
        return buckets.length;
    }

    public List<Entry<K, V>> entries() {
        List<Entry<K, V>> allEntries = new ArrayList<>();
        for (Entry<K, V> bucket : buckets) {
            Entry<K, V> current = bucket;
            while (current != null) {
                allEntries.add(current);
                current = current.next;
            }
        }
        return allEntries;
    }
}