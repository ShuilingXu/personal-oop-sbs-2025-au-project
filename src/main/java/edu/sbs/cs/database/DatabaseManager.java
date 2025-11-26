package edu.sbs.cs.database;

import edu.sbs.cs.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:database/team_management.db";
    private Connection connection;

    static {
        // 显式加载 SQLite JDBC 驱动
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite JDBC 驱动加载成功");
        } catch (ClassNotFoundException e) {
            System.err.println("无法加载 SQLite JDBC 驱动: " + e.getMessage());
            System.err.println("请确保 SQLite JDBC 驱动在类路径中");
        }
    }

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            // 确保数据库目录存在
            java.nio.file.Paths.get("database").toFile().mkdirs();

            System.out.println("正在连接数据库: " + DB_URL);
            connection = DriverManager.getConnection(DB_URL);

            // 设置连接属性
            connection.setAutoCommit(true);

            createTables();
            System.out.println("数据库初始化成功！");
        } catch (SQLException e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        // 启用外键约束
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }

        String[] createTableSQLs = {
                // 成员表
                "CREATE TABLE IF NOT EXISTS members (" +
                        "member_id TEXT PRIMARY KEY, " +
                        "name TEXT NOT NULL, " +
                        "role TEXT NOT NULL, " +
                        "email TEXT UNIQUE NOT NULL, " +
                        "password TEXT NOT NULL)",

                // 项目表
                "CREATE TABLE IF NOT EXISTS projects (" +
                        "project_id TEXT PRIMARY KEY, " +
                        "name TEXT NOT NULL, " +
                        "description TEXT, " +
                        "start_date TEXT, " +
                        "due_date TEXT)",

                // 任务表
                "CREATE TABLE IF NOT EXISTS tasks (" +
                        "task_id TEXT PRIMARY KEY, " +
                        "title TEXT NOT NULL, " +
                        "description TEXT, " +
                        "priority TEXT, " +
                        "status TEXT, " +
                        "due_date TEXT, " +
                        "progress REAL DEFAULT 0.0, " +
                        "task_type TEXT, " +
                        "assigned_to TEXT, " +
                        "project_id TEXT, " +
                        "story_points INTEGER, " +
                        "feature_category TEXT, " +
                        "severity TEXT, " +
                        "steps_to_reproduce TEXT, " +
                        "environment TEXT, " +
                        "FOREIGN KEY (assigned_to) REFERENCES members(member_id), " +
                        "FOREIGN KEY (project_id) REFERENCES projects(project_id))",

                // 项目成员关联表
                "CREATE TABLE IF NOT EXISTS project_members (" +
                        "project_id TEXT, " +
                        "member_id TEXT, " +
                        "PRIMARY KEY (project_id, member_id), " +
                        "FOREIGN KEY (project_id) REFERENCES projects(project_id), " +
                        "FOREIGN KEY (member_id) REFERENCES members(member_id))"
        };

        for (String sql : createTableSQLs) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
            } catch (SQLException e) {
                System.err.println("创建表失败 - SQL: " + sql);
                System.err.println("错误: " + e.getMessage());
                throw e;
            }
        }
    }

    // 检查连接是否有效
    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // 成员相关操作
    public void saveMember(TeamMember member) throws SQLException {
        if (!isConnectionValid()) {
            throw new SQLException("数据库连接不可用");
        }

        String sql = "INSERT OR REPLACE INTO members (member_id, name, role, email, password) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, member.getMemberId());
            pstmt.setString(2, member.getName());
            pstmt.setString(3, member.getRole().toString());
            pstmt.setString(4, member.getEmail());
            pstmt.setString(5, member.getPassword());
            pstmt.executeUpdate();
        }
    }

    public List<TeamMember> loadAllMembers() throws SQLException {
        if (!isConnectionValid()) {
            throw new SQLException("数据库连接不可用");
        }

        List<TeamMember> members = new ArrayList<>();
        String sql = "SELECT * FROM members";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String memberId = rs.getString("member_id");
                String name = rs.getString("name");
                Role role = Role.valueOf(rs.getString("role"));
                String email = rs.getString("email");
                String password = rs.getString("password");

                TeamMember member = new TeamMember(memberId, name, role, email, password);
                members.add(member);
            }
        }
        return members;
    }

    // 项目相关操作
    public void saveProject(Project project) throws SQLException {
        if (!isConnectionValid()) {
            throw new SQLException("数据库连接不可用");
        }

        String sql = "INSERT OR REPLACE INTO projects (project_id, name, description, start_date, due_date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, project.getProjectId());
            pstmt.setString(2, project.getName());
            pstmt.setString(3, project.getDescription());
            pstmt.setString(4, project.getStartDate().toString());
            pstmt.setString(5, project.getDueDate().toString());
            pstmt.executeUpdate();
        }

        // 保存项目成员关系
        saveProjectMembers(project);
    }

    private void saveProjectMembers(Project project) throws SQLException {
        if (!isConnectionValid()) {
            throw new SQLException("数据库连接不可用");
        }

        // 先删除旧的关系
        String deleteSql = "DELETE FROM project_members WHERE project_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteSql)) {
            pstmt.setString(1, project.getProjectId());
            pstmt.executeUpdate();
        }

        // 插入新的关系
        String insertSql = "INSERT INTO project_members (project_id, member_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
            for (TeamMember member : project.getMembers()) {
                pstmt.setString(1, project.getProjectId());
                pstmt.setString(2, member.getMemberId());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public List<Project> loadAllProjects() throws SQLException {
        if (!isConnectionValid()) {
            throw new SQLException("数据库连接不可用");
        }

        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String projectId = rs.getString("project_id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                LocalDate startDate = LocalDate.parse(rs.getString("start_date"));
                LocalDate dueDate = LocalDate.parse(rs.getString("due_date"));

                Project project = new Project(projectId, name, description, dueDate);
                project.getMembers().addAll(loadProjectMembers(projectId));
                projects.add(project);
            }
        }
        return projects;
    }

    private List<TeamMember> loadProjectMembers(String projectId) throws SQLException {
        if (!isConnectionValid()) {
            throw new SQLException("数据库连接不可用");
        }

        List<TeamMember> members = new ArrayList<>();
        String sql = "SELECT m.* FROM members m JOIN project_members pm ON m.member_id = pm.member_id WHERE pm.project_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, projectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String memberId = rs.getString("member_id");
                    String name = rs.getString("name");
                    Role role = Role.valueOf(rs.getString("role"));
                    String email = rs.getString("email");
                    String password = rs.getString("password");

                    TeamMember member = new TeamMember(memberId, name, role, email, password);
                    members.add(member);
                }
            }
        }
        return members;
    }

    // 任务相关操作
    public void saveTask(TaskItem task, String projectId) throws SQLException {
        if (!isConnectionValid()) {
            throw new SQLException("数据库连接不可用");
        }

        String sql = "INSERT OR REPLACE INTO tasks (task_id, title, description, priority, status, due_date, progress, " +
                "task_type, assigned_to, project_id, story_points, feature_category, severity, steps_to_reproduce, environment) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, task.getTaskId());
            pstmt.setString(2, task.getTitle());
            pstmt.setString(3, task.getDescription());
            pstmt.setString(4, task.getPriority().toString());
            pstmt.setString(5, task.getStatus().toString());
            pstmt.setString(6, task.getDueDate().toString());
            pstmt.setDouble(7, task.getProgress());

            if (task instanceof FeatureTask) {
                FeatureTask featureTask = (FeatureTask) task;
                pstmt.setString(8, "FEATURE");
                pstmt.setString(9, task.getAssignedTo() != null ? task.getAssignedTo().getMemberId() : null);
                pstmt.setString(10, projectId);
                pstmt.setInt(11, featureTask.getStoryPoints());
                pstmt.setString(12, featureTask.getFeatureCategory());
                pstmt.setNull(13, Types.VARCHAR);
                pstmt.setNull(14, Types.VARCHAR);
                pstmt.setNull(15, Types.VARCHAR);
            } else if (task instanceof BugReport) {
                BugReport bugReport = (BugReport) task;
                pstmt.setString(8, "BUG");
                pstmt.setString(9, task.getAssignedTo() != null ? task.getAssignedTo().getMemberId() : null);
                pstmt.setString(10, projectId);
                pstmt.setNull(11, Types.INTEGER);
                pstmt.setNull(12, Types.VARCHAR);
                pstmt.setString(13, bugReport.getSeverity().toString());
                pstmt.setString(14, bugReport.getStepsToReproduce());
                pstmt.setString(15, bugReport.getEnvironment());
            }

            pstmt.executeUpdate();
        }
    }

    public List<TaskItem> loadAllTasks(Map<String, TeamMember> membersMap) throws SQLException {
        if (!isConnectionValid()) {
            throw new SQLException("数据库连接不可用");
        }

        List<TaskItem> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String taskId = rs.getString("task_id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                Priority priority = Priority.valueOf(rs.getString("priority"));
                TaskStatus status = TaskStatus.valueOf(rs.getString("status"));
                LocalDate dueDate = LocalDate.parse(rs.getString("due_date"));
                double progress = rs.getDouble("progress");
                String taskType = rs.getString("task_type");
                String assignedToId = rs.getString("assigned_to");

                TeamMember assignedTo = assignedToId != null ? membersMap.get(assignedToId) : null;

                TaskItem task;
                if ("FEATURE".equals(taskType)) {
                    int storyPoints = rs.getInt("story_points");
                    String featureCategory = rs.getString("feature_category");
                    task = new FeatureTask(taskId, title, description, priority, dueDate, storyPoints, featureCategory);
                } else {
                    Severity severity = Severity.valueOf(rs.getString("severity"));
                    String stepsToReproduce = rs.getString("steps_to_reproduce");
                    String environment = rs.getString("environment");
                    task = new BugReport(taskId, title, description, priority, dueDate, severity, stepsToReproduce, environment);
                }

                task.setStatus(status);
                task.setProgress(progress);
                task.setAssignedTo(assignedTo);
                tasks.add(task);
            }
        }
        return tasks;
    }

    public void updateTaskStatus(String taskId, TaskStatus status, double progress) throws SQLException {
        if (!isConnectionValid()) {
            throw new SQLException("数据库连接不可用");
        }

        String sql = "UPDATE tasks SET status = ?, progress = ? WHERE task_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status.toString());
            pstmt.setDouble(2, progress);
            pstmt.setString(3, taskId);
            pstmt.executeUpdate();
        }
    }

    public void assignTask(String taskId, String memberId) throws SQLException {
        if (!isConnectionValid()) {
            throw new SQLException("数据库连接不可用");
        }

        String sql = "UPDATE tasks SET assigned_to = ? WHERE task_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, memberId);
            pstmt.setString(2, taskId);
            pstmt.executeUpdate();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("数据库连接已关闭");
            }
        } catch (SQLException e) {
            System.err.println("关闭数据库连接失败: " + e.getMessage());
        }
    }
}