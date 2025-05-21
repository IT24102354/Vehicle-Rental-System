package com.rental.vehicle.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * Base Admin entity class for storing admin information
 * Implements encapsulation for admin credentials and permissions
 * Parent class for inheritance hierarchy (SuperAdmin and StaffAdmin)
 */
@Entity
@Table(name = "admins")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "admin_type")
public abstract class Admin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Admin name is required")
    private String name;
    
    @NotBlank(message = "Admin email is required")
    private String email;
    
    @NotBlank(message = "Admin username is required")
    @Column(unique = true)
    private String username;
    
    @NotBlank(message = "Admin password is required")
    private String password;
    
    @NotBlank(message = "Admin role is required")
    private String role;
    
    private boolean active = true;
    
    private String lastLoginDate;
    
    /**
     * Check if admin has the specified permission
     * @param permissionName Name of the permission
     * @return true if admin has permission, false otherwise
     */
    public abstract boolean hasPermission(String permissionName);
    
    // Default constructor
    public Admin() {
    }
    
    // Constructor with parameters
    public Admin(String name, String email, String username, String password, String role) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public String getLastLoginDate() {
        return lastLoginDate;
    }
    
    public void setLastLoginDate(String lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
    
    @Override
    public String toString() {
        return "Admin{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
