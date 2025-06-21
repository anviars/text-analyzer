package com.chebotarev.textanalyzer.model;


// Класс для хранения слова и его частоты
public class WordFrequency {
    private final String word;  // неизменяемое слово
    private final int frequency; // частота встречаемости

    // Конструктор
    public WordFrequency(String word, int frequency) {
        this.word = word;
        this.frequency = frequency;
    }

    // Геттеры
    public String getWord() {
        return word;
    }

    public int getFrequency() {
        return frequency;
    }
}
