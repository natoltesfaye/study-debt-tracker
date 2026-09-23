package com.studydebt.service;

import com.studydebt.model.Topic;

public interface DecayStrategy {
    double calculateDebt(Topic topic);
}
