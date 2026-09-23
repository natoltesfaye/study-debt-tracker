package com.studydebt.ui;

import com.studydebt.model.Status;
import com.studydebt.model.Topic;
import com.studydebt.persistence.JsonTopicStore;
import com.studydebt.repository.TopicRepository;
import com.studydebt.service.DebtCalculator;
import com.studydebt.service.DebtMonitor;
import com.studydebt.service.LinearDecayStrategy;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * JavaFX desktop front-end for Study Debt Tracker.
 *
 * Reuses every existing layer unchanged (model / service / repository /
 * persistence) — this class only adds a UI on top, wired via the same
 * Observer (DebtMonitor + GuiAlertListener) and Repository seams the
 * console app already exposed.
 */
public class MainApp extends Application {

    private static final String DATA_FILE = "data.json";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final TopicRepository repository = new TopicRepository();
    private final DebtCalculator calculator = new DebtCalculator(new LinearDecayStrategy());
    private final DebtMonitor monitor = new DebtMonitor(calculator);
    private final JsonTopicStore store = new JsonTopicStore(DATA_FILE);

    private final ObservableList<Topic> tableData = FXCollections.observableArrayList();
    private final ObservableList<String> alertLog = FXCollections.observableArrayList();

    private TableView<Topic> table;
    private Label totalTopicsValue;
    private Label totalDebtValue;
    private Label criticalValue;
    private Label avgProgressValue;
    private Label statusBar;

    private XYChart.Series<String, Number> debtSeries;
    private BarChart<String, Number> debtChart;
    private PieChart statusChart;

