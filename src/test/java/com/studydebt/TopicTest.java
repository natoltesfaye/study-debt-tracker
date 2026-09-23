package com.studydebt;

import com.studydebt.model.Status;
import com.studydebt.model.Topic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TopicTest {

    @Test
    void newTopic_startsAtZeroProgressAndNotStarted() {
        Topic topic = new Topic("Spring Security", 4);
        assertEquals(0, topic.getProgressPercent());
        assertEquals(Status.NOT_STARTED, topic.getStatus());
    }

    @Test
    void logProgress_updatesPercentAndStatus() {
        Topic topic = new Topic("Spring Security", 4);
        topic.logProgress(30);
        assertEquals(30, topic.getProgressPercent());
        assertEquals(Status.IN_PROGRESS, topic.getStatus());
    }

    @Test
    void logProgress_reaching100_marksDone() {
        Topic topic = new Topic("Spring Security", 4);
        topic.logProgress(100);
        assertEquals(Status.DONE, topic.getStatus());
    }

    @Test
    void setProgressPercent_rejectsValuesAboveHundred() {
        Topic topic = new Topic("Spring Security", 4);
        assertThrows(IllegalArgumentException.class, () -> topic.setProgressPercent(150));
    }

    @Test
    void setProgressPercent_rejectsNegativeValues() {
        Topic topic = new Topic("Spring Security", 4);
        assertThrows(IllegalArgumentException.class, () -> topic.setProgressPercent(-5));
    }

    @Test
    void constructor_rejectsInvalidImportanceWeight() {
        assertThrows(IllegalArgumentException.class, () -> new Topic("Spring Security", 6));
        assertThrows(IllegalArgumentException.class, () -> new Topic("Spring Security", 0));
    }

    @Test
    void constructor_rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new Topic("", 3));
        assertThrows(IllegalArgumentException.class, () -> new Topic(null, 3));
    }

    @Test
    void logProgress_rejectsNegativeIncrement() {
        Topic topic = new Topic("Spring Security", 4);
        assertThrows(IllegalArgumentException.class, () -> topic.logProgress(-10));
    }
}