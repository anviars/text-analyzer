package com.chebotarev.textanalyzer;

import com.chebotarev.textanalyzer.model.WordFrequency;
import com.chebotarev.textanalyzer.service.TextAnalysisService;
import com.chebotarev.textanalyzer.util.CustomHashMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class TextAnalyzerApp {
    private static String currentText = "";
    private static CustomHashMap<String, Boolean> dictionary = new CustomHashMap<>();
    private static TextAnalysisService service;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Text Analyzer v2.0 ===");
        service = new TextAnalysisService(dictionary);

        if (args.length > 0) {
            processCommandLineArgs(args);
        } else {
            showMainMenu();
        }
    }

    private static void processCommandLineArgs(String[] args) {
        try {
            // Загрузка текста
            Path textPath = Paths.get(args[0]);
            currentText = Files.readString(textPath);
            System.out.println("Loaded text from: " + textPath);

            // Загрузка словаря
            if (args.length > 1) {
                Path dictPath = Paths.get(args[1]);
                loadDictionary(dictPath);
                System.out.println("Loaded dictionary from: " + dictPath);
            }

            // Выполнение полного анализа
            performFullAnalysis();
        } catch (IOException e) {
            System.err.println("Error loading files: " + e.getMessage());
            showMainMenu();
        }
    }

    private static void showMainMenu() {
        while (true) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. Load text from file");
            System.out.println("2. Load dictionary from file");
            System.out.println("3. Enter text manually");
            System.out.println("4. Perform text analysis");
            System.out.println("5. Check spelling");
            System.out.println("6. Search for words");
            System.out.println("7. Word frequency statistics");
            System.out.println("8. Save analysis results");
            System.out.println("9. Exit");
            System.out.print("Select an option: ");

            int choice = readIntInput();
            switch (choice) {
                case 1: loadTextFromFile(); break;
                case 2: loadDictionaryFromFile(); break;
                case 3: enterTextManually(); break;
                case 4: performFullAnalysis(); break;
                case 5: checkSpelling(); break;
                case 6: searchForWords(); break;
                case 7: showWordFrequency(); break;
                case 8: saveResults(); break;
                case 9: exitApp(); return;
                default: System.out.println("Invalid option!");
            }
        }
    }

    private static void loadTextFromFile() {
        System.out.print("Enter text file path: ");
        String path = scanner.nextLine().trim();
        try {
            currentText = Files.readString(Paths.get(path));
            System.out.println("Text loaded successfully!");
            System.out.println("Preview: " + currentText.substring(0, Math.min(100, currentText.length())) + "...");
        } catch (IOException e) {
            System.err.println("Error loading file: " + e.getMessage());
        }
    }

    private static void loadDictionaryFromFile() {
        System.out.print("Enter dictionary file path: ");
        String path = scanner.nextLine().trim();
        try {
            Path dictPath = Paths.get(path);
            loadDictionary(dictPath);
            System.out.println("Dictionary loaded successfully!");
            System.out.println("Words loaded: " + dictionary.entries().size());
        } catch (IOException e) {
            System.err.println("Error loading dictionary: " + e.getMessage());
        }
    }

    private static void loadDictionary(Path path) throws IOException {
        dictionary = new CustomHashMap<>();
        List<String> words = Files.readAllLines(path);
        for (String word : words) {
            if (!word.trim().isEmpty()) {
                dictionary.put(word.trim().toLowerCase(), true);
            }
        }
        service = new TextAnalysisService(dictionary);
    }

    private static void enterTextManually() {
        System.out.println("Enter your text (type 'END' on a new line to finish):");
        StringBuilder sb = new StringBuilder();
        while (true) {
            String line = scanner.nextLine();
            if (line.equalsIgnoreCase("END")) break;
            sb.append(line).append("\n");
        }
        currentText = sb.toString();
        System.out.println("Text saved successfully!");
    }

    private static void performFullAnalysis() {
        if (currentText.isEmpty()) {
            System.out.println("No text loaded! Please load text first.");
            return;
        }

        System.out.println("\n=== TEXT ANALYSIS ===");

        // 1. Основная статистика
        int totalWords = service.countTotalWords(currentText);
        CustomHashMap<String, Integer> frequencyMap = service.countWords(currentText);
        List<WordFrequency> sortedFrequencies = service.sortByFrequency(frequencyMap);

        System.out.println("Total words: " + totalWords);
        System.out.println("Unique words: " + sortedFrequencies.size());

        // 2. Топ-10 слов
        System.out.println("\nTop 10 most frequent words:");
        int limit = Math.min(10, sortedFrequencies.size());
        for (int i = 0; i < limit; i++) {
            WordFrequency wf = sortedFrequencies.get(i);
            System.out.printf("%2d. %-15s : %5d occurrences%n",
                    i + 1, wf.getWord(), wf.getFrequency());
        }

        // 3. Проверка орфографии
        checkSpelling();

        // 4. Интерактивный поиск
        searchForWords();
    }

    private static void checkSpelling() {
        if (dictionary.entries().isEmpty()) {
            System.out.println("\nDictionary not loaded! Spelling check skipped.");
            return;
        }

        CustomHashMap<String, List<String>> spellingErrors = service.checkSpelling(currentText);
        List<CustomHashMap.Entry<String, List<String>>> errorEntries = spellingErrors.entries();

        if (errorEntries.isEmpty()) {
            System.out.println("\nNo spelling errors found!");
        } else {
            System.out.println("\nSpelling errors and suggestions:");
            for (CustomHashMap.Entry<String, List<String>> entry : errorEntries) {
                String suggestions = entry.value.isEmpty() ?
                        "No suggestions available" :
                        String.join(", ", entry.value);
                System.out.println("  - " + entry.key + ": " + suggestions);
            }
            System.out.println("Total errors: " + errorEntries.size());
        }
    }

    private static void searchForWords() {
        System.out.println("\n=== WORD SEARCH ===");
        System.out.println("Enter words to search (type 'exit' to quit):");

        while (true) {
            System.out.print("> ");
            String word = scanner.nextLine().trim();

            if (word.equalsIgnoreCase("exit")) {
                break;
            }

            List<Integer> positions = service.findWordPositions(currentText, word);
            if (positions.isEmpty()) {
                System.out.println("Word '" + word + "' not found");
            } else {
                System.out.println("Word '" + word + "' found at positions: " + positions);
                System.out.println("Total occurrences: " + positions.size());
            }
        }
    }

    private static void showWordFrequency() {
        if (currentText.isEmpty()) {
            System.out.println("No text loaded!");
            return;
        }

        CustomHashMap<String, Integer> frequencyMap = service.countWords(currentText);
        List<WordFrequency> sortedFrequencies = service.sortByFrequency(frequencyMap);

        System.out.println("\nWord Frequency Statistics:");
        System.out.println("+-----------------+------------+");
        System.out.println("|      Word       | Frequency  |");
        System.out.println("+-----------------+------------+");

        for (WordFrequency wf : sortedFrequencies) {
            System.out.printf("| %-15s | %10d |%n", wf.getWord(), wf.getFrequency());
        }

        System.out.println("+-----------------+------------+");
        System.out.println("Total unique words: " + sortedFrequencies.size());
    }

    private static void saveResults() {
        if (currentText.isEmpty()) {
            System.out.println("No analysis results to save!");
            return;
        }

        System.out.print("Enter output file path: ");
        String path = scanner.nextLine().trim();

        try {
            StringBuilder report = new StringBuilder("=== TEXT ANALYSIS REPORT ===\n\n");

            // 1. Basic stats
            int totalWords = service.countTotalWords(currentText);
            report.append("Total words: ").append(totalWords).append("\n");

            // 2. Frequency stats
            CustomHashMap<String, Integer> frequencyMap = service.countWords(currentText);
            List<WordFrequency> sortedFrequencies = service.sortByFrequency(frequencyMap);
            report.append("Unique words: ").append(sortedFrequencies.size()).append("\n\n");

            // 3. Top 20 words
            report.append("Top 20 most frequent words:\n");
            int limit = Math.min(20, sortedFrequencies.size());
            for (int i = 0; i < limit; i++) {
                WordFrequency wf = sortedFrequencies.get(i);
                report.append(String.format("%2d. %-15s : %5d%n",
                        i + 1, wf.getWord(), wf.getFrequency()));
            }

            // 4. Spelling errors
            if (!dictionary.entries().isEmpty()) {
                CustomHashMap<String, List<String>> spellingErrors = service.checkSpelling(currentText);
                List<CustomHashMap.Entry<String, List<String>>> errorEntries = spellingErrors.entries();

                if (!errorEntries.isEmpty()) {
                    report.append("\nSpelling errors:\n");
                    for (CustomHashMap.Entry<String, List<String>> entry : errorEntries) {
                        report.append("  - ").append(entry.key).append(": ")
                                .append(String.join(", ", entry.value)).append("\n");
                    }
                }
            }

            // Save to file
            Files.writeString(Paths.get(path), report.toString());
            System.out.println("Report saved to: " + path);

        } catch (IOException e) {
            System.err.println("Error saving report: " + e.getMessage());
        }
    }

    private static void exitApp() {
        System.out.println("Exiting Text Analyzer...");
        scanner.close();
        System.exit(0);
    }

    private static int readIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input! Please enter a number: ");
            }
        }
    }
}