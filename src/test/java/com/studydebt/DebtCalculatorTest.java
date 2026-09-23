package com.studydebt;

import com.studydebt.model.Topic;
import com.studydebt.service.DebtCalculator;
import com.studydebt.service.LinearDecayStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DebtCalculatorTest {

    private final DebtCalculator calculator = new DebtCalculator(new LinearDecayStrategy());

    @Test
    void freshTopic_touchedToday_hasZeroDebt() {
        Topic topic = new Topic("Spring Security", 5);
        assertEquals(0.0, calculator.calculateDebt(topic));
    }

    @Test
    void doneTopic_alwaysHasZeroDebt() {
        Topic topic = new Topic("Spring Security", 5);
        topic.logProgress(100);
        assertEquals(0.0, calculator.calculateDebt(topic));
    }

    @Test
    void isCritical_returnsFalseForFreshTopic() {
        Topic topic = new Topic("Spring Security", 5);
        assertFalse(calculator.isCritical(topic));
    }

    @Test
    void rankByDebt_ordersFromHighestToLowest() {
        Topic low = new Topic("Low Importance", 1);
        Topic high = new Topic("High Importance", 5);
        List<Topic> ranked = calculator.rankByDebt(List.of(low, high));
        assertEquals(2, ranked.size());
    }

    @Test
    void totalDebt_ofEmptyList_isZero() {
        assertEquals(0.0, calculator.totalDebt(List.of()));
    }

    @Test
    void constructor_rejectsNullStrategy() {
        assertThrows(IllegalArgumentException.class, () -> new DebtCalculator(null));
    }
}
