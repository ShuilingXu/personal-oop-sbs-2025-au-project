package edu.sbs.cs.model;

import java.time.LocalDate;

public abstract class TaskItem {
    protected String taskId;
    protected String title;
    protected String description;
    protected Priority priority;
    protected TaskStatus status;
    protected LocalDate dueDate;
    protected TeamMember assignedTo;
    protected double progress;

    public TaskItem(String taskId, String title, String description, Priority priority, LocalDate dueDate) {
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.status = TaskStatus.TODO;
        this.progress = 0.0;
    }

    // 抽象方法 - 多态体现
    public abstract void displayDetails();

    // Getter和Setter - 封装体现
    public String getTaskId() { return taskId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public TeamMember getAssignedTo() { return assignedTo; }
    public void setAssignedTo(TeamMember assignedTo) { this.assignedTo = assignedTo; }
    public double getProgress() { return progress; }
    public void setProgress(double progress) { this.progress = progress; }
}