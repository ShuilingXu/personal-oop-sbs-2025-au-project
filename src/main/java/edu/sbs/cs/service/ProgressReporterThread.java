package edu.sbs.cs.service;

import edu.sbs.cs.model.Project;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProgressReporterThread extends Thread {
    private DataManager dataManager;
    private volatile boolean running;
    private static final String REPORT_FILE = "progress_reports.log";

    public ProgressReporterThread(DataManager dataManager) {
        this.dataManager = dataManager;
        this.running = true;
        this.setDaemon(true); // 设置为守护线程
    }

    @Override
    public void run() {
        while (running) {
            try {
                // 每30秒生成一次报告
                Thread.sleep(30000);
                generateProgressReport();
            } catch (InterruptedException e) {
                System.out.println("进度报告线程被中断");
                break;
            }
        }
    }

    private void generateProgressReport() {
        try (FileWriter fw = new FileWriter(REPORT_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            pw.println("=== 项目进度报告 - " + now.format(formatter) + " ===");

            for (Project project : dataManager.getProjects().values()) {
                double progress = project.calculateProgress();
                long totalTasks = project.getTasks().size();
                long completedTasks = project.getTasks().stream()
                        .filter(task -> task.getStatus() == edu.sbs.cs.model.TaskStatus.COMPLETED)
                        .count();

                pw.printf("项目: %s | 进度: %.1f%% | 任务: %d/%d 完成%n",
                        project.getName(), progress, completedTasks, totalTasks);
            }
            pw.println("=== 报告结束 ===\n");

        } catch (IOException e) {
            System.err.println("写入进度报告失败: " + e.getMessage());
        }
    }

    public void stopReporting() {
        running = false;
        this.interrupt();
    }
}