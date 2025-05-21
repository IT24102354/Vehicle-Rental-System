package com.rental.vehicle.model;

import jakarta.persistence.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * StaffAdmin entity class for storing staff admin information
 * Extends the base Admin class with limited access
 * Implements role-based permissions system
 */
@Entity
@Table(name = "staff_admins")
@DiscriminatorValue("STAFF_ADMIN")
public class StaffAdmin extends Admin {
    
    // StaffAdmin has limited access to the system
    private boolean canManageUsers = true;
    private boolean canManageVehicles = true;
    private boolean canManageRentals = true;
    private boolean canViewReports = false;
    
    // Department this staff admin belongs to
    private String department;
    
    // Reporting manager (another admin)
    private Long managerAdminId;
    
    // Stores specific permissions for this StaffAdmin
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "staff_admin_permissions", joinColumns = @JoinColumn(name = "admin_id"))
    @Column(name = "permission")
    private Set<String> permissions = new HashSet<>(Arrays.asList(
            "MANAGE_USERS", "MANAGE_VEHICLES", "MANAGE_RENTALS"));
    
    // Default constructor
    public StaffAdmin() {
        super();
        setRole("STAFF_ADMIN");
    }
    
    // Constructor with parameters
    public StaffAdmin(String name, String email, String username, String password, String department) {
        super(name, email, username, password, "STAFF_ADMIN");
        this.department = department;
    }
    
    // Implementation of abstract method
    @Override
    public boolean hasPermission(String permissionName) {
        return permissions.contains(permissionName);
    }
    
    // Getters and Setters for StaffAdmin-specific fields
    public boolean isCanManageUsers() {
        return canManageUsers;
    }
    
    public void setCanManageUsers(boolean canManageUsers) {
        this.canManageUsers = canManageUsers;
        if (canManageUsers) {
            permissions.add("MANAGE_USERS");
        } else {
            permissions.remove("MANAGE_USERS");
        }
    }
    
    public boolean isCanManageVehicles() {
        return canManageVehicles;
    }
    
    public void setCanManageVehicles(boolean canManageVehicles) {
        this.canManageVehicles = canManageVehicles;
        if (canManageVehicles) {
            permissions.add("MANAGE_VEHICLES");
        } else {
            permissions.remove("MANAGE_VEHICLES");
        }
    }
    
    public boolean isCanManageRentals() {
        return canManageRentals;
    }
    
    public void setCanManageRentals(boolean canManageRentals) {
        this.canManageRentals = canManageRentals;
        if (canManageRentals) {
            permissions.add("MANAGE_RENTALS");
        } else {
            permissions.remove("MANAGE_RENTALS");
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
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public Long getManagerAdminId() {
        return managerAdminId;
    }
    
    public void setManagerAdminId(Long managerAdminId) {
        this.managerAdminId = managerAdminId;
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
