package edu.sbs.cs.model;

import java.time.LocalDate;

public class FeatureTask extends TaskItem {
    private int storyPoints;
    private String featureCategory;

    public FeatureTask(String taskId, String title, String description, Priority priority,
                       LocalDate dueDate, int storyPoints, String featureCategory) {
        super(taskId, title, description, priority, dueDate);
        this.storyPoints = storyPoints;
        this.featureCategory = featureCategory;
    }

    @Override
    public void displayDetails() {
        System.out.println("=== 功能开发任务 ===");
        System.out.println("任务ID: " + taskId);
        System.out.println("标题: " + title);
        System.out.println("描述: " + description);
        System.out.println("优先级: " + priority);
        System.out.println("状态: " + status);
        System.out.println("截止日期: " + dueDate);
        System.out.println("故事点: " + storyPoints);
        System.out.println("功能类别: " + featureCategory);
        System.out.println("进度: " + progress + "%");
        if (assignedTo != null) {
            System.out.println("分配给: " + assignedTo.getName());
        }
    }

    // Getter和Setter
    public int getStoryPoints() { return storyPoints; }
    public void setStoryPoints(int storyPoints) { this.storyPoints = storyPoints; }
    public String getFeatureCategory() { return featureCategory; }
    public void setFeatureCategory(String featureCategory) { this.featureCategory = featureCategory; }
}