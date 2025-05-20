package com.rental.vehicle.repository;

import com.rental.vehicle.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for User entity
 * Extends JpaRepository to inherit basic CRUD operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find a user by username
     * @param username Username to search for
     * @return User if found, null otherwise
     */
    User findByUsername(String username);
    
    /**
     * Find a user by email
     * @param email Email to search for
     * @return User if found, null otherwise
     */
    User findByEmail(String email);
    
    /**
     * Check if a username exists
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if an email exists
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);
}
