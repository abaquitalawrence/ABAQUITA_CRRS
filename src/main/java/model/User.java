
package model;

public class User {

    private final int id;
    private final String username;
    private final String password;
    private final String roles;
    private final String status;

    public User(int id, String username, String password,
                String roles, String status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRoles() {
        return roles;
    }

    public String getRole() {
        return roles;
    }

    public String getStatus() {
        return status;
    }

    public boolean isApproved() {
        return "APPROVED".equalsIgnoreCase(status);
    }

    public boolean isActive() {
        return isApproved();
    }
}
