package edu.sbs.cs.service;

import edu.sbs.cs.database.DatabaseManager;
import edu.sbs.cs.model.*;
import java.util.*;
import java.util.stream.Collectors;

public class DataManager {
    // 使用嵌套集合存储复杂关系
    private Map<String, Project> projects; // Key: projectId
    private Map<String, TeamMember> members; // Key: memberId
    private Map<String, TaskItem> tasks; // Key: taskId
    private Map<Project, Map<TeamMember, List<TaskItem>>> taskAssignments;

    private TeamMember currentUser;
    private DatabaseManager databaseManager;
    private boolean databaseEnabled;

    public DataManager() {
        this.projects = new HashMap<>();
        this.members = new HashMap<>();
        this.tasks = new HashMap<>();
        this.taskAssignments = new HashMap<>();

        try {
            this.databaseManager = new DatabaseManager();
            this.databaseEnabled = databaseManager.isConnectionValid();

            if (databaseEnabled) {
                loadDataFromDatabase();
                System.out.println("数据库模式已启用");
            } else {
                System.out.println("数据库连接失败，使用内存模式");
                initializeSampleData();
            }
        } catch (Exception e) {
            System.err.println("数据库初始化异常: " + e.getMessage());
            this.databaseEnabled = false;
            initializeSampleData();
        }
    }

    private void loadDataFromDatabase() {
        try {
            // 加载成员
            List<TeamMember> loadedMembers = databaseManager.loadAllMembers();
            for (TeamMember member : loadedMembers) {
                members.put(member.getMemberId(), member);
            }

            // 加载项目
            List<Project> loadedProjects = databaseManager.loadAllProjects();
            for (Project project : loadedProjects) {
                projects.put(project.getProjectId(), project);
                taskAssignments.put(project, new HashMap<>());

                // 为项目中的每个成员初始化任务分配映射
                for (TeamMember member : project.getMembers()) {
                    taskAssignments.get(project).put(member, new ArrayList<>());
                }
            }

            // 加载任务
            List<TaskItem> loadedTasks = databaseManager.loadAllTasks(members);
            for (TaskItem task : loadedTasks) {
                tasks.put(task.getTaskId(), task);

                // 将任务添加到对应的项目和成员
                for (Project project : projects.values()) {
                    // 查找任务所属的项目
                    boolean taskInProject = project.getTasks().stream()
                            .anyMatch(t -> t.getTaskId().equals(task.getTaskId()));

                    if (!taskInProject) {
                        // 如果任务不在项目任务列表中，尝试通过project_id查找
                        // 这里需要从数据库查询任务的项目关系
                        // 暂时跳过，在保存任务时会建立关系
                        continue;
                    }

                    // 更新任务分配映射
                    if (task.getAssignedTo() != null) {
                        taskAssignments.get(project).putIfAbsent(task.getAssignedTo(), new ArrayList<>());
                        taskAssignments.get(project).get(task.getAssignedTo()).add(task);
                    }
                    break;
                }
            }

            System.out.println("从数据库加载数据成功！");
            System.out.println("成员数量: " + members.size());
            System.out.println("项目数量: " + projects.size());
            System.out.println("任务数量: " + tasks.size());

        } catch (Exception e) {
            System.err.println("从数据库加载数据失败: " + e.getMessage());
            e.printStackTrace();
            // 如果数据库加载失败，回退到内存模式
            initializeSampleData();
        }
    }

    private void initializeSampleData() {
        System.out.println("初始化示例数据...");

        TeamMember admin = new TeamMember("M001", "张经理", Role.ADMIN, "admin@company.com", "admin123");
        TeamMember user1 = new TeamMember("M002", "李开发", Role.USER, "dev1@company.com", "user123");
        TeamMember user2 = new TeamMember("M003", "王测试", Role.USER, "tester@company.com", "user123");

        // 只在数据库可用时保存到数据库
        if (databaseEnabled) {
            saveMemberToDatabase(admin);
            saveMemberToDatabase(user1);
            saveMemberToDatabase(user2);
        }

        members.put(admin.getMemberId(), admin);
        members.put(user1.getMemberId(), user1);
        members.put(user2.getMemberId(), user2);

        Project project1 = new Project("P001", "电商平台开发", "开发新一代电商平台",
                java.time.LocalDate.now().plusMonths(6));
        project1.addMember(admin);
        project1.addMember(user1);
        project1.addMember(user2);

        // 只在数据库可用时保存到数据库
        if (databaseEnabled) {
            saveProjectToDatabase(project1);
        }

        projects.put(project1.getProjectId(), project1);

        // 初始化任务分配映射
        taskAssignments.put(project1, new HashMap<>());
        taskAssignments.get(project1).put(user1, new ArrayList<>());
        taskAssignments.get(project1).put(user2, new ArrayList<>());

        System.out.println("示例数据初始化完成！");
    }

    // 数据库操作方法
    private void saveMemberToDatabase(TeamMember member) {
        if (!databaseEnabled) return;

        try {
            databaseManager.saveMember(member);
        } catch (Exception e) {
            System.err.println("保存成员到数据库失败: " + e.getMessage());
        }
    }

    private void saveProjectToDatabase(Project project) {
        if (!databaseEnabled) return;

        try {
            databaseManager.saveProject(project);
        } catch (Exception e) {
            System.err.println("保存项目到数据库失败: " + e.getMessage());
        }
    }

    private void saveTaskToDatabase(TaskItem task, String projectId) {
        if (!databaseEnabled) return;

        try {
            databaseManager.saveTask(task, projectId);
        } catch (Exception e) {
            System.err.println("保存任务到数据库失败: " + e.getMessage());
        }
    }

