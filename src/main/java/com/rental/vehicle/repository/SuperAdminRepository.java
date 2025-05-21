package com.rental.vehicle.repository;

import com.rental.vehicle.model.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for SuperAdmin entity
 * Provides methods for CRUD operations on SuperAdmin objects
 */
@Repository
public interface SuperAdminRepository extends JpaRepository<SuperAdmin, Long> {
    
    /**
     * Find super admin by username
     * @param username SuperAdmin's username
     * @return SuperAdmin if found, null otherwise
     */
    SuperAdmin findByUsername(String username);
}
