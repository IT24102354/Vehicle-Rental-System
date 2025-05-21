package com.rental.vehicle.service;

import com.rental.vehicle.model.Admin;
import com.rental.vehicle.model.SuperAdmin;
import com.rental.vehicle.model.StaffAdmin;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Admin management
 * Defines methods for admin operations
 */
public interface AdminService {
    
    /**
     * Register a new admin
     * @param admin Admin object to register
     * @return Registered admin object
     * @throws Exception If registration fails
     */
    Admin registerAdmin(Admin admin) throws Exception;
    
    /**
     * Authenticate admin login
     * @param username Admin's username
     * @param password Admin's password
     * @return Authenticated admin object
     * @throws Exception If authentication fails
     */
    Admin authenticateAdmin(String username, String password) throws Exception;
    
    /**
     * Get admin by ID
     * @param id Admin ID
     * @return Admin object
     * @throws Exception If admin not found
     */
    Admin getAdminById(Long id) throws Exception;
    
    /**
     * Get all admins
     * @return List of all admin objects
     */
    List<Admin> getAllAdmins();
    
    /**
     * Update admin details
     * @param id Admin ID
     * @param updatedAdmin Updated admin details
     * @return Updated admin object
     * @throws Exception If update fails
     */
    Admin updateAdmin(Long id, Admin updatedAdmin) throws Exception;
    
    /**
     * Delete admin
     * @param id Admin ID
     * @throws Exception If deletion fails
     */
    void deleteAdmin(Long id) throws Exception;
    
    /**
     * Get admin by username
     * @param username Admin's username
     * @return Optional containing admin if found, empty otherwise
     */
    Optional<Admin> getAdminByUsername(String username);
    
    /**
     * Get all super admins
     * @return List of all super admin objects
     */
    List<SuperAdmin> getAllSuperAdmins();
    
    /**
     * Get all staff admins
     * @return List of all staff admin objects
     */
    List<StaffAdmin> getAllStaffAdmins();
    
    /**
     * Get staff admins by department
     * @param department Department name
     * @return List of staff admin objects in the given department
     */
    List<StaffAdmin> getStaffAdminsByDepartment(String department);
    
    /**
     * Get staff admins managed by a specific admin
     * @param managerAdminId ID of the manager admin
     * @return List of staff admin objects managed by the given admin
     */
    List<StaffAdmin> getStaffAdminsByManager(Long managerAdminId);
    
    /**
     * Add permission to admin
     * @param adminId Admin ID
     * @param permission Permission to add
     * @throws Exception If operation fails
     */
    void addPermission(Long adminId, String permission) throws Exception;
    
    /**
     * Remove permission from admin
     * @param adminId Admin ID
     * @param permission Permission to remove
     * @throws Exception If operation fails
     */
    void removePermission(Long adminId, String permission) throws Exception;
}
