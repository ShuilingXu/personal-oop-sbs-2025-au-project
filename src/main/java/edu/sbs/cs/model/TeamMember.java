package edu.sbs.cs.model;

public class TeamMember {
    private String memberId;
    private String name;
    private Role role;
    private String email;
    private String password;

    public TeamMember(String memberId, String name, Role role, String email, String password) {
        this.memberId = memberId;
        this.name = name;
        this.role = role;
        this.email = email;
        this.password = password;
    }

    // Getter和Setter
    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return name + " (" + role + ")";
    }
}