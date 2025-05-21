package com.rental.vehicle.service;

import com.rental.vehicle.model.*;
import com.rental.vehicle.repository.PublicReviewRepository;
import com.rental.vehicle.repository.ReviewRepository;
import com.rental.vehicle.repository.VerifiedReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Implementation of ReviewService
 * Provides methods for managing reviews
 */
@Service
public class ReviewServiceImpl implements ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private PublicReviewRepository publicReviewRepository;
    
    @Autowired
    private VerifiedReviewRepository verifiedReviewRepository;
    
    @Override
    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }
    
    @Override
    public PublicReview createPublicReview(PublicReview publicReview) throws IllegalArgumentException {
        if (!publicReview.validateReview()) {
            throw new IllegalArgumentException("Invalid review. Comment must be at least 10 characters long.");
        }
        
        // Check if user has already reviewed this vehicle
        if (hasUserReviewedVehicle(publicReview.getUser(), publicReview.getVehicle())) {
            throw new IllegalArgumentException("You have already reviewed this vehicle.");
        }
        
        return publicReviewRepository.save(publicReview);
    }
    
    @Override
    public VerifiedReview createVerifiedReview(VerifiedReview verifiedReview) throws IllegalArgumentException {
        if (!verifiedReview.validateReview()) {
            throw new IllegalArgumentException("Invalid review. Must be associated with a valid rental.");
        }
        
        // Check if there's already a review for this rental
        if (hasVerifiedReviewForRental(verifiedReview.getRental())) {
            throw new IllegalArgumentException("A review already exists for this rental.");
        }
        
        return verifiedReviewRepository.save(verifiedReview);
    }
    
    @Override
    public Review getReviewById(Long id) throws IllegalArgumentException {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + id));
    }
    
    @Override
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }
    
    @Override
    public List<Review> getReviewsByVehicle(Vehicle vehicle) {
        return reviewRepository.findByVehicle(vehicle);
    }
    
    @Override
    public List<Review> getApprovedReviewsByVehicle(Vehicle vehicle) {
        return reviewRepository.findByVehicleAndApproved(vehicle, true);
    }
    
    @Override
    public List<Review> getReviewsByUser(User user) {
        return reviewRepository.findByUser(user);
    }
    
    @Override
    public Review updateReview(Long id, Review updatedReview) throws IllegalArgumentException {
        Review existingReview = getReviewById(id);
        
        // Only update modifiable fields
        existingReview.setRating(updatedReview.getRating());
        existingReview.setComment(updatedReview.getComment());
        existingReview.setUpdatedAt(LocalDateTime.now());
        
        // If it's a public review, update those specific fields
        if (existingReview instanceof PublicReview && updatedReview instanceof PublicReview) {
            PublicReview existingPublicReview = (PublicReview) existingReview;
            PublicReview updatedPublicReview = (PublicReview) updatedReview;
            existingPublicReview.setDisplayUserName(updatedPublicReview.getDisplayUserName());
        }
        
        // Revalidate the review
        if (!existingReview.validateReview()) {
            throw new IllegalArgumentException("Updated review is invalid.");
        }
        
        return reviewRepository.save(existingReview);
    }
    
    @Override
    public void deleteReview(Long id) throws IllegalArgumentException {
        Review review = getReviewById(id);
        reviewRepository.delete(review);
    }
    
    @Override
    public Review approveReview(Long id) throws IllegalArgumentException {
        Review review = getReviewById(id);
        review.setApproved(true);
        return reviewRepository.save(review);
    }
    
    @Override
    public Review rejectReview(Long id) throws IllegalArgumentException {
        Review review = getReviewById(id);
        review.setApproved(false);
        return reviewRepository.save(review);
    }
    
    @Override
    public List<Review> getReviewsPendingApproval() {
        return reviewRepository.findByApproved(false);
    }
    
    @Override
    public boolean hasUserReviewedVehicle(User user, Vehicle vehicle) {
        return !reviewRepository.findByUserAndVehicle(user, vehicle).isEmpty();
    }
    
    @Override
    public boolean hasVerifiedReviewForRental(Rental rental) {
        return verifiedReviewRepository.existsByRental(rental);
    }
    
    @Override
    public double getAverageRatingForVehicle(Vehicle vehicle) {
        List<Review> approvedReviews = getApprovedReviewsByVehicle(vehicle);
        
        if (approvedReviews.isEmpty()) {
            return 0.0;
        }
        
        double sum = 0.0;
        for (Review review : approvedReviews) {
            sum += review.getRating();
        }
        
        return sum / approvedReviews.size();
    }
}
