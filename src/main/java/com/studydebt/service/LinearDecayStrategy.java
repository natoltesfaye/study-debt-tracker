package com.studydebt.service;

import com.studydebt.model.Topic;

public class LinearDecayStrategy implements DecayStrategy {

    @Override
    public double calculateDebt(Topic topic) {
        if (topic.getStatus() == com.studydebt.model.Status.DONE) {
            return 0.0;
        }

        double progressRatio = topic.getProgressPercent() / 100.0;
        long daysSinceTouched = topic.daysSinceLastTouched();

        return topic.getImportanceWeight() * daysSinceTouched * (1 - progressRatio);
    }
}
