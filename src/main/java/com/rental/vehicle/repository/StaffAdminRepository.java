package com.rental.vehicle.repository;

import com.rental.vehicle.model.StaffAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for StaffAdmin entity
 * Provides methods for CRUD operations on StaffAdmin objects
 */
@Repository
public interface StaffAdminRepository extends JpaRepository<StaffAdmin, Long> {
    
    /**
     * Find staff admin by username
     * @param username StaffAdmin's username
     * @return StaffAdmin if found, null otherwise
     */
    StaffAdmin findByUsername(String username);
    
    /**
     * Find staff admins by department
     * @param department Department name
     * @return List of StaffAdmin objects in the given department
     */
    List<StaffAdmin> findByDepartment(String department);
    
    /**
     * Find staff admins by manager admin ID
     * @param managerAdminId ID of the manager admin
     * @return List of StaffAdmin objects managed by the given admin
     */
    List<StaffAdmin> findByManagerAdminId(Long managerAdminId);
}
