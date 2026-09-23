package com.studydebt.repository;

import com.studydebt.model.Topic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory storage for Topics, keyed by id for O(1) lookups.
 * This is the seam where persistence (see JsonTopicStore) plugs in
 * without any code that depends on this class needing to change.
 *
 * LinkedHashMap preserves insertion order, so findAll() returns topics
 * in the order they were added — matches user expectation in a console app.
 */
public class TopicRepository {

    private final Map<String, Topic> topics = new LinkedHashMap<>();

    public Topic save(Topic topic) {
        if (topic == null) {
            throw new IllegalArgumentException("Cannot save a null topic");
        }
        topics.put(topic.getId(), topic);
        return topic;
    }

    public Optional<Topic> findById(String id) {
        return Optional.ofNullable(topics.get(id));
    }

    public List<Topic> findAll() {
        return List.copyOf(topics.values());
    }

    public boolean deleteById(String id) {
        return topics.remove(id) != null;
    }

    public int count() {
        return topics.size();
    }

    public boolean existsById(String id) {
        return topics.containsKey(id);
    }

    /** Replaces all topics currently held (used when loading from disk). */
    public void replaceAll(List<Topic> newTopics) {
        topics.clear();
        for (Topic t : newTopics) {
            topics.put(t.getId(), t);
        }
    }
}
