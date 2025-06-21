package com.chebotarev.textanalyzer.gui;

import com.chebotarev.textanalyzer.model.WordFrequency;
import com.chebotarev.textanalyzer.service.TextAnalysisService;
import com.chebotarev.textanalyzer.util.CustomHashMap;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class TextAnalyzerAppGUI extends Application {

    // Основные компоненты интерфейса
    private TextArea textInput;
    private TextArea resultArea;
    private Label statusLabel;
    private Label dictStatusLabel;
    private TableView<WordFrequency> statsTable;
    private TableView<SpellingError> errorsTable;

    // Сервисы и данные
    private TextAnalysisService service;
    private CustomHashMap<String, Boolean> dictionary;
    private CustomHashMap<String, List<String>> currentErrors;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Инициализация сервисов
        initializeServices();

        // Создание главного контейнера
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // 1. Верхняя панель (заголовок и кнопки)
        root.setTop(createHeaderPanel());

        // 2. Центральная область (ввод текста и результаты)
        SplitPane centerPane = new SplitPane();
        centerPane.getItems().addAll(createInputPanel(), createResultPanel());
        centerPane.setDividerPosition(0, 0.5);
        root.setCenter(centerPane);

        // 3. Нижняя панель (статус бар)
        root.setBottom(createStatusBar());

        // Настройка сцены
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Text Analyzer Pro");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeServices() {
        dictionary = new CustomHashMap<>();
        service = new TextAnalysisService(dictionary);
    }

    // ======================= КОМПОНЕНТЫ ИНТЕРФЕЙСА =======================

    private HBox createHeaderPanel() {
        HBox header = new HBox(10);
        header.setPadding(new Insets(0, 0, 15, 0));

        // Кнопки управления
        Button loadTextBtn = createButton("Load Text", this::loadTextFromFile);
        Button loadDictBtn = createButton("Load Dictionary", this::loadDictionaryFromFile);
        Button clearBtn = createButton("Clear All", this::clearAll);
        Button analyzeBtn = createButton("Analyze Text", this::analyzeText);
        analyzeBtn.setStyle("-fx-font-weight: bold;");

        header.getChildren().addAll(loadTextBtn, loadDictBtn, clearBtn, analyzeBtn);
        return header;
    }

    private VBox createInputPanel() {
        VBox inputPanel = new VBox(10);

        // Область ввода текста
        textInput = new TextArea();
        textInput.setPromptText("Enter text to analyze...");
        textInput.setWrapText(true);

        // Панель статистики ввода
        HBox statsPanel = new HBox(10);
        Label charCount = new Label("Characters: 0");
        Label wordCount = new Label("Words: 0");

        // Обновление статистики при вводе
        textInput.textProperty().addListener((obs, oldVal, newVal) -> {
            charCount.setText("Characters: " + newVal.length());
            wordCount.setText("Words: " + service.countTotalWords(newVal));
        });

        statsPanel.getChildren().addAll(charCount, wordCount);
        inputPanel.getChildren().addAll(
                new Label("Input Text:"),
                textInput,
                statsPanel
        );

        return inputPanel;
    }

    private VBox createResultPanel() {
        VBox resultPanel = new VBox(10);

        // Область текстовых результатов
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);

        // Таблица статистики
        statsTable = new TableView<>();
        TableColumn<WordFrequency, String> wordCol = new TableColumn<>("Word");
        wordCol.setCellValueFactory(new PropertyValueFactory<>("word"));
        TableColumn<WordFrequency, Integer> freqCol = new TableColumn<>("Frequency");
        freqCol.setCellValueFactory(new PropertyValueFactory<>("frequency"));
        statsTable.getColumns().addAll(wordCol, freqCol);

        // Таблица ошибок
        errorsTable = new TableView<>();
        TableColumn<SpellingError, String> errorWordCol = new TableColumn<>("Word");
        errorWordCol.setCellValueFactory(new PropertyValueFactory<>("word"));
        TableColumn<SpellingError, String> suggestionsCol = new TableColumn<>("Suggestions");
        suggestionsCol.setCellValueFactory(new PropertyValueFactory<>("suggestions"));
        errorsTable.getColumns().addAll(errorWordCol, suggestionsCol);

        // Добавляем контекстное меню для замены слов
        errorsTable.setRowFactory(tv -> {
            TableRow<SpellingError> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    SpellingError error = row.getItem();
                    showReplaceDialog(error);
                }
            });
            return row;
        });

        // Панель вкладок
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                new Tab("Statistics", statsTable),
                new Tab("Spelling Errors", errorsTable)
        );

        resultPanel.getChildren().addAll(
                new Label("Analysis Results:"),
                resultArea,
                new Label("Detailed Stats:"),
                tabPane
        );

        return resultPanel;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(10, 0, 0, 0));

        statusLabel = new Label("Ready");
        dictStatusLabel = new Label("Dictionary: not loaded");

        statusBar.getChildren().addAll(statusLabel, dictStatusLabel);
        return statusBar;
    }

    private Button createButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setOnAction(e -> action.run());
        return btn;
    }

    // ======================= ОБРАБОТЧИКИ СОБЫТИЙ =======================

    private void loadTextFromFile() {
        File file = showFileChooser("Open Text File", "*.txt");
        if (file != null) {
            try {
                String content = Files.readString(file.toPath());
                textInput.setText(content);
                statusLabel.setText("Text loaded: " + file.getName());
            } catch (IOException e) {
                showError("File Error", "Could not read file: " + e.getMessage());
            }
        }
    }

    private void loadDictionaryFromFile() {
        File file = showFileChooser("Open Dictionary", "*.txt");
        if (file != null) {
            try {
                List<String> words = Files.readAllLines(file.toPath());

                // Используем метод clear() вместо создания нового экземпляра
                dictionary.clear();

                for (String word : words) {
                    if (!word.trim().isEmpty()) {
                        dictionary.put(word.trim().toLowerCase(), true);
                    }
                }

                service = new TextAnalysisService(dictionary);
                dictStatusLabel.setText("Dictionary: " + words.size() + " words");
                statusLabel.setText("Dictionary loaded: " + file.getName());
            } catch (IOException e) {
                showError("Dictionary Error", "Could not load dictionary: " + e.getMessage());
            }
        }
    }

    private void clearAll() {
        // Используем метод clear() для словаря
        dictionary.clear();

        // Очищаем поля интерфейса
        textInput.clear();
        resultArea.clear();
        statsTable.getItems().clear();
        errorsTable.getItems().clear();

        // Сбрасываем статусы
        dictStatusLabel.setText("Dictionary: not loaded");
        statusLabel.setText("All data cleared");
    }

    private void analyzeText() {
        String text = textInput.getText();
        if (text == null || text.trim().isEmpty()) {
            showError("Input Error", "Please enter some text to analyze");
            return;
        }

        try {
            // 1. Основной анализ текста
            int totalWords = service.countTotalWords(text);
            CustomHashMap<String, Integer> freqMap = service.countWords(text);
            List<WordFrequency> sorted = service.sortByFrequency(freqMap);

            // 2. Форматирование результатов
            StringBuilder result = new StringBuilder();
            result.append("=== TEXT ANALYSIS RESULTS ===\n\n");
            result.append("Total words: ").append(totalWords).append("\n");
            result.append("Unique words: ").append(sorted.size()).append("\n\n");

            result.append("Top 10 frequent words:\n");
            int limit = Math.min(10, sorted.size());
            for (int i = 0; i < limit; i++) {
                WordFrequency wf = sorted.get(i);
                result.append(String.format("%2d. %-15s : %d\n",
                        i+1, wf.getWord(), wf.getFrequency()));
            }

            // 3. Проверка орфографии (используем isEmpty())
            if (!dictionary.isEmpty()) {
                currentErrors = service.checkSpelling(text);
                if (!currentErrors.entries().isEmpty()) {
                    result.append("\nSpelling errors found: ").append(currentErrors.entries().size());
                    updateErrorsTable(currentErrors);
                } else {
                    result.append("\nNo spelling errors found");
                }
            } else {
                result.append("\nDictionary not loaded - spelling check skipped");
            }

            // 4. Обновление интерфейса
            resultArea.setText(result.toString());
            updateStatsTable(sorted);
            statusLabel.setText("Analysis completed successfully");

        } catch (Exception e) {
            showError("Analysis Error", "Error during analysis: " + e.getMessage());
        }
    }

    // ======================= ФУНКЦИЯ ЗАМЕНЫ СЛОВ =======================

    private void showReplaceDialog(SpellingError error) {
        if (error.getSuggestions().isEmpty()) {
            showError("No Suggestions", "No replacement suggestions for: " + error.getWord());
            return;
        }

        // Создаем диалог выбора
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                error.getSuggestions().get(0),
                error.getSuggestions()
        );

        dialog.setTitle("Replace Word");
        dialog.setHeaderText("Replace \"" + error.getWord() + "\" with:");
        dialog.setContentText("Select replacement:");

        // Получаем результат
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(replacement -> {
            replaceWordInText(error.getWord(), replacement);
            statusLabel.setText("Replaced \"" + error.getWord() + "\" with \"" + replacement + "\"");

            // Перезапускаем анализ
            analyzeText();
        });
    }

    private void replaceWordInText(String oldWord, String newWord) {
        String text = textInput.getText();
        String newText = text.replaceAll("\\b" + oldWord + "\\b", newWord);
        textInput.setText(newText);
    }

    // ======================= ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =======================

    private File showFileChooser(String title, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", extension)
        );
        return fileChooser.showOpenDialog(null);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        statusLabel.setText("Error: " + title);
    }

    private void updateStatsTable(List<WordFrequency> data) {
        ObservableList<WordFrequency> items = FXCollections.observableArrayList(
                data.stream().limit(10).toList()
        );
        statsTable.setItems(items);
    }

    private void updateErrorsTable(CustomHashMap<String, List<String>> errors) {
        ObservableList<SpellingError> items = FXCollections.observableArrayList();
        for (var entry : errors.entries()) {
            items.add(new SpellingError(entry.key, entry.value));
        }
        errorsTable.setItems(items);
    }

    // ======================= ВНУТРЕННИЕ КЛАССЫ =======================

    /**
     * Модель для отображения орфографических ошибок
     */
    public static class SpellingError {
        private final String word;
        private final List<String> suggestions;

        public SpellingError(String word, List<String> suggestions) {
            this.word = word;
            this.suggestions = suggestions;
        }

        public String getWord() {
            return word;
        }

        public List<String> getSuggestions() {
            return suggestions;
        }
    }
}