package com.studydebt;

import com.studydebt.model.Topic;
import com.studydebt.persistence.JsonTopicStore;
import com.studydebt.repository.TopicRepository;
import com.studydebt.service.ConsoleAlertListener;
import com.studydebt.service.DebtCalculator;
import com.studydebt.service.DebtMonitor;
import com.studydebt.service.LinearDecayStrategy;

import java.util.List;
import java.util.Scanner;

/**
 * Console entry point. Wires the layers together (repository, calculator,
 * monitor, persistence) and drives a simple text menu.
 */
public class Main {

    private static final String DATA_FILE = "data.json";

    private final TopicRepository repository = new TopicRepository();
    private final DebtCalculator calculator = new DebtCalculator(new LinearDecayStrategy());
    private final DebtMonitor monitor = new DebtMonitor(calculator);
    private final JsonTopicStore store = new JsonTopicStore(DATA_FILE);
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        System.out.println("=== Study Debt Tracker ===");
        monitor.subscribe(new ConsoleAlertListener());

        List<Topic> loaded = store.load();
        repository.replaceAll(loaded);
        if (!loaded.isEmpty()) {
            System.out.printf("Loaded %d saved topic(s) from %s.%n", loaded.size(), DATA_FILE);
        }

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addTopic();
                case "2" -> logProgress();
                case "3" -> viewRankedByDebt();
                case "4" -> checkCriticalAlerts();
                case "5" -> viewAllTopics();
                case "0" -> {
                    store.save(repository.findAll());
                    System.out.println("Saved. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Not a valid option, try again.");
            }
        }
    }

    private void printMenu() {
        System.out.println("""

                1. Add topic
                2. Log study progress
                3. View topics ranked by debt (most neglected first)
                4. Check for critical debt alerts
                5. View all topics
                0. Save and exit
                Choose an option:""");
    }

    private void addTopic() {
        System.out.print("Topic name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Importance (1-5): ");
        int importance = readInt();
        try {
            Topic topic = new Topic(name, importance);
            repository.save(topic);
            System.out.println("Added: " + topic);
        } catch (IllegalArgumentException e) {
            System.out.println("Could not add topic: " + e.getMessage());
        }
    }

    private void logProgress() {
        List<Topic> topics = repository.findAll();
        if (topics.isEmpty()) {
            System.out.println("No topics yet — add one first.");
            return;
        }
        listTopicsShort(topics);
        System.out.print("Pick a topic number: ");
        Topic topic = pickTopic(topics);
        if (topic == null) return;

        System.out.print("Progress to add (%): ");
        int amount = readInt();
        try {
            topic.logProgress(amount);
            System.out.println("Updated: " + topic);
            monitor.check(topic);
        } catch (IllegalArgumentException e) {
            System.out.println("Could not log progress: " + e.getMessage());
        }
    }

    private void viewRankedByDebt() {
        List<Topic> ranked = calculator.rankByDebt(repository.findAll());
        if (ranked.isEmpty()) {
            System.out.println("No topics yet — add one first.");
            return;
        }
        System.out.println("\n--- Ranked by debt (highest first) ---");
        for (Topic t : ranked) {
            double debt = calculator.calculateDebt(t);
            System.out.printf("  %.1f  |  %s%n", debt, t);
        }
        System.out.printf("Total debt across all topics: %.1f%n", calculator.totalDebt(ranked));
    }

    private void checkCriticalAlerts() {
        List<Topic> topics = repository.findAll();
        if (topics.isEmpty()) {
            System.out.println("No topics yet — add one first.");
            return;
        }
        System.out.println("\n--- Checking for critical debt ---");
        monitor.checkAll(topics);
        System.out.println("Check complete.");
    }

    private void viewAllTopics() {
        List<Topic> topics = repository.findAll();
        if (topics.isEmpty()) {
            System.out.println("No topics yet — add one first.");
            return;
        }
        listTopicsShort(topics);
    }

    // ---------- small helpers ----------

    private void listTopicsShort(List<Topic> topics) {
        for (int i = 0; i < topics.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, topics.get(i));
        }
    }

    private Topic pickTopic(List<Topic> topics) {
        int index = readInt() - 1;
        if (index < 0 || index >= topics.size()) {
            System.out.println("Invalid selection.");
            return null;
        }
        return topics.get(index);
    }

    private int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
