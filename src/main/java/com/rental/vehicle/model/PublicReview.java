package com.rental.vehicle.model;

import jakarta.persistence.*;

/**
 * PublicReview entity class that inherits from Review
 * Represents reviews submitted by any registered user
 */
@Entity
@Table(name = "public_reviews")
@PrimaryKeyJoinColumn(name = "review_id")
@DiscriminatorValue("PUBLIC")
public class PublicReview extends Review {
    
    private String userIpAddress;
    
    private Boolean displayUserName = true;
    
    // Default constructor
    public PublicReview() {
        super();
    }
    
    // Constructor with parameters
    public PublicReview(Integer rating, String comment, User user, Vehicle vehicle, 
                       String userIpAddress, Boolean displayUserName) {
        super(rating, comment, user, vehicle);
        this.userIpAddress = userIpAddress;
        this.displayUserName = displayUserName;
    }
    
    // Getters and Setters
    public String getUserIpAddress() {
        return userIpAddress;
    }
    
    public void setUserIpAddress(String userIpAddress) {
        this.userIpAddress = userIpAddress;
    }
    
    public Boolean getDisplayUserName() {
        return displayUserName;
    }
    
    public void setDisplayUserName(Boolean displayUserName) {
        this.displayUserName = displayUserName;
    }
    
    /**
     * Validates if a public review meets the criteria
     * Public reviews require a minimum comment length of 10 characters
     */
    @Override
    public boolean validateReview() {
        String comment = getComment();
        return comment != null && comment.trim().length() >= 10;
    }
}
