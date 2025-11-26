package edu.sbs.cs.service;

import edu.sbs.cs.model.TaskItem;
import edu.sbs.cs.model.TaskStatus;
import edu.sbs.cs.model.Priority;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskProcessor<T extends TaskItem> {

    // 统计任务状态分布
    public Map<TaskStatus, Long> countTasksByStatus(List<T> tasks) {
        return tasks.stream()
                .collect(Collectors.groupingBy(
                        TaskItem::getStatus,
                        Collectors.counting()
                ));
    }

    // 按优先级过滤任务
    public List<T> filterByPriority(List<T> tasks, Priority priority) {
        return tasks.stream()
                .filter(task -> task.getPriority() == priority)
                .collect(Collectors.toList());
    }

    // 计算平均进度
    public double calculateAverageProgress(List<T> tasks) {
        return tasks.stream()
                .mapToDouble(TaskItem::getProgress)
                .average()
                .orElse(0.0);
    }

    // 获取逾期任务
    public List<T> getOverdueTasks(List<T> tasks) {
        return tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                .filter(task -> java.time.LocalDate.now().isAfter(task.getDueDate()))
                .collect(Collectors.toList());
    }
}