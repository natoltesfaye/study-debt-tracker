package com.studydebt;

import com.studydebt.model.Course;
import com.studydebt.model.Status;
import com.studydebt.model.Topic;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CourseTest {

    @Test
    void newCourse_hasNoTopics() {
        Course course = new Course("Backend Development");

        assertTrue(course.getTopics().isEmpty());
        assertEquals(0.0, course.getAverageProgress());
    }

    @Test
    void addTopic_increasesTopicCount() {
        Course course = new Course("Backend Development");
        Topic topic = new Topic("Spring Security", 4);

        course.addTopic(topic);

        assertEquals(1, course.getTopics().size());
    }

    @Test
    void addTopic_rejectsDuplicate() {
        Course course = new Course("Backend Development");
        Topic topic = new Topic("Spring Security", 4);
        course.addTopic(topic);

        assertThrows(IllegalArgumentException.class, () -> course.addTopic(topic));
    }

    @Test
    void addTopic_rejectsNull() {
        Course course = new Course("Backend Development");

        assertThrows(IllegalArgumentException.class, () -> course.addTopic(null));
    }

    @Test
    void getAverageProgress_calculatesCorrectly() {
        Course course = new Course("Backend Development");
        Topic t1 = new Topic("Spring Security", 4);
        Topic t2 = new Topic("REST APIs", 3);
        t1.logProgress(40);
        t2.logProgress(60);

        course.addTopic(t1);
        course.addTopic(t2);

        assertEquals(50.0, course.getAverageProgress());
    }

    @Test
    void countByStatus_countsCorrectly() {
        Course course = new Course("Backend Development");
        Topic notStarted = new Topic("Spring Security", 4);
        Topic inProgress = new Topic("REST APIs", 3);
        Topic done = new Topic("Database Design", 5);
        inProgress.logProgress(50);
        done.logProgress(100);

        course.addTopic(notStarted);
        course.addTopic(inProgress);
        course.addTopic(done);

        assertEquals(1, course.countByStatus(Status.NOT_STARTED));
        assertEquals(1, course.countByStatus(Status.IN_PROGRESS));
        assertEquals(1, course.countByStatus(Status.DONE));
    }

    @Test
    void getMostNeglectedTopic_returnsEmptyWhenNoTopics() {
        Course course = new Course("Backend Development");

        Optional<Topic> result = course.getMostNeglectedTopic();

        assertTrue(result.isEmpty());
    }

    @Test
    void getTopics_returnsUnmodifiableList() {
        Course course = new Course("Backend Development");
        course.addTopic(new Topic("Spring Security", 4));

        assertThrows(UnsupportedOperationException.class,
                () -> course.getTopics().add(new Topic("Hacked In", 1)));
    }

    @Test
    void constructor_rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new Course(""));
        assertThrows(IllegalArgumentException.class, () -> new Course(null));
    }
}