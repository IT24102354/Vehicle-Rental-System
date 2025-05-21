package com.rental.vehicle.service;

import com.rental.vehicle.model.Admin;
import com.rental.vehicle.model.SuperAdmin;
import com.rental.vehicle.model.StaffAdmin;
import com.rental.vehicle.repository.AdminRepository;
import com.rental.vehicle.repository.SuperAdminRepository;
import com.rental.vehicle.repository.StaffAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for Admin management
 * Implements business logic for admin operations
 */
@Service
public class AdminServiceImpl implements AdminService {
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private SuperAdminRepository superAdminRepository;
    
    @Autowired
    private StaffAdminRepository staffAdminRepository;
    
    @Override
    public Admin registerAdmin(Admin admin) throws Exception {
        // Check if username already exists
        if (adminRepository.existsByUsername(admin.getUsername())) {
            throw new Exception("Username already exists");
        }
        
        // Check if email already exists
        if (adminRepository.existsByEmail(admin.getEmail())) {
            throw new Exception("Email already exists");
        }
        
        // Set password (in real world, this would be encrypted)
        // For demo purposes, we'll just store it as is
        
        return adminRepository.save(admin);
    }
    
    @Override
    public Admin authenticateAdmin(String username, String password) throws Exception {
        Optional<Admin> adminOpt = adminRepository.findByUsername(username);
        
        if (adminOpt.isEmpty()) {
            throw new Exception("Invalid username or password");
        }
        
        Admin admin = adminOpt.get();
        
        if (!admin.getPassword().equals(password)) {
            throw new Exception("Invalid username or password");
        }
        
        if (!admin.isActive()) {
            throw new Exception("Admin account is inactive");
        }
        
        // Update last login date
        admin.setLastLoginDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        adminRepository.save(admin);
        
        return admin;
    }
    
    @Override
    public Admin getAdminById(Long id) throws Exception {
        return adminRepository.findById(id)
                .orElseThrow(() -> new Exception("Admin not found"));
    }
    
    @Override
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }
    
    @Override
    public Admin updateAdmin(Long id, Admin updatedAdmin) throws Exception {
        Admin existingAdmin = getAdminById(id);
        
        // Update fields
        existingAdmin.setName(updatedAdmin.getName());
        existingAdmin.setEmail(updatedAdmin.getEmail());
        existingAdmin.setRole(updatedAdmin.getRole());
        existingAdmin.setActive(updatedAdmin.isActive());
        
        // If password is provided and not empty, update it
        if (updatedAdmin.getPassword() != null && !updatedAdmin.getPassword().isEmpty()) {
            existingAdmin.setPassword(updatedAdmin.getPassword());
        }
        
        // Handle specific subclass updates using pattern matching with instanceof
        if (existingAdmin instanceof SuperAdmin existingSuperAdmin && updatedAdmin instanceof SuperAdmin updatedSuperAdmin) {
            existingSuperAdmin.setCanManageAdmins(updatedSuperAdmin.isCanManageAdmins());
            existingSuperAdmin.setCanViewReports(updatedSuperAdmin.isCanViewReports());
            existingSuperAdmin.setCanConfigureSystem(updatedSuperAdmin.isCanConfigureSystem());
        } else if (existingAdmin instanceof StaffAdmin existingStaffAdmin && updatedAdmin instanceof StaffAdmin updatedStaffAdmin) {
            existingStaffAdmin.setCanManageUsers(updatedStaffAdmin.isCanManageUsers());
            existingStaffAdmin.setCanManageVehicles(updatedStaffAdmin.isCanManageVehicles());
            existingStaffAdmin.setCanManageRentals(updatedStaffAdmin.isCanManageRentals());
            existingStaffAdmin.setCanViewReports(updatedStaffAdmin.isCanViewReports());
            existingStaffAdmin.setDepartment(updatedStaffAdmin.getDepartment());
            existingStaffAdmin.setManagerAdminId(updatedStaffAdmin.getManagerAdminId());
        }
        
        return adminRepository.save(existingAdmin);
    }
    
    @Override
    public void deleteAdmin(Long id) throws Exception {
        Admin admin = getAdminById(id);
        
        // If admin is a SuperAdmin, check if it's the last one
        if (admin instanceof SuperAdmin) {
            long superAdminCount = superAdminRepository.count();
            if (superAdminCount <= 1) {
                throw new Exception("Cannot delete the last SuperAdmin");
            }
        }
        
        // Check if this admin is managing any staff admins
        List<StaffAdmin> managedStaffAdmins = staffAdminRepository.findByManagerAdminId(id);
        if (!managedStaffAdmins.isEmpty()) {
            throw new Exception("Cannot delete admin who is managing staff admins");
        }
        
        adminRepository.delete(admin);
    }
    
    @Override
    public Optional<Admin> getAdminByUsername(String username) {
        return adminRepository.findByUsername(username);
    }
    
    @Override
    public List<SuperAdmin> getAllSuperAdmins() {
        return superAdminRepository.findAll();
    }
    
    @Override
    public List<StaffAdmin> getAllStaffAdmins() {
        return staffAdminRepository.findAll();
    }
    
    @Override
    public List<StaffAdmin> getStaffAdminsByDepartment(String department) {
        return staffAdminRepository.findByDepartment(department);
    }
    
    @Override
    public List<StaffAdmin> getStaffAdminsByManager(Long managerAdminId) {
        return staffAdminRepository.findByManagerAdminId(managerAdminId);
    }
    
    @Override
    public void addPermission(Long adminId, String permission) throws Exception {
        Admin admin = getAdminById(adminId);
        
        // Using pattern matching with instanceof (Java 16+)
        if (admin instanceof SuperAdmin superAdmin) {
            superAdmin.addPermission(permission);
        } else if (admin instanceof StaffAdmin staffAdmin) {
            staffAdmin.addPermission(permission);
        } else {
            throw new Exception("Cannot add permission to this admin type");
        }
        
        adminRepository.save(admin);
    }
    
    @Override
    public void removePermission(Long adminId, String permission) throws Exception {
        Admin admin = getAdminById(adminId);
        
        // Using pattern matching with instanceof (Java 16+)
        if (admin instanceof SuperAdmin superAdmin) {
            superAdmin.removePermission(permission);
        } else if (admin instanceof StaffAdmin staffAdmin) {
            staffAdmin.removePermission(permission);
        } else {
            throw new Exception("Cannot remove permission from this admin type");
        }
        
        adminRepository.save(admin);
    }
}
