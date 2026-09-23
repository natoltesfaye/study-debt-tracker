package com.studydebt.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Topic {

    private final String id;
    private String name;
    private int importanceWeight;
    private int progressPercent;
    private Status status;
    private final LocalDate dateStarted;
    private LocalDate lastTouched;

    public Topic(String name, int importanceWeight) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Topic name cannot be blank");
        }
        this.id = UUID.randomUUID().toString();
        this.name = name;
        setImportanceWeight(importanceWeight);
        this.progressPercent = 0;
        this.status = Status.NOT_STARTED;
        this.dateStarted = LocalDate.now();
        this.lastTouched = LocalDate.now();
    }

    /**
     * Rehydration constructor — rebuilds a Topic exactly as it was saved
     * (same id and dates), used only by the persistence layer when loading
     * from disk. Regular application code should use the public constructor
     * above, which always creates a brand-new topic.
     */
    public Topic(String id, String name, int importanceWeight, int progressPercent,
                 LocalDate dateStarted, LocalDate lastTouched) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Topic name cannot be blank");
        }
        this.id = id;
        this.name = name;
        setImportanceWeight(importanceWeight);
        setProgressPercent(progressPercent);
        this.dateStarted = dateStarted;
        this.lastTouched = lastTouched;
    }

    public void logProgress(int additionalPercent) {
        if (additionalPercent < 0) {
            throw new IllegalArgumentException("additionalPercent cannot be negative");
        }
        setProgressPercent(this.progressPercent + additionalPercent);
        this.lastTouched = LocalDate.now();
    }

    public long daysSinceLastTouched() {
        return java.time.temporal.ChronoUnit.DAYS.between(lastTouched, LocalDate.now());
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getImportanceWeight() { return importanceWeight; }
    public int getProgressPercent() { return progressPercent; }
    public Status getStatus() { return status; }
    public LocalDate getDateStarted() { return dateStarted; }
    public LocalDate getLastTouched() { return lastTouched; }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Topic name cannot be blank");
        }
        this.name = name;
    }

    public void setImportanceWeight(int importanceWeight) {
        if (importanceWeight < 1 || importanceWeight > 5) {
            throw new IllegalArgumentException("importanceWeight must be between 1 and 5");
        }
        this.importanceWeight = importanceWeight;
    }

    public void setProgressPercent(int progressPercent) {
        if (progressPercent < 0 || progressPercent > 100) {
            throw new IllegalArgumentException("progressPercent must be between 0 and 100");
        }
        this.progressPercent = progressPercent;
        if (progressPercent == 0) {
            this.status = Status.NOT_STARTED;
        } else if (progressPercent == 100) {
            this.status = Status.DONE;
        } else {
            this.status = Status.IN_PROGRESS;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Topic)) return false;
        Topic topic = (Topic) o;
        return id.equals(topic.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Topic[name=%s, progress=%d%%, status=%s, lastTouched=%s]",
                name, progressPercent, status, lastTouched);
    }
}