    @Override
    public void start(Stage stage) {
        monitor.subscribe(new GuiAlertListener(alertLog));

        List<Topic> loaded = store.load();
        repository.replaceAll(loaded);

        BorderPane root = new BorderPane();
        root.setTop(buildHeader());

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab dashboardTab = new Tab("Dashboard", buildDashboard());
        Tab statsTab = new Tab("Statistics", buildStatisticsPane());
        tabs.getTabs().addAll(dashboardTab, statsTab);
        root.setCenter(tabs);

        statusBar = new Label("Ready.");
        statusBar.setPadding(new Insets(6, 10, 6, 10));
        statusBar.setStyle("-fx-background-color: #e8ecf1; -fx-font-size: 11px; -fx-text-fill: #555;");
        root.setBottom(statusBar);

        refreshAll();
        if (!loaded.isEmpty()) {
            statusBar.setText("Loaded " + loaded.size() + " topic(s) from " + DATA_FILE + ".");
        }
        // Surface any pre-existing critical debt in the alert log without a popup.
        monitor.checkAll(repository.findAll());

        Scene scene = new Scene(root, 1080, 680);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setTitle("Study Debt Tracker");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> saveData());
        stage.show();
    }

    // ---------------------------------------------------------------
    // Header: title + stat cards
    // ---------------------------------------------------------------

    private VBox buildHeader() {
        Label title = new Label("Study Debt Tracker");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));

        Label subtitle = new Label("Unfinished topics, ranked by how much they're costing you.");
        subtitle.setStyle("-fx-text-fill: #666;");

        totalTopicsValue = new Label("0");
        totalDebtValue = new Label("0.0");
        criticalValue = new Label("0");
        avgProgressValue = new Label("0%");

        HBox cards = new HBox(14,
                statCard("Total Topics", totalTopicsValue, "#3b6ea5"),
                statCard("Total Debt", totalDebtValue, "#c0533f"),
                statCard("Critical Alerts", criticalValue, "#c94f4f"),
                statCard("Avg. Progress", avgProgressValue, "#3f9e63"));
        cards.setPadding(new Insets(10, 0, 0, 0));

        VBox header = new VBox(4, title, subtitle, cards);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.setStyle("-fx-background-color: #f5f7fa; -fx-border-color: #dde3ea; -fx-border-width: 0 0 1 0;");
        return header;
    }

    private VBox statCard(String label, Label valueLabel, String accentColor) {
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        valueLabel.setStyle("-fx-text-fill: " + accentColor + ";");
        Label caption = new Label(label);
        caption.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");

        VBox card = new VBox(2, valueLabel, caption);
        card.setPadding(new Insets(10, 18, 10, 18));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: #dde3ea; -fx-border-radius: 8; -fx-border-width: 0 0 0 4; " +
                "-fx-border-color: transparent transparent transparent " + accentColor + ";");
        card.setPrefWidth(190);
        return card;
    }

    // ---------------------------------------------------------------
    // Dashboard tab: table + buttons + alert log
    // ---------------------------------------------------------------

    private SplitPane buildDashboard() {
        table = buildTable();
        VBox tableBox = new VBox(10, buildButtonBar(), table);
        tableBox.setPadding(new Insets(16));
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox alertBox = buildAlertPanel();

        SplitPane split = new SplitPane(tableBox, alertBox);
        split.setDividerPositions(0.74);
        return split;
    }

    @SuppressWarnings("unchecked")
    private TableView<Topic> buildTable() {
        TableView<Topic> tv = new TableView<>(tableData);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setPlaceholder(new Label("No topics yet — add one to get started."));

        TableColumn<Topic, String> nameCol = new TableColumn<>("Topic");
        nameCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getName()));
        nameCol.setPrefWidth(220);

        TableColumn<Topic, Integer> importanceCol = new TableColumn<>("Importance");
        importanceCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getImportanceWeight()));
        importanceCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Topic, Double> progressCol = new TableColumn<>("Progress");
        progressCol.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getProgressPercent() / 100.0));
        progressCol.setCellFactory(col -> new ProgressCell());
        progressCol.setPrefWidth(160);

        TableColumn<Topic, Status> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getStatus()));
        statusCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Topic, String> lastTouchedCol = new TableColumn<>("Last Touched");
        lastTouchedCol.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(cd.getValue().getLastTouched().format(DATE_FMT)));

        TableColumn<Topic, Long> idleCol = new TableColumn<>("Days Idle");
        idleCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().daysSinceLastTouched()));
        idleCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Topic, Double> debtCol = new TableColumn<>("Debt Score");
        debtCol.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(calculator.calculateDebt(cd.getValue())));
        debtCol.setCellFactory(col -> new DebtCell());
        debtCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        tv.getColumns().addAll(List.of(nameCol, importanceCol, progressCol, statusCol,
                lastTouchedCol, idleCol, debtCol));

        // Highlight rows that are currently critical.
        tv.setRowFactory(this::buildRow);

        return tv;
    }

    private TableRow<Topic> buildRow(TableView<Topic> tv) {
        return new TableRow<>() {
            @Override
            protected void updateItem(Topic topic, boolean empty) {
                super.updateItem(topic, empty);
                if (empty || topic == null) {
                    setStyle("");
                } else if (calculator.isCritical(topic)) {
                    setStyle("-fx-background-color: #fdecea;");
                } else {
                    setStyle("");
                }
            }
        };
    }

    private HBox buildButtonBar() {
        Button addBtn = new Button("Add Topic");
        addBtn.setOnAction(e -> showAddTopicDialog());

        Button logBtn = new Button("Log Progress");
        logBtn.setOnAction(e -> showLogProgressDialog());

        Button deleteBtn = new Button("Delete Topic");
        deleteBtn.setOnAction(e -> deleteSelectedTopic());

        Button checkBtn = new Button("Check Critical Alerts");
        checkBtn.setOnAction(e -> checkCriticalAlerts());

        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> saveData());

        for (Button b : List.of(addBtn, logBtn, deleteBtn, checkBtn, saveBtn)) {
            b.setPrefHeight(32);
        }
        addBtn.setStyle("-fx-base: #3f9e63; -fx-text-fill: white;");
        checkBtn.setStyle("-fx-base: #c94f4f; -fx-text-fill: white;");

        HBox bar = new HBox(10, addBtn, logBtn, deleteBtn, checkBtn, saveBtn);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private VBox buildAlertPanel() {
        Label header = new Label("Alert Log");
        header.setFont(Font.font("System", FontWeight.BOLD, 14));

        ListView<String> listView = new ListView<>(alertLog);
        listView.setPlaceholder(new Label("No alerts yet."));
        VBox.setVgrow(listView, Priority.ALWAYS);

        VBox box = new VBox(8, header, listView);
        box.setPadding(new Insets(16));
        return box;
    }

    // ---------------------------------------------------------------
    // Statistics tab
    // ---------------------------------------------------------------

    private HBox buildStatisticsPane() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Topic");
        yAxis.setLabel("Debt Score");
        debtChart = new BarChart<>(xAxis, yAxis);
        debtChart.setTitle("Debt by Topic (highest first)");
        debtChart.setLegendVisible(false);
        debtChart.setAnimated(false);
        debtSeries = new XYChart.Series<>();
        debtChart.getData().add(debtSeries);

        statusChart = new PieChart();
        statusChart.setTitle("Topics by Status");
        statusChart.setAnimated(false);

        HBox.setHgrow(debtChart, Priority.ALWAYS);
        HBox.setHgrow(statusChart, Priority.ALWAYS);
        debtChart.setPrefWidth(600);
        statusChart.setPrefWidth(400);

        HBox pane = new HBox(16, debtChart, statusChart);
        pane.setPadding(new Insets(16));
        return pane;
    }

    private void refreshCharts(List<Topic> ranked) {
        debtSeries.getData().clear();
        int limit = Math.min(ranked.size(), 10);
        for (int i = 0; i < limit; i++) {
            Topic t = ranked.get(i);
            debtSeries.getData().add(new XYChart.Data<>(t.getName(), calculator.calculateDebt(t)));
        }

        statusChart.getData().clear();
        for (Status s : Status.values()) {
            long count = repository.findAll().stream().filter(t -> t.getStatus() == s).count();
            if (count > 0) {
                statusChart.getData().add(new PieChart.Data(s.name() + " (" + count + ")", count));
            }
        }
    }

    // ---------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------

    private void showAddTopicDialog() {
        Dialog<Topic> dialog = new Dialog<>();
        dialog.setTitle("Add Topic");
        dialog.setHeaderText("Add a new topic to track");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Recursion & backtracking");
        Spinner<Integer> importanceSpinner = new Spinner<>(1, 5, 3);
        importanceSpinner.setEditable(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Importance (1-5):"), 0, 1);
        grid.add(importanceSpinner, 1, 1);
        dialog.getDialogPane().setContent(grid);

        Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(bt -> {
            if (bt == addButtonType) {
                try {
                    return new Topic(nameField.getText().trim(), importanceSpinner.getValue());
                } catch (IllegalArgumentException ex) {
                    showError("Could not add topic", ex.getMessage());
                }
            }
            return null;
        });

        Optional<Topic> result = dialog.showAndWait();
        result.ifPresent(topic -> {
            repository.save(topic);
            refreshAll();
            statusBar.setText("Added \"" + topic.getName() + "\".");
        });
    }

    private void showLogProgressDialog() {
        Topic selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No topic selected", "Select a topic in the table first.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("10");
        dialog.setTitle("Log Progress");
        dialog.setHeaderText("Log progress for \"" + selected.getName() + "\"");
        dialog.setContentText("Progress to add (%):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(text -> {
            try {
                int amount = Integer.parseInt(text.trim());
                selected.logProgress(amount);
                monitor.check(selected);
                if (calculator.isCritical(selected)) {
                    showWarning("Still critical", "\"" + selected.getName() +
                            "\" still has a critical debt score of " +
                            String.format("%.1f", calculator.calculateDebt(selected)) + ".");
                }
                refreshAll();
                statusBar.setText("Logged " + amount + "% progress on \"" + selected.getName() + "\".");
            } catch (NumberFormatException ex) {
                showError("Invalid input", "Enter a whole number for progress.");
            } catch (IllegalArgumentException ex) {
                showError("Could not log progress", ex.getMessage());
            }
        });
    }

    private void deleteSelectedTopic() {
        Topic selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No topic selected", "Select a topic in the table first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + selected.getName() + "\"? This cannot be undone.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                repository.deleteById(selected.getId());
                refreshAll();
                statusBar.setText("Deleted \"" + selected.getName() + "\".");
            }
        });
    }

    private void checkCriticalAlerts() {
        List<Topic> topics = repository.findAll();
        if (topics.isEmpty()) {
            showError("No topics yet", "Add a topic first.");
            return;
        }
        long before = alertLog.size();
        monitor.checkAll(topics);
        long newAlerts = alertLog.size() - before;

        long criticalCount = topics.stream().filter(calculator::isCritical).count();
        if (criticalCount == 0) {
            Alert info = new Alert(Alert.AlertType.INFORMATION, "No topics are currently critical. Nice work!");
            info.setTitle("Critical Debt Check");
            info.setHeaderText(null);
            info.showAndWait();
        } else {
            Alert warn = new Alert(Alert.AlertType.WARNING,
                    criticalCount + " topic(s) have crossed the critical debt threshold.\n" +
                            "See the Alert Log for details.");
            warn.setTitle("Critical Debt Check");
            warn.setHeaderText(criticalCount + " critical topic(s) found");
            warn.showAndWait();
        }
        statusBar.setText("Checked " + topics.size() + " topic(s); " + newAlerts + " new alert(s) logged.");
        refreshAll();
    }

    private void saveData() {
        try {
            store.save(repository.findAll());
            statusBar.setText("Saved " + repository.count() + " topic(s) to " + DATA_FILE + ".");
        } catch (Exception ex) {
            showError("Save failed", ex.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Refresh / helpers
    // ---------------------------------------------------------------

    private void refreshAll() {
        List<Topic> ranked = calculator.rankByDebt(repository.findAll());
        tableData.setAll(ranked);
        if (table != null) {
            table.refresh();
        }

        int total = ranked.size();
        double totalDebt = calculator.totalDebt(ranked);
        long critical = ranked.stream().filter(calculator::isCritical).count();
        double avgProgress = ranked.isEmpty() ? 0.0 :
                ranked.stream().mapToInt(Topic::getProgressPercent).average().orElse(0.0);

        totalTopicsValue.setText(String.valueOf(total));
        totalDebtValue.setText(String.format("%.1f", totalDebt));
        criticalValue.setText(String.valueOf(critical));
        avgProgressValue.setText(String.format("%.0f%%", avgProgress));

        if (debtChart != null) {
            refreshCharts(ranked);
        }
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR, content, ButtonType.OK);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    private void showWarning(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING, content, ButtonType.OK);
        alert.setTitle("Warning");
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    // ---------------------------------------------------------------
    // Custom cells
    // ---------------------------------------------------------------

    /** Renders a progress ratio (0.0-1.0) as a ProgressBar with a percentage label. */
    private static class ProgressCell extends TableCell<Topic, Double> {
        private final ProgressBar bar = new ProgressBar();
        private final Label label = new Label();
        private final StackPane pane = new StackPane(bar, label);

        ProgressCell() {
            bar.setMaxWidth(Double.MAX_VALUE);
            bar.setPrefHeight(16);
            label.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");
            pane.setPadding(new Insets(2, 0, 2, 0));
        }

        @Override
        protected void updateItem(Double ratio, boolean empty) {
            super.updateItem(ratio, empty);
            if (empty || ratio == null) {
                setGraphic(null);
            } else {
                bar.setProgress(ratio);
                bar.setStyle(ratio >= 1.0 ? "-fx-accent: #3f9e63;" :
                        ratio >= 0.5 ? "-fx-accent: #3b6ea5;" : "-fx-accent: #e0a030;");
                label.setText(String.format("%.0f%%", ratio * 100));
                setGraphic(pane);
            }
        }
    }

    /** Renders a debt score, highlighted red when it's at/above the critical threshold. */
    private class DebtCell extends TableCell<Topic, Double> {
        @Override
        protected void updateItem(Double debt, boolean empty) {
            super.updateItem(debt, empty);
            if (empty || debt == null) {
                setText(null);
                setTextFill(Color.BLACK);
                return;
            }
            boolean critical = debt >= calculator.getCriticalThreshold();
            setText(String.format("%.1f", debt));
            setTextFill(critical ? Color.web("#c94f4f") : Color.web("#333333"));
            setStyle(critical ? "-fx-font-weight: bold;" : "");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
