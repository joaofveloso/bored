package com.bored;

import java.time.LocalDateTime;

public record Task(Long id, String description, Status status, LocalDateTime createdAt, LocalDateTime updatedAt) {

    static Task of(String description) {
        return new Task(null, description, Status.TODO, LocalDateTime.now(), null);
    }

    public Task withId(Long id) {
        if (this.id != null) {
            throw new IllegalStateException("Task already has an ID");
        }
        return new Task(id, description, status, createdAt, LocalDateTime.now());
    }

    public Task withStatus(Status status) {
        return new Task(id, description, status, createdAt, LocalDateTime.now());
    }

    public Task withDescription(String description) {
        return new Task(id, description, status, createdAt, LocalDateTime.now());
    }

    public enum Status {
        TODO, IN_PROGRESS, DONE
    }

    @Override
    public String toString() {
        return "Task{id=" + id + ", description='" + description + "', status='" + status +
                "', createdAt=" + createdAt + ", updatedAt=" + updatedAt + "}";
    }
}
