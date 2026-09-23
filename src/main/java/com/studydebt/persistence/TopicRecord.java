package com.studydebt.persistence;

import java.time.LocalDate;

/**
 * Plain data-transfer object mirroring Topic's fields, used only for
 * JSON serialization. Keeping this separate from Topic means the domain
 * model stays clean (no Jackson annotations leaking into business logic)
 * and free to change its internal representation independently of the
 * saved file format.
 */
public class TopicRecord {

    public String id;
    public String name;
    public int importanceWeight;
    public int progressPercent;
    public LocalDate dateStarted;
    public LocalDate lastTouched;

    // Empty constructor required by Jackson for deserialization.
    public TopicRecord() {
    }

    public TopicRecord(String id, String name, int importanceWeight, int progressPercent,
                        LocalDate dateStarted, LocalDate lastTouched) {
        this.id = id;
        this.name = name;
        this.importanceWeight = importanceWeight;
        this.progressPercent = progressPercent;
        this.dateStarted = dateStarted;
        this.lastTouched = lastTouched;
    }
}
