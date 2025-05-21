package com.rental.vehicle.repository;

import com.rental.vehicle.model.Review;
import com.rental.vehicle.model.User;
import com.rental.vehicle.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Review entity
 * Extends JpaRepository for CRUD operations
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    /**
     * Find reviews by vehicle
     * @param vehicle The vehicle to find reviews for
     * @return List of reviews for the vehicle
     */
    List<Review> findByVehicle(Vehicle vehicle);
    
    /**
     * Find reviews by user
     * @param user The user to find reviews for
     * @return List of reviews by the user
     */
    List<Review> findByUser(User user);
    
    /**
     * Find approved reviews by vehicle
     * @param vehicle The vehicle to find reviews for
     * @param approved Whether the reviews are approved
     * @return List of approved reviews for the vehicle
     */
    List<Review> findByVehicleAndApproved(Vehicle vehicle, Boolean approved);
    
    /**
     * Find reviews by user and vehicle
     * @param user The user who created the reviews
     * @param vehicle The vehicle the reviews are for
     * @return List of reviews by the user for the vehicle
     */
    List<Review> findByUserAndVehicle(User user, Vehicle vehicle);
    
    /**
     * Find reviews by approval status
     * @param approved Whether the reviews are approved
     * @return List of reviews with the given approval status
     */
    List<Review> findByApproved(Boolean approved);
}
