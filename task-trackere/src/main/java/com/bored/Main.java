package com.bored;

import java.util.Map;
import java.util.Scanner;
import java.util.function.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {

    private static final Pattern COMMAND_PATTERN = Pattern.compile(
            "task-cli\\s+([\\w-]+)(?:\\s+(\\d+|\\w+))?(?:\\s+\"([^\"]+)\")?"
    );

    private static JsonTaskRepository jsonTaskRepository;

    public static void main(String[] args) {

        jsonTaskRepository = new JsonTaskRepository();

        Map<String, Object> predefinedCommands = initializeCommand();
        System.out.println("Welcome to the Command Line App. Type 'exit' to quit.");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye!");
                    break;
                }

                Matcher matcher = COMMAND_PATTERN.matcher(input);

                if (!matcher.matches()) {
                    System.out.println("Invalid command");
                    continue;
                }

                String action = matcher.group(1);
                String id = matcher.group(2);
                String description = matcher.group(3);

                Object command = predefinedCommands.get(action);
                executeCommand(command, id, description);
            }
        }
    }


    private static Map<String, Object> initializeCommand() {
        return Map.of(
                "add", (BiFunction<String, String, String>) (id, description) -> {
                    Task insert = jsonTaskRepository.insert(Task.of(description));
                    return "Task added successfully (ID: %s)".formatted(insert.id());
                },
                "update", (BiConsumer<String, String>) (id, description)
                        -> jsonTaskRepository.updateDescriptionById(Long.valueOf(id), description),
                "delete", (Consumer<String>) id
                        -> jsonTaskRepository.deleteById(Long.valueOf(id)),
                "mark-in-progress", (Consumer<String>) (id)
                        -> jsonTaskRepository.updateStatusById(Long.valueOf(id), Task.Status.IN_PROGRESS),
                "mark-done", (Consumer<String>) id
                        -> jsonTaskRepository.updateStatusById(Long.valueOf(id), Task.Status.DONE),
                "list", (Function<String, String>) status -> {
                    if (status == null || status.isBlank()) {
                        return jsonTaskRepository.getAllTasks().stream()
                                .map(Task::toString)
                                .collect(Collectors.joining("\n"));
                    } else {

                        return jsonTaskRepository.getAllTasks().stream()
                                .filter(p -> p.status().toString().equalsIgnoreCase(status.replace("-", "_")))
                                .map(Task::toString)
                                .collect(Collectors.joining("\n"));
                    }
                }
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void executeCommand(Object command, String id, String param) {

        switch (command) {
            case Runnable runnable -> runnable.run();
            case Supplier supplier -> System.out.println(supplier.get());
            case BiFunction function -> System.out.println(function.apply(id, param));
            case BiConsumer consumer -> consumer.accept(id, param);
            case Function function -> System.out.println(function.apply(id));
            case Consumer consumer -> consumer.accept(id);
            case null, default -> System.out.println("Unsupported command type.");
        }
    }
}