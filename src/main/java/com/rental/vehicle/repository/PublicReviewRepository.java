package com.rental.vehicle.repository;

import com.rental.vehicle.model.PublicReview;
import com.rental.vehicle.model.User;
import com.rental.vehicle.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for PublicReview entity
 * Extends JpaRepository for CRUD operations
 */
@Repository
public interface PublicReviewRepository extends JpaRepository<PublicReview, Long> {
    
    /**
     * Find public reviews by vehicle
     * @param vehicle The vehicle to find reviews for
     * @return List of public reviews for the vehicle
     */
    List<PublicReview> findByVehicle(Vehicle vehicle);
    
    /**
     * Find public reviews by user
     * @param user The user to find reviews for
     * @return List of public reviews by the user
     */
    List<PublicReview> findByUser(User user);
    
    /**
     * Find public reviews by approval status
     * @param approved Whether the reviews are approved
     * @return List of public reviews with the given approval status
     */
    List<PublicReview> findByApproved(Boolean approved);
    
    /**
     * Find public reviews by user display name preference
     * @param displayUserName Whether to display the user's name
     * @return List of public reviews with the given display name preference
     */
    List<PublicReview> findByDisplayUserName(Boolean displayUserName);
}
