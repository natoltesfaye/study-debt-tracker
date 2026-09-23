package com.studydebt;

import com.studydebt.model.Topic;
import com.studydebt.persistence.JsonTopicStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonTopicStoreTest {

    private static final String TEST_FILE = "test-data.json";

    @AfterEach
    void cleanup() {
        new File(TEST_FILE).delete();
    }

    @Test
    void save_thenLoad_restoresEquivalentTopics() {
        JsonTopicStore store = new JsonTopicStore(TEST_FILE);
        Topic topic = new Topic("Spring Security", 4);
        topic.logProgress(35);

        store.save(List.of(topic));
        List<Topic> loaded = store.load();

        assertEquals(1, loaded.size());
        Topic restored = loaded.get(0);
        assertEquals(topic.getId(), restored.getId());
        assertEquals(topic.getName(), restored.getName());
        assertEquals(topic.getProgressPercent(), restored.getProgressPercent());
        assertEquals(topic.getStatus(), restored.getStatus());
    }

    @Test
    void load_whenFileDoesNotExist_returnsEmptyList() {
        JsonTopicStore store = new JsonTopicStore("does-not-exist.json");

        List<Topic> loaded = store.load();

        assertTrue(loaded.isEmpty());
    }

    @Test
    void save_thenLoad_preservesMultipleTopics() {
        JsonTopicStore store = new JsonTopicStore(TEST_FILE);
        Topic t1 = new Topic("Topic A", 3);
        Topic t2 = new Topic("Topic B", 5);

        store.save(List.of(t1, t2));
        List<Topic> loaded = store.load();

        assertEquals(2, loaded.size());
    }
}
