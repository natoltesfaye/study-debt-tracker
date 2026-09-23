package com.studydebt.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.studydebt.model.Topic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves/loads Topics to a JSON file on disk, so data survives between runs.
 * Converts Topic <-> TopicRecord at the boundary, keeping Jackson entirely
 * out of the domain model.
 */
public class JsonTopicStore {

    private final File file;
    private final ObjectMapper mapper;

    public JsonTopicStore(String filePath) {
        this.file = new File(filePath);
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
    }

    public void save(List<Topic> topics) {
        try {
            List<TopicRecord> records = new ArrayList<>();
            for (Topic t : topics) {
                records.add(new TopicRecord(
                        t.getId(), t.getName(), t.getImportanceWeight(),
                        t.getProgressPercent(), t.getDateStarted(), t.getLastTouched()));
            }
            mapper.writeValue(file, records);
        } catch (IOException e) {
            throw new PersistenceException("Failed to save topics to " + file.getPath(), e);
        }
    }

    public List<Topic> load() {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try {
            TopicRecord[] records = mapper.readValue(file, TopicRecord[].class);
            List<Topic> topics = new ArrayList<>();
            for (TopicRecord r : records) {
                topics.add(new Topic(r.id, r.name, r.importanceWeight, r.progressPercent,
                        r.dateStarted, r.lastTouched));
            }
            return topics;
        } catch (IOException e) {
            throw new PersistenceException("Failed to load topics from " + file.getPath(), e);
        }
    }
}