    // 用户管理方法
    public boolean registerUser(String name, String email, String password, Role role) {
        String memberId = "M" + String.format("%03d", members.size() + 1);
        TeamMember newMember = new TeamMember(memberId, name, role, email, password);

        saveMemberToDatabase(newMember);
        members.put(memberId, newMember);
        return true;
    }

    public boolean login(String email, String password) {
        Optional<TeamMember> user = members.values().stream()
                .filter(member -> member.getEmail().equals(email) && member.getPassword().equals(password))
                .findFirst();

        if (user.isPresent()) {
            currentUser = user.get();
            return true;
        }
        return false;
    }

    public void logout() {
        currentUser = null;
    }

    // 项目管理方法
    public boolean createProject(String name, String description, java.time.LocalDate dueDate) {
        if (!isAdminLoggedIn()) return false;

        String projectId = "P" + String.format("%03d", projects.size() + 1);
        Project project = new Project(projectId, name, description, dueDate);
        project.addMember(currentUser); // 项目经理自动加入项目

        saveProjectToDatabase(project);
        projects.put(projectId, project);
        taskAssignments.put(project, new HashMap<>());
        return true;
    }

    public boolean addMemberToProject(String projectId, String memberId) {
        if (!isAdminLoggedIn()) return false;

        Project project = projects.get(projectId);
        TeamMember member = members.get(memberId);

        if (project != null && member != null) {
            project.addMember(member);

            // 更新数据库
            saveProjectToDatabase(project);

            taskAssignments.get(project).putIfAbsent(member, new ArrayList<>());
            return true;
        }
        return false;
    }

    // 任务管理方法
    public String createFeatureTask(String projectId, String title, String description,
                                    Priority priority, java.time.LocalDate dueDate, int storyPoints, String category) {
        if (!isAdminLoggedIn()) return null;

        Project project = projects.get(projectId);
        if (project == null) return null;

        String taskId = "T" + String.format("%03d", tasks.size() + 1);
        FeatureTask task = new FeatureTask(taskId, title, description, priority, dueDate, storyPoints, category);

        saveTaskToDatabase(task, projectId);
        tasks.put(taskId, task);
        project.addTask(task);
        return taskId;
    }

    public String createBugReport(String projectId, String title, String description,
                                  Priority priority, java.time.LocalDate dueDate, Severity severity,
                                  String steps, String environment) {
        if (!isAdminLoggedIn()) return null;

        Project project = projects.get(projectId);
        if (project == null) return null;

        String taskId = "T" + String.format("%03d", tasks.size() + 1);
        BugReport task = new BugReport(taskId, title, description, priority, dueDate, severity, steps, environment);

        saveTaskToDatabase(task, projectId);
        tasks.put(taskId, task);
        project.addTask(task);
        return taskId;
    }

    public boolean assignTask(String taskId, String memberId) {
        if (!isAdminLoggedIn()) return false;

        TaskItem task = tasks.get(taskId);
        TeamMember member = members.get(memberId);

        if (task != null && member != null) {
            task.setAssignedTo(member);

            // 更新数据库
            if (databaseEnabled) {
                try {
                    databaseManager.assignTask(taskId, memberId);
                } catch (Exception e) {
                    System.err.println("更新任务分配到数据库失败: " + e.getMessage());
                }
            }

            // 更新任务分配映射
            for (Map.Entry<Project, Map<TeamMember, List<TaskItem>>> entry : taskAssignments.entrySet()) {
                if (entry.getKey().getTasks().contains(task)) {
                    entry.getValue().putIfAbsent(member, new ArrayList<>());
                    entry.getValue().get(member).add(task);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean updateTaskStatus(String taskId, TaskStatus status, double progress) {
        TaskItem task = tasks.get(taskId);
        if (task != null) {
            task.setStatus(status);
            task.setProgress(progress);

            // 更新数据库
            if (databaseEnabled) {
                try {
                    databaseManager.updateTaskStatus(taskId, status, progress);
                } catch (Exception e) {
                    System.err.println("更新任务状态到数据库失败: " + e.getMessage());
                }
            }
            return true;
        }
        return false;
    }

    // 查询方法
    public List<TaskItem> getAssignedTasks() {
        if (currentUser == null) return new ArrayList<>();

        List<TaskItem> assignedTasks = new ArrayList<>();
        for (Map<TeamMember, List<TaskItem>> assignment : taskAssignments.values()) {
            if (assignment.containsKey(currentUser)) {
                assignedTasks.addAll(assignment.get(currentUser));
            }
        }
        return assignedTasks;
    }

    public List<TaskItem> searchTasks(String projectId, TaskStatus status, Priority priority) {
        return tasks.values().stream()
                .filter(task -> projectId == null || taskBelongsToProject(task, projectId))
                .filter(task -> status == null || task.getStatus() == status)
                .filter(task -> priority == null || task.getPriority() == priority)
                .collect(Collectors.toList());
    }

    private boolean taskBelongsToProject(TaskItem task, String projectId) {
        Project project = projects.get(projectId);
        return project != null && project.getTasks().contains(task);
    }

    // 辅助方法
    private boolean isAdminLoggedIn() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    // Getter方法
    public TeamMember getCurrentUser() { return currentUser; }
    public Map<String, Project> getProjects() { return projects; }
    public Map<String, TeamMember> getMembers() { return members; }
    public Map<String, TaskItem> getTasks() { return tasks; }
    public boolean isDatabaseEnabled() { return databaseEnabled; }

    // 关闭数据库连接
    public void close() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }
}