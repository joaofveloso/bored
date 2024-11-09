package com.bored;

import com.bored.Task.Status;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.UnaryOperator;

public class JsonTaskRepository {

    private static final String FILE_PATH = "tasks.json";
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private final List<Task> tasks;
    private final Lock lock = new ReentrantLock();

    public JsonTaskRepository() {
        this.tasks = loadTasksFromFile();
    }

    private List<Task> loadTasksFromFile() {
        lock.lock();
        try {
            Path filePath = Path.of(FILE_PATH);
            if (Files.notExists(filePath)) return new ArrayList<>();
            return objectMapper.readValue(Files.newBufferedReader(filePath), new TypeReference<>() {});
        } catch (IOException e) {
            System.err.println("Failed to load tasks: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            lock.unlock();
        }
    }

    private void saveTasksToFile() {
        lock.lock();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), tasks);
        } catch (IOException e) {
            System.err.println("Failed to save tasks: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    private long getNextId() {
        return tasks.stream()
                .mapToLong(Task::id)
                .max()
                .orElse(0L) + 1;
    }

    public Task insert(Task task) {
        lock.lock();
        try {
            long newId = getNextId();
            Task taskToInsert = task.withId(newId);
            tasks.add(taskToInsert);
            saveTasksToFile();
            return taskToInsert;
        } finally {
            lock.unlock();
        }
    }

    public List<Task> getAllTasks() {
        lock.lock();
        try {
            return new ArrayList<>(tasks);
        } finally {
            lock.unlock();
        }
    }

    public void deleteById(Long id) {
        lock.lock();
        try {
            tasks.removeIf(task -> Objects.equals(task.id(), id));
            saveTasksToFile();
        } finally {
            lock.unlock();
        }
    }

    private Optional<Task> findById(Long id) {
        lock.lock();
        try {
            return tasks.stream().filter(task -> Objects.equals(task.id(), id)).findFirst();
        } finally {
            lock.unlock();
        }
    }

    public void updateDescriptionById(Long id, String description) {
        updateTask(id, task -> task.withDescription(description));
    }

    public void updateStatusById(Long id, Status newStatus) {
        updateTask(id, task -> task.withStatus(newStatus));
    }

    private void updateTask(Long id, UnaryOperator<Task> updater) {
        lock.lock();
        try {
            findById(id).ifPresent(task -> {
                tasks.remove(task);
                tasks.add(updater.apply(task));
                saveTasksToFile();
            });
        } finally {
            lock.unlock();
        }
    }
}