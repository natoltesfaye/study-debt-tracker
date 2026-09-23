package com.studydebt;

import com.studydebt.model.Topic;
import com.studydebt.service.DebtCalculator;
import com.studydebt.service.DebtMonitor;
import com.studydebt.service.LinearDecayStrategy;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DebtMonitorTest {

    // Rehydration constructor lets us simulate a topic neglected for 30 days,
    // since a freshly-created Topic always has 0 days since last touched.
    private Topic neglectedTopic() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        return new Topic("neglected-id", "Neglected Topic", 5, 10, thirtyDaysAgo, thirtyDaysAgo);
    }

    @Test
    void check_notifiesListenersWhenTopicIsCritical() {
        DebtMonitor monitor = new DebtMonitor(new DebtCalculator(new LinearDecayStrategy()));
        List<String> notified = new ArrayList<>();
        monitor.subscribe((topic, debt) -> notified.add(topic.getName()));

        monitor.check(neglectedTopic());

        assertEquals(List.of("Neglected Topic"), notified);
    }

    @Test
    void check_doesNotNotifyWhenTopicIsFresh() {
        DebtMonitor monitor = new DebtMonitor(new DebtCalculator(new LinearDecayStrategy()));
        List<String> notified = new ArrayList<>();
        monitor.subscribe((topic, debt) -> notified.add(topic.getName()));

        monitor.check(new Topic("Fresh Topic", 5));

        assertTrue(notified.isEmpty());
    }

    @Test
    void unsubscribe_stopsFurtherNotifications() {
        DebtMonitor monitor = new DebtMonitor(new DebtCalculator(new LinearDecayStrategy()));
        List<String> notified = new ArrayList<>();
        var listener = (com.studydebt.service.DebtAlertListener) (topic, debt) -> notified.add(topic.getName());
        monitor.subscribe(listener);
        monitor.unsubscribe(listener);

        monitor.check(neglectedTopic());

        assertTrue(notified.isEmpty());
    }

    @Test
    void constructor_rejectsNullCalculator() {
        assertThrows(IllegalArgumentException.class, () -> new DebtMonitor(null));
    }
}
