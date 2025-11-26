package edu.sbs.cs.model;

import java.time.LocalDate;

public class BugReport extends TaskItem {
    private Severity severity;
    private String stepsToReproduce;
    private String environment;

    public BugReport(String taskId, String title, String description, Priority priority,
                     LocalDate dueDate, Severity severity, String stepsToReproduce, String environment) {
        super(taskId, title, description, priority, dueDate);
        this.severity = severity;
        this.stepsToReproduce = stepsToReproduce;
        this.environment = environment;
    }

    @Override
    public void displayDetails() {
        System.out.println("=== 缺陷报告 ===");
        System.out.println("任务ID: " + taskId);
        System.out.println("标题: " + title);
        System.out.println("描述: " + description);
        System.out.println("优先级: " + priority);
        System.out.println("状态: " + status);
        System.out.println("截止日期: " + dueDate);
        System.out.println("严重程度: " + severity + " ⚠️");
        System.out.println("重现步骤: " + stepsToReproduce);
        System.out.println("环境: " + environment);
        System.out.println("进度: " + progress + "%");
        if (assignedTo != null) {
            System.out.println("分配给: " + assignedTo.getName());
        }
    }

    // Getter和Setter
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public String getStepsToReproduce() { return stepsToReproduce; }
    public void setStepsToReproduce(String stepsToReproduce) { this.stepsToReproduce = stepsToReproduce; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
}