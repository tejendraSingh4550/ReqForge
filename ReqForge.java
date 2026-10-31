import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class ReqForge {
    private static final Path DATA_FILE = Path.of("requirements.db");
    private static final Scanner INPUT = new Scanner(System.in);
    private static final List<Requirement> requirements = new ArrayList<>();

    private static class Requirement {
        int id;
        String title;
        String description;
        String priority;
        String status;
        String createdAt;

        Requirement(int id, String title, String description, String priority, String status, String createdAt) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.priority = priority;
            this.status = status;
            this.createdAt = createdAt;
        }
    }

    public static void main(String[] args) {
        load();
        System.out.println("ReqForge - Requirements Tracker");
        while (true) {
            System.out.println("\n1. List requirements\n2. Add requirement\n3. Search\n4. Update\n5. Delete\n0. Exit");
            System.out.print("Choose an option: ");
            String choice = INPUT.nextLine().trim();
            switch (choice) {
                case "1" -> list(requirements);
                case "2" -> add();
                case "3" -> search();
                case "4" -> update();
                case "5" -> delete();
                case "0" -> { System.out.println("Goodbye!"); return; }
                default -> System.out.println("Choose one of the listed options.");
            }
        }
    }

    private static void load() {
        if (!Files.exists(DATA_FILE)) return;
        try {
            for (String line : Files.readAllLines(DATA_FILE, StandardCharsets.UTF_8)) {
                String[] fields = line.split("\\t", -1);
                if (fields.length != 6) continue;
                try {
                    requirements.add(new Requirement(Integer.parseInt(fields[0]), decode(fields[1]), decode(fields[2]),
                            decode(fields[3]), decode(fields[4]), decode(fields[5])));
                } catch (IllegalArgumentException ignored) {
                    // Skip malformed records and continue loading the rest.
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read requirements.db: " + e.getMessage());
        }
    }

    private static void save() {
        List<String> lines = new ArrayList<>();
        for (Requirement r : requirements) {
            lines.add(r.id + "\t" + encode(r.title) + "\t" + encode(r.description) + "\t" + encode(r.priority)
                    + "\t" + encode(r.status) + "\t" + encode(r.createdAt));
        }
        try {
            Files.write(DATA_FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Could not save requirements: " + e.getMessage());
        }
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private static void list(List<Requirement> items) {
        if (items.isEmpty()) {
            System.out.println("\nNo requirements yet. Add one to get started.");
            return;
        }
        System.out.printf("%n%-5s %-12s %-12s %s%n", "ID", "PRIORITY", "STATUS", "TITLE");
        System.out.println("-".repeat(76));
        items.stream().sorted(Comparator.comparingInt(r -> r.id)).forEach(r -> {
            System.out.printf("%-5d %-12s %-12s %s%n", r.id, r.priority, r.status, r.title);
            if (!r.description.isBlank()) System.out.println("      " + r.description.replace('\n', ' '));
        });
    }

    private static void add() {
        System.out.print("Title: ");
        String title = INPUT.nextLine().trim();
        if (title.isEmpty()) { System.out.println("Title cannot be empty."); return; }
        System.out.print("Description: ");
        String description = INPUT.nextLine().trim();
        System.out.print("Priority [low/medium/high] (medium): ");
        String priority = INPUT.nextLine().trim().toLowerCase();
        if (priority.isEmpty()) priority = "medium";
        if (!List.of("low", "medium", "high").contains(priority)) {
            System.out.println("Unknown priority; using medium.");
            priority = "medium";
        }
        int id = requirements.stream().mapToInt(r -> r.id).max().orElse(0) + 1;
        requirements.add(new Requirement(id, title, description, priority, "open", OffsetDateTime.now().toString()));
        save();
        System.out.println("Requirement added.");
    }

    private static Requirement findById() {
        System.out.print("Requirement ID: ");
        try {
            int id = Integer.parseInt(INPUT.nextLine().trim());
            Requirement found = requirements.stream().filter(r -> r.id == id).findFirst().orElse(null);
            if (found == null) System.out.println("Requirement not found.");
            return found;
        } catch (NumberFormatException e) {
            System.out.println("Enter a numeric ID.");
            return null;
        }
    }

    private static void update() {
        Requirement r = findById();
        if (r == null) return;
        System.out.println("Press Enter to keep the current value.");
        System.out.printf("Title [%s]: ", r.title);
        String value = INPUT.nextLine().trim();
        if (!value.isEmpty()) r.title = value;
        System.out.printf("Description [%s]: ", r.description);
        value = INPUT.nextLine().trim();
        if (!value.isEmpty()) r.description = value;
        System.out.printf("Status [open/in progress/done] [%s]: ", r.status);
        value = INPUT.nextLine().trim().toLowerCase();
        if (!value.isEmpty()) {
            if (List.of("open", "in progress", "done").contains(value)) r.status = value;
            else System.out.println("Invalid status; status left unchanged.");
        }
        System.out.printf("Priority [low/medium/high] [%s]: ", r.priority);
        value = INPUT.nextLine().trim().toLowerCase();
        if (!value.isEmpty()) {
            if (List.of("low", "medium", "high").contains(value)) r.priority = value;
            else System.out.println("Invalid priority; priority left unchanged.");
        }
        save();
        System.out.println("Requirement updated.");
    }

    private static void delete() {
        Requirement r = findById();
        if (r == null) return;
        System.out.printf("Delete '%s'? [y/N]: ", r.title);
        if (INPUT.nextLine().trim().equalsIgnoreCase("y")) {
            requirements.remove(r);
            save();
            System.out.println("Requirement deleted.");
        } else System.out.println("Delete cancelled.");
    }

    private static void search() {
        System.out.print("Search title/description: ");
        String query = INPUT.nextLine().trim().toLowerCase();
        List<Requirement> matches = requirements.stream()
                .filter(r -> r.title.toLowerCase().contains(query) || r.description.toLowerCase().contains(query))
                .toList();
        list(matches);
    }
}
