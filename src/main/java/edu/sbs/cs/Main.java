package edu.sbs.cs;

import edu.sbs.cs.service.DataManager;
import edu.sbs.cs.service.ProgressReporterThread;
import edu.sbs.cs.service.TaskProcessor;
import edu.sbs.cs.model.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private DataManager dataManager;
    private ProgressReporterThread progressReporter;
    private Scanner scanner;
    private boolean systemRunning;

    public Main() {
        this.dataManager = new DataManager();
        this.scanner = new Scanner(System.in);
        this.systemRunning = true;

        // 启动进度报告线程
        this.progressReporter = new ProgressReporterThread(dataManager);
        progressReporter.start();
    }

    public void start() {
        System.out.println("=== 欢迎使用虚拟团队项目管理系统 ===");

        while (systemRunning) {
            displayMainMenu();
            int choice = getIntInput("请选择操作: ");

            switch (choice) {
                case 1:
                    register();
                    break;
                case 2:
                    login();
                    break;
                case 3:
                    viewAssignedTasks();
                    break;
                case 4:
                    searchTasks();
                    break;
                case 5:
                    if (isAdminLoggedIn()) {
                        adminMenu();
                    } else {
                        System.out.println("需要管理员权限！");
                    }
                    break;
                case 6:
                    logout();
                    break;
                case 7:
                    shutdown();
                    break;
                default:
                    System.out.println("无效选择，请重新输入！");
            }
        }
    }

    private void displayMainMenu() {
        System.out.println("\n=== 主菜单 ===");
        System.out.println("1. 注册");
        System.out.println("2. 登录");
        System.out.println("3. 查看分配任务 (用户)");
        System.out.println("4. 按项目/状态搜索任务");
        System.out.println("5. 管理员菜单 (仅项目经理)");
        System.out.println("6. 登出");
        System.out.println("7. 退出系统");
        System.out.print("请选择: ");
    }

    private void adminMenu() {
        boolean inAdminMenu = true;

        while (inAdminMenu && isAdminLoggedIn()) {
            System.out.println("\n=== 项目经理菜单 ===");
            System.out.println("1. 创建新项目");
            System.out.println("2. 添加团队成员");
            System.out.println("3. 创建新任务 (分配给成员)");
            System.out.println("4. 查看所有项目及状态");
            System.out.println("5. 运行进度报告 (高级)");
            System.out.println("6. 返回主菜单");
            System.out.print("请选择: ");

            int choice = getIntInput("");

            switch (choice) {
                case 1:
                    createProject();
                    break;
                case 2:
                    addTeamMember();
                    break;
                case 3:
                    createTask();
                    break;
                case 4:
                    viewAllProjects();
                    break;
                case 5:
                    runProgressReport();
                    break;
                case 6:
                    inAdminMenu = false;
                    break;
                default:
                    System.out.println("无效选择！");
            }
        }
    }

    // 用户注册
    private void register() {
        System.out.println("\n=== 用户注册 ===");
        System.out.print("姓名: ");
        String name = scanner.nextLine();

        System.out.print("邮箱: ");
        String email = scanner.nextLine();

        System.out.print("密码: ");
        String password = scanner.nextLine();

        System.out.print("角色 (1-用户, 2-项目经理): ");
        int roleChoice = getIntInput("");
        Role role = (roleChoice == 2) ? Role.ADMIN : Role.USER;

        if (dataManager.registerUser(name, email, password, role)) {
            System.out.println("注册成功！");
        } else {
            System.out.println("注册失败！");
        }
    }

    // 用户登录
    private void login() {
        System.out.println("\n=== 用户登录 ===");
        System.out.print("邮箱: ");
        String email = scanner.nextLine();

        System.out.print("密码: ");
        String password = scanner.nextLine();

        if (dataManager.login(email, password)) {
            TeamMember user = dataManager.getCurrentUser();
            System.out.println("登录成功！欢迎 " + user.getName() + " (" + user.getRole() + ")");
        } else {
            System.out.println("登录失败！邮箱或密码错误。");
        }
    }

    // 查看分配的任务
    private void viewAssignedTasks() {
        if (!isUserLoggedIn()) {
            System.out.println("请先登录！");
            return;
        }

        List<TaskItem> tasks = dataManager.getAssignedTasks();
        System.out.println("\n=== 您的任务列表 ===");

        if (tasks.isEmpty()) {
            System.out.println("暂无分配的任务。");
        } else {
            for (int i = 0; i < tasks.size(); i++) {
                System.out.println((i + 1) + ". " + tasks.get(i).getTitle() +
                        " [" + tasks.get(i).getStatus() + "]");
            }

            System.out.print("\n输入任务编号查看详情 (0返回): ");
            int taskChoice = getIntInput("");

            if (taskChoice > 0 && taskChoice <= tasks.size()) {
                TaskItem task = tasks.get(taskChoice - 1);
                task.displayDetails();

                if (task.getAssignedTo().equals(dataManager.getCurrentUser())) {
                    System.out.print("\n是否更新任务状态？ (1-是, 0-否): ");
                    int updateChoice = getIntInput("");

                    if (updateChoice == 1) {
                        updateTaskStatus(task);
                    }
                }
            }
        }
    }

    // 更新任务状态
    private void updateTaskStatus(TaskItem task) {
        System.out.println("\n=== 更新任务状态 ===");
        System.out.println("1. 待办");
        System.out.println("2. 进行中");
        System.out.println("3. 已完成");
        System.out.print("选择状态: ");

        int statusChoice = getIntInput("");
        switch (statusChoice) {
            case 1:
                task.setStatus(TaskStatus.TODO);
                break;
            case 2:
                task.setStatus(TaskStatus.IN_PROGRESS);
                System.out.print("输入进度 (0-100): ");
                double progress = getDoubleInput("");
                task.setProgress(progress);
                break;
            case 3:
                task.setStatus(TaskStatus.COMPLETED);
                task.setProgress(100.0);
                break;
            default:
                System.out.println("无效选择！");
                return;
        }

        System.out.println("任务状态更新成功！");
    }

    // 搜索任务
    private void searchTasks() {
        System.out.println("\n=== 任务搜索 ===");

        // 显示项目列表
        System.out.println("可用项目:");
        dataManager.getProjects().values().forEach(project ->
                System.out.println(project.getProjectId() + ": " + project.getName()));

        System.out.print("输入项目ID (留空忽略): ");
        String projectId = scanner.nextLine().trim();
        if (projectId.isEmpty()) projectId = null;

        System.out.print("状态筛选 (1-待办, 2-进行中, 3-已完成, 0-忽略): ");
        int statusChoice = getIntInput("");
        TaskStatus status = null;
        if (statusChoice > 0 && statusChoice <= 3) {
            status = TaskStatus.values()[statusChoice - 1];
        }

        System.out.print("优先级筛选 (1-低, 2-中, 3-高, 4-严重, 0-忽略): ");
        int priorityChoice = getIntInput("");
        Priority priority = null;
        if (priorityChoice > 0 && priorityChoice <= 4) {
            priority = Priority.values()[priorityChoice - 1];
        }

        List<TaskItem> results = dataManager.searchTasks(projectId, status, priority);

        System.out.println("\n=== 搜索结果 ===");
        if (results.isEmpty()) {
            System.out.println("未找到匹配的任务。");
        } else {
            for (int i = 0; i < results.size(); i++) {
                TaskItem task = results.get(i);
                System.out.printf("%d. %s [%s] - %s - 进度: %.1f%%%n",
                        i + 1, task.getTitle(), task.getStatus(),
                        task.getPriority(), task.getProgress());
            }

            // 使用泛型处理器进行统计
            TaskProcessor<TaskItem> processor = new TaskProcessor<>();
            System.out.println("\n=== 统计信息 ===");
            System.out.println("任务状态分布: " + processor.countTasksByStatus(results));
            System.out.println("平均进度: " + processor.calculateAverageProgress(results) + "%");
        }
    }

    // 管理员功能实现
    private void createProject() {
        System.out.println("\n=== 创建新项目 ===");
        System.out.print("项目名称: ");
        String name = scanner.nextLine();

        System.out.print("项目描述: ");
        String description = scanner.nextLine();

        System.out.print("截止日期 (YYYY-MM-DD): ");
        String dueDateStr = scanner.nextLine();

        try {
            LocalDate dueDate = LocalDate.parse(dueDateStr);
            if (dataManager.createProject(name, description, dueDate)) {
                System.out.println("项目创建成功！");
            } else {
                System.out.println("项目创建失败！");
            }
        } catch (DateTimeParseException e) {
            System.out.println("日期格式错误！");
        }
    }

    // 添加团队成员到项目
    private void addTeamMember() {
        if (!isAdminLoggedIn()) {
            System.out.println("需要管理员权限！");
            return;
        }

        System.out.println("\n=== 添加团队成员到项目 ===");

        // 显示所有项目
        System.out.println("可用项目:");
        dataManager.getProjects().values().forEach(project ->
                System.out.println(project.getProjectId() + ": " + project.getName()));

        System.out.print("输入项目ID: ");
        String projectId = scanner.nextLine();

        // 显示所有成员
        System.out.println("可用成员:");
        dataManager.getMembers().values().forEach(member ->
                System.out.println(member.getMemberId() + ": " + member.getName() + " (" + member.getRole() + ")"));

        System.out.print("输入成员ID: ");
        String memberId = scanner.nextLine();

        if (dataManager.addMemberToProject(projectId, memberId)) {
            System.out.println("成员添加成功！");
        } else {
            System.out.println("成员添加失败！请检查项目ID和成员ID是否正确。");
        }
    }

    private void createTask() {
        System.out.println("\n=== 创建新任务 ===");

        // 选择项目
        System.out.println("选择项目:");
        dataManager.getProjects().values().forEach(project ->
                System.out.println(project.getProjectId() + ": " + project.getName()));

        System.out.print("输入项目ID: ");
        String projectId = scanner.nextLine();

        System.out.print("任务类型 (1-功能开发, 2-缺陷报告): ");
        int taskType = getIntInput("");

        System.out.print("任务标题: ");
        String title = scanner.nextLine();

        System.out.print("任务描述: ");
        String description = scanner.nextLine();

        System.out.print("优先级 (1-低, 2-中, 3-高, 4-严重): ");
        int priorityChoice = getIntInput("");
        Priority priority = Priority.values()[priorityChoice - 1];

        System.out.print("截止日期 (YYYY-MM-DD): ");
        String dueDateStr = scanner.nextLine();

        String taskId = null;

        try {
            LocalDate dueDate = LocalDate.parse(dueDateStr);

            if (taskType == 1) {
                // 功能开发任务
                System.out.print("故事点: ");
                int storyPoints = getIntInput("");

                System.out.print("功能类别: ");
                String category = scanner.nextLine();

                taskId = dataManager.createFeatureTask(projectId, title, description,
                        priority, dueDate, storyPoints, category);
            } else if (taskType == 2) {
                // 缺陷报告
                System.out.print("严重程度 (1-低, 2-中, 3-高, 4-阻塞): ");
                int severityChoice = getIntInput("");
                Severity severity = Severity.values()[severityChoice - 1];

                System.out.print("重现步骤: ");
                String steps = scanner.nextLine();

                System.out.print("环境: ");
                String environment = scanner.nextLine();

                taskId = dataManager.createBugReport(projectId, title, description,
                        priority, dueDate, severity, steps, environment);
            }

            if (taskId != null) {
                System.out.println("任务创建成功！任务ID: " + taskId);
                assignTaskToMember(taskId);
            } else {
                System.out.println("任务创建失败！");
            }

        } catch (DateTimeParseException e) {
            System.out.println("日期格式错误！");
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("无效的选择！");
        }
    }

    private void assignTaskToMember(String taskId) {
        System.out.print("是否立即分配任务？ (1-是, 0-否): ");
        int assignChoice = getIntInput("");

        if (assignChoice == 1) {
            System.out.println("可用成员:");
            dataManager.getMembers().values().forEach(member ->
                    System.out.println(member.getMemberId() + ": " + member.getName()));

            System.out.print("输入成员ID: ");
            String memberId = scanner.nextLine();

            if (dataManager.assignTask(taskId, memberId)) {
                System.out.println("任务分配成功！");
            } else {
                System.out.println("任务分配失败！");
            }
        }
    }

    private void viewAllProjects() {
        System.out.println("\n=== 所有项目状态 ===");
        dataManager.getProjects().values().forEach(project -> {
            double progress = project.calculateProgress();
            System.out.printf("项目: %s | 进度: %.1f%% | 成员: %d | 任务: %d%n",
                    project.getName(), progress, project.getMembers().size(), project.getTasks().size());
        });
    }

    private void runProgressReport() {
        System.out.println("\n=== 进度报告 ===");
        TaskProcessor<TaskItem> processor = new TaskProcessor<>();

        for (Project project : dataManager.getProjects().values()) {
            List<TaskItem> tasks = project.getTasks();
            System.out.println("\n项目: " + project.getName());
            System.out.println("任务状态分布: " + processor.countTasksByStatus(tasks));
            System.out.println("平均进度: " + processor.calculateAverageProgress(tasks) + "%");

            List<TaskItem> overdueTasks = processor.getOverdueTasks(tasks);
            if (!overdueTasks.isEmpty()) {
                System.out.println("逾期任务: " + overdueTasks.size());
                overdueTasks.forEach(task ->
                        System.out.println("  - " + task.getTitle() + " (应于: " + task.getDueDate() + ")"));
            }
        }
    }

    // 辅助方法
    private boolean isUserLoggedIn() {
        return dataManager.getCurrentUser() != null;
    }

    private boolean isAdminLoggedIn() {
        return isUserLoggedIn() && dataManager.getCurrentUser().getRole() == Role.ADMIN;
    }

    private void logout() {
        dataManager.logout();
        System.out.println("已登出！");
    }



    private int getIntInput(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private double getDoubleInput(String prompt) {
        System.out.print(prompt);
        try {
            return Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static void main(String[] args) {
        Main system = new Main();
        system.start();
    }
    private void shutdown() {
        System.out.println("正在关闭系统...");
        progressReporter.stopReporting();
        systemRunning = false;
        scanner.close();
        System.out.println("系统已关闭！");
    }
}