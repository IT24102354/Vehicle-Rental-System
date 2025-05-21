package com.rental.vehicle.model;

import jakarta.persistence.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * SuperAdmin entity class for storing super admin information
 * Extends the base Admin class with full system access
 * Implements role-based permissions system
 */
@Entity
@Table(name = "super_admins")
@DiscriminatorValue("SUPER_ADMIN")
public class SuperAdmin extends Admin {
    
    // SuperAdmin has access to all areas of the system
    private boolean canManageAdmins = true;
    private boolean canViewReports = true;
    private boolean canConfigureSystem = true;
    
    // Stores specific permissions for this SuperAdmin
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "super_admin_permissions", joinColumns = @JoinColumn(name = "admin_id"))
    @Column(name = "permission")
    private Set<String> permissions = new HashSet<>(Arrays.asList(
            "MANAGE_USERS", "MANAGE_ADMINS", "MANAGE_VEHICLES", 
            "MANAGE_RENTALS", "VIEW_REPORTS", "SYSTEM_CONFIG"));
    
    // Default constructor
    public SuperAdmin() {
        super();
        setRole("SUPER_ADMIN");
    }
    
    // Constructor with parameters
    public SuperAdmin(String name, String email, String username, String password) {
        super(name, email, username, password, "SUPER_ADMIN");
    }
    
    // Implementation of abstract method
    @Override
    public boolean hasPermission(String permissionName) {
        return permissions.contains(permissionName);
    }
    
    // Getters and Setters for SuperAdmin-specific fields
    public boolean isCanManageAdmins() {
        return canManageAdmins;
    }
    
    public void setCanManageAdmins(boolean canManageAdmins) {
        this.canManageAdmins = canManageAdmins;
        if (canManageAdmins) {
            permissions.add("MANAGE_ADMINS");
        } else {
            permissions.remove("MANAGE_ADMINS");
        }
    }
    
    public boolean isCanViewReports() {
        return canViewReports;
    }
    
    public void setCanViewReports(boolean canViewReports) {
        this.canViewReports = canViewReports;
        if (canViewReports) {
            permissions.add("VIEW_REPORTS");
        } else {
            permissions.remove("VIEW_REPORTS");
        }
    }
    
    public boolean isCanConfigureSystem() {
        return canConfigureSystem;
    }
    
    public void setCanConfigureSystem(boolean canConfigureSystem) {
        this.canConfigureSystem = canConfigureSystem;
        if (canConfigureSystem) {
            permissions.add("SYSTEM_CONFIG");
        } else {
            permissions.remove("SYSTEM_CONFIG");
        }
    }
    
    public Set<String> getPermissions() {
        return permissions;
    }
    
    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }
    
    public void addPermission(String permission) {
        this.permissions.add(permission);
    }
    
    public void removePermission(String permission) {
        this.permissions.remove(permission);
    }
}
