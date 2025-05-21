package com.rental.vehicle.repository;

import com.rental.vehicle.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Admin entity
 * Provides methods for CRUD operations on Admin objects
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    /**
     * Find admin by username
     * @param username Admin's username
     * @return Optional containing admin if found, empty otherwise
     */
    Optional<Admin> findByUsername(String username);
    
    /**
     * Find admin by email
     * @param email Admin's email
     * @return Optional containing admin if found, empty otherwise
     */
    Optional<Admin> findByEmail(String email);
    
    /**
     * Check if admin with given username exists
     * @param username Admin's username
     * @return true if exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if admin with given email exists
     * @param email Admin's email
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);
}
