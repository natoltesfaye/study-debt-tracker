package com.studydebt.service;

import com.studydebt.model.Topic;

import java.util.ArrayList;
import java.util.List;

/**
 * Observer pattern subject. Holds a list of listeners and notifies all
 * of them whenever a checked Topic is critical. Decouples "detecting
 * critical debt" from "what to do about it" (print, log, email, etc).
 */
public class DebtMonitor {

    private final DebtCalculator calculator;
    private final List<DebtAlertListener> listeners = new ArrayList<>();

    public DebtMonitor(DebtCalculator calculator) {
        if (calculator == null) {
            throw new IllegalArgumentException("DebtCalculator cannot be null");
        }
        this.calculator = calculator;
    }

    public void subscribe(DebtAlertListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        listeners.add(listener);
    }

    public void unsubscribe(DebtAlertListener listener) {
        listeners.remove(listener);
    }

    /** Checks one topic and notifies all listeners if it's critical. */
    public void check(Topic topic) {
        double debt = calculator.calculateDebt(topic);
        if (calculator.isCritical(topic)) {
            for (DebtAlertListener listener : listeners) {
                listener.onCriticalDebt(topic, debt);
            }
        }
    }

    /** Convenience: checks every topic in the list. */
    public void checkAll(List<Topic> topics) {
        topics.forEach(this::check);
    }
}
