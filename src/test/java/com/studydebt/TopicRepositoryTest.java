package com.studydebt;

import com.studydebt.model.Topic;
import com.studydebt.repository.TopicRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TopicRepositoryTest {

    @Test
    void save_thenFindById_returnsTheSameTopic() {
        TopicRepository repo = new TopicRepository();
        Topic topic = new Topic("Spring Security", 4);

        repo.save(topic);
        Optional<Topic> found = repo.findById(topic.getId());

        assertTrue(found.isPresent());
        assertEquals(topic, found.get());
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        TopicRepository repo = new TopicRepository();

        assertTrue(repo.findById("nonexistent").isEmpty());
    }

    @Test
    void findAll_returnsAllSavedTopicsInInsertionOrder() {
        TopicRepository repo = new TopicRepository();
        Topic first = new Topic("First", 3);
        Topic second = new Topic("Second", 3);

        repo.save(first);
        repo.save(second);

        List<Topic> all = repo.findAll();
        assertEquals(List.of(first, second), all);
    }

    @Test
    void deleteById_removesTopic() {
        TopicRepository repo = new TopicRepository();
        Topic topic = new Topic("Spring Security", 4);
        repo.save(topic);

        boolean deleted = repo.deleteById(topic.getId());

        assertTrue(deleted);
        assertEquals(0, repo.count());
    }

    @Test
    void deleteById_unknownId_returnsFalse() {
        TopicRepository repo = new TopicRepository();

        assertFalse(repo.deleteById("nonexistent"));
    }

    @Test
    void save_rejectsNull() {
        TopicRepository repo = new TopicRepository();

        assertThrows(IllegalArgumentException.class, () -> repo.save(null));
    }

    @Test
    void replaceAll_swapsOutAllTopics() {
        TopicRepository repo = new TopicRepository();
        repo.save(new Topic("Old", 2));

        Topic replacement = new Topic("New", 3);
        repo.replaceAll(List.of(replacement));

        assertEquals(1, repo.count());
        assertTrue(repo.findById(replacement.getId()).isPresent());
    }
}
