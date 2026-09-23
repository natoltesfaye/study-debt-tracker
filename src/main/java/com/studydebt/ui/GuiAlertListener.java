package com.studydebt.ui;

import com.studydebt.model.Topic;
import com.studydebt.service.DebtAlertListener;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Concrete Observer (see DebtAlertListener) that feeds critical-debt
 * alerts into an ObservableList so the JavaFX dashboard can render them
 * in an alerts panel, instead of printing to the console.
 *
 * Swapped in for ConsoleAlertListener without touching DebtMonitor at all
 * — this is exactly the point of the Observer pattern already in place.
 */
public class GuiAlertListener implements DebtAlertListener {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ObservableList<String> alertLog;

    public GuiAlertListener(ObservableList<String> alertLog) {
        this.alertLog = alertLog;
    }

    @Override
    public void onCriticalDebt(Topic topic, double debtScore) {
        String message = String.format("[%s] \"%s\" is critical — debt score %.1f",
                LocalTime.now().format(TIME_FMT), topic.getName(), debtScore);

        if (Platform.isFxApplicationThread()) {
            alertLog.add(0, message);
        } else {
            Platform.runLater(() -> alertLog.add(0, message));
        }
    }
}
