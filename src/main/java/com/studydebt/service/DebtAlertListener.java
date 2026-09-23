package com.studydebt.service;

import com.studydebt.model.Topic;

/**
 * Observer pattern: implement this to react whenever a Topic crosses
 * into critical debt. DebtMonitor is the subject; listeners subscribe
 * without DebtMonitor needing to know anything about them.
 */
public interface DebtAlertListener {
    void onCriticalDebt(Topic topic, double debtScore);
}
