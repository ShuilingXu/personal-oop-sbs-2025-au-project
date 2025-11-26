package edu.sbs.cs.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Project {
    private String projectId;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate dueDate;
    private List<TeamMember> members;
    private List<TaskItem> tasks;

    public Project(String projectId, String name, String description, LocalDate dueDate) {
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.startDate = LocalDate.now();
        this.dueDate = dueDate;
        this.members = new ArrayList<>();
        this.tasks = new ArrayList<>();
    }

    // Getter和Setter
    public String getProjectId() { return projectId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public List<TeamMember> getMembers() { return members; }
    public List<TaskItem> getTasks() { return tasks; }

    public void addMember(TeamMember member) {
        if (!members.contains(member)) {
            members.add(member);
        }
    }

    public void removeMember(TeamMember member) {
        members.remove(member);
    }

    public void addTask(TaskItem task) {
        tasks.add(task);
    }

    public double calculateProgress() {
        if (tasks.isEmpty()) return 0.0;

        double totalProgress = tasks.stream()
                .mapToDouble(TaskItem::getProgress)
                .sum();
        return totalProgress / tasks.size();
    }
}