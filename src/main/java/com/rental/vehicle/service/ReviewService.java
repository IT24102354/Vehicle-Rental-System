package com.rental.vehicle.service;

import com.rental.vehicle.model.*;

import java.util.List;

/**
 * Service interface for managing reviews
 */
public interface ReviewService {
    
    /**
     * Save a review
     * @param review The review to save
     * @return The saved review
     */
    Review saveReview(Review review);
    
    /**
     * Create a public review
     * @param publicReview The public review to create
     * @return The created public review
     * @throws IllegalArgumentException if the review is invalid
     */
    PublicReview createPublicReview(PublicReview publicReview) throws IllegalArgumentException;
    
    /**
     * Create a verified review
     * @param verifiedReview The verified review to create
     * @return The created verified review
     * @throws IllegalArgumentException if the review is invalid or if a review already exists for the rental
     */
    VerifiedReview createVerifiedReview(VerifiedReview verifiedReview) throws IllegalArgumentException;
    
    /**
     * Get a review by ID
     * @param id The ID of the review
     * @return The review with the given ID
     * @throws IllegalArgumentException if the review doesn't exist
     */
    Review getReviewById(Long id) throws IllegalArgumentException;
    
    /**
     * Get all reviews
     * @return List of all reviews
     */
    List<Review> getAllReviews();
    
    /**
     * Get reviews by vehicle
     * @param vehicle The vehicle to get reviews for
     * @return List of reviews for the vehicle
     */
    List<Review> getReviewsByVehicle(Vehicle vehicle);
    
    /**
     * Get approved reviews by vehicle
     * @param vehicle The vehicle to get reviews for
     * @return List of approved reviews for the vehicle
     */
    List<Review> getApprovedReviewsByVehicle(Vehicle vehicle);
    
    /**
     * Get reviews by user
     * @param user The user to get reviews for
     * @return List of reviews by the user
     */
    List<Review> getReviewsByUser(User user);
    
    /**
     * Update a review
     * @param id The ID of the review to update
     * @param updatedReview The updated review details
     * @return The updated review
     * @throws IllegalArgumentException if the review doesn't exist or is invalid
     */
    Review updateReview(Long id, Review updatedReview) throws IllegalArgumentException;
    
    /**
     * Delete a review
     * @param id The ID of the review to delete
     * @throws IllegalArgumentException if the review doesn't exist
     */
    void deleteReview(Long id) throws IllegalArgumentException;
    
    /**
     * Approve a review
     * @param id The ID of the review to approve
     * @return The approved review
     * @throws IllegalArgumentException if the review doesn't exist
     */
    Review approveReview(Long id) throws IllegalArgumentException;
    
    /**
     * Reject a review
     * @param id The ID of the review to reject
     * @return The rejected review
     * @throws IllegalArgumentException if the review doesn't exist
     */
    Review rejectReview(Long id) throws IllegalArgumentException;
    
    /**
     * Get reviews pending approval
     * @return List of reviews pending approval
     */
    List<Review> getReviewsPendingApproval();
    
    /**
     * Check if a user has reviewed a vehicle
     * @param user The user
     * @param vehicle The vehicle
     * @return True if the user has reviewed the vehicle, false otherwise
     */
    boolean hasUserReviewedVehicle(User user, Vehicle vehicle);
    
    /**
     * Check if a user has a verified review for a rental
     * @param rental The rental
     * @return True if the user has a verified review for the rental, false otherwise
     */
    boolean hasVerifiedReviewForRental(Rental rental);
    
    /**
     * Get the average rating for a vehicle
     * @param vehicle The vehicle
     * @return The average rating for the vehicle
     */
    double getAverageRatingForVehicle(Vehicle vehicle);
}
