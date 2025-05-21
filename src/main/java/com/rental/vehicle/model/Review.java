package com.rental.vehicle.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Base Review entity class for storing review information
 * Implements encapsulation by storing review attributes
 * Parent class for inheritance hierarchy (PublicReview, VerifiedReview)
 */
@Entity
@Table(name = "reviews")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "review_type")
public abstract class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;
    
    @NotBlank(message = "Comment is required")
    @Column(length = 1000)
    private String comment;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
    
    @NotNull
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Boolean approved = false;
    
    // Abstract method for review validation
    public abstract boolean validateReview();
    
    // Default constructor
    public Review() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor with parameters
    public Review(Integer rating, String comment, User user, Vehicle vehicle) {
        this.rating = rating;
        this.comment = comment;
        this.user = user;
        this.vehicle = vehicle;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getRating() {
        return rating;
    }
    
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Vehicle getVehicle() {
        return vehicle;
    }
    
    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Boolean getApproved() {
        return approved;
    }
    
    public void setApproved(Boolean approved) {
        this.approved = approved;
    }
    
    /**
     * Returns the type of review (PublicReview, VerifiedReview) based on the discriminator value
     * This is used in templates to display the review type
     * @return String representing the review type
     */
    public String getReviewType() {
        String className = this.getClass().getSimpleName();
        return className != null ? className : "Review";
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
