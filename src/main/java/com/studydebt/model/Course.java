package com.studydebt.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Course {

    private final String id;
    private String name;
    private final List<Topic> topics;

    public Course(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Course name cannot be blank");
        }
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.topics = new ArrayList<>();
    }

    public void addTopic(Topic topic) {
        if (topic == null) {
            throw new IllegalArgumentException("Cannot add a null topic");
        }
        if (topics.contains(topic)) {
            throw new IllegalArgumentException("Topic already exists in this course: " + topic.getName());
        }
        topics.add(topic);
    }

    public void removeTopic(Topic topic) {
        topics.remove(topic);
    }

    public double getAverageProgress() {
        if (topics.isEmpty()) {
            return 0.0;
        }
        return topics.stream()
                .mapToInt(Topic::getProgressPercent)
                .average()
                .orElse(0.0);
    }

    public Optional<Topic> getMostNeglectedTopic() {
        return topics.stream()
                .max(Comparator.comparingLong(Topic::daysSinceLastTouched));
    }

    public long countByStatus(Status status) {
        return topics.stream()
                .filter(t -> t.getStatus() == status)
                .count();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Topic> getTopics() {
        return List.copyOf(topics);
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Course name cannot be blank");
        }
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("Course[name=%s, topics=%d, avgProgress=%.1f%%]",
                name, topics.size(), getAverageProgress());
    }
}
