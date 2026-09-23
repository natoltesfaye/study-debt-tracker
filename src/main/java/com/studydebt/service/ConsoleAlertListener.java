package com.studydebt.service;

import com.studydebt.model.Topic;

/**
 * Simple concrete Observer: prints a warning to the console.
 * Swappable later for an email/desktop-notification listener without
 * touching DebtMonitor.
 */
public class ConsoleAlertListener implements DebtAlertListener {

    @Override
    public void onCriticalDebt(Topic topic, double debtScore) {
        System.out.printf("⚠️  CRITICAL: \"%s\" has a debt score of %.1f — it needs attention.%n",
                topic.getName(), debtScore);
    }
}
