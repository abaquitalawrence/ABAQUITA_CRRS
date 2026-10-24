
package model;

public class User {

    private int id;
    private String username;
    private String password;
    private String roles;
    private String status;

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

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}
