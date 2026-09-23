package com.studydebt.service;

import com.studydebt.model.Topic;

import java.util.Comparator;
import java.util.List;

public class DebtCalculator {

    private static final double CRITICAL_THRESHOLD = 20.0;

    private final DecayStrategy strategy;

    public DebtCalculator(DecayStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("DecayStrategy cannot be null");
        }
        this.strategy = strategy;
    }

    public double calculateDebt(Topic topic) {
        return strategy.calculateDebt(topic);
    }

    public boolean isCritical(Topic topic) {
        return calculateDebt(topic) >= CRITICAL_THRESHOLD;
    }

    /** Exposes the critical threshold so callers (e.g. the UI) don't have to duplicate the magic number. */
    public double getCriticalThreshold() {
        return CRITICAL_THRESHOLD;
    }

    public List<Topic> rankByDebt(List<Topic> topics) {
        return topics.stream()
                .sorted(Comparator.comparingDouble(this::calculateDebt).reversed())
                .toList();
    }

    public double totalDebt(List<Topic> topics) {
        return topics.stream()
                .mapToDouble(this::calculateDebt)
                .sum();
    }
}
