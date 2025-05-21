package com.rental.vehicle.model;

import jakarta.persistence.*;

/**
 * VerifiedReview entity class that inherits from Review
 * Represents reviews submitted by users who have actually rented the vehicle
 */
@Entity
@Table(name = "verified_reviews")
@PrimaryKeyJoinColumn(name = "review_id")
@DiscriminatorValue("VERIFIED")
public class VerifiedReview extends Review {
    
    @ManyToOne
    @JoinColumn(name = "rental_id")
    private Rental rental;
    
    private Boolean verified = true;
    
    private String verificationDetails;
    
    // Default constructor
    public VerifiedReview() {
        super();
    }
    
    // Constructor with parameters
    public VerifiedReview(Integer rating, String comment, User user, Vehicle vehicle, 
                         Rental rental, String verificationDetails) {
        super(rating, comment, user, vehicle);
        this.rental = rental;
        this.verificationDetails = verificationDetails;
        this.setApproved(true); // Verified reviews are automatically approved
    }
    
    // Getters and Setters
    public Rental getRental() {
        return rental;
    }
    
    public void setRental(Rental rental) {
        this.rental = rental;
    }
    
    public Boolean getVerified() {
        return verified;
    }
    
    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
    
    public String getVerificationDetails() {
        return verificationDetails;
    }
    
    public void setVerificationDetails(String verificationDetails) {
        this.verificationDetails = verificationDetails;
    }
    
    /**
     * Validates if a verified review meets the criteria
     * Verified reviews must have a linked rental record
     */
    @Override
    public boolean validateReview() {
        return rental != null && getUser() != null && getVehicle() != null;
    }
}
