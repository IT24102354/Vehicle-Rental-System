package com.rental.vehicle.repository;

import com.rental.vehicle.model.Rental;
import com.rental.vehicle.model.User;
import com.rental.vehicle.model.Vehicle;
import com.rental.vehicle.model.VerifiedReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for VerifiedReview entity
 * Extends JpaRepository for CRUD operations
 */
@Repository
public interface VerifiedReviewRepository extends JpaRepository<VerifiedReview, Long> {
    
    /**
     * Find verified reviews by vehicle
     * @param vehicle The vehicle to find reviews for
     * @return List of verified reviews for the vehicle
     */
    List<VerifiedReview> findByVehicle(Vehicle vehicle);
    
    /**
     * Find verified reviews by user
     * @param user The user to find reviews for
     * @return List of verified reviews by the user
     */
    List<VerifiedReview> findByUser(User user);
    
    /**
     * Find verified reviews by rental
     * @param rental The rental to find reviews for
     * @return List of verified reviews for the rental
     */
    List<VerifiedReview> findByRental(Rental rental);
    
    /**
     * Find a verified review by rental
     * @param rental The rental to find the review for
     * @return Optional containing the verified review for the rental, if it exists
     */
    Optional<VerifiedReview> findOneByRental(Rental rental);
    
    /**
     * Check if a verified review exists for a rental
     * @param rental The rental to check for
     * @return True if a verified review exists for the rental, false otherwise
     */
    boolean existsByRental(Rental rental);
}
