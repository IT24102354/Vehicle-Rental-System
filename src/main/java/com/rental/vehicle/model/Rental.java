package com.rental.vehicle.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Rental entity class for storing rental transactions
 * Implements CRUD functionalities for Booking and Rental Management component
 */
@Entity
@Table(name = "rentals")
public class Rental {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    @NotNull(message = "Vehicle is required")
    private Vehicle vehicle;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    private LocalDate returnDate;
    
    private double totalCost;
    
    private String status; // PENDING, ACTIVE, COMPLETED, CANCELLED
    
    private String paymentStatus; // PENDING, PAID, REFUNDED
    
    private String notes;
    
    // Constructor with required fields
    public Rental(Vehicle vehicle, User user, LocalDate startDate, LocalDate endDate) {
        this.vehicle = vehicle;
        this.user = user;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = "PENDING";
        this.paymentStatus = "PENDING";
        calculateTotalCost();
    }
    
    // Default constructor required by JPA
    public Rental() {
    }
    
    /**
     * Calculate the total cost of the rental based on the vehicle's daily rate and rental duration
     * Demonstrates polymorphism as different vehicle types may have different pricing models
     */
    public void calculateTotalCost() {
        if (vehicle != null && startDate != null && endDate != null) {
            // Calculate number of days between start and end dates
            long days = ChronoUnit.DAYS.between(startDate, endDate);
            
            // If same day rental, count as 1 day minimum
            if (days == 0) {
                days = 1;
            }
            
            // Calculate the total cost using the vehicle's pricing model
            this.totalCost = vehicle.calculateRentalPrice((int)days).doubleValue();
        }
    }
    
    /**
     * Check if the vehicle is available for the requested dates
     * Demonstrates abstraction by hiding the implementation details
     * @return true if available, false otherwise
     */
    public boolean checkAvailability() {
        // This would typically check against existing rentals in the database
        // For now, we'll just check if the vehicle is marked as available
        return vehicle != null && vehicle.getAvailable();
    }
    
    /**
     * Process the return of a vehicle
     * @param returnDate The date when the vehicle was returned
     * @param notes Any notes about the return condition
     */
    public void processReturn(LocalDate returnDate, String notes) {
        this.returnDate = returnDate;
        this.notes = notes;
        this.status = "COMPLETED";
        
        // Update vehicle availability
        if (this.vehicle != null) {
            this.vehicle.setAvailable(true);
        }
        
        // Recalculate cost if returned early or late
        if (returnDate.isBefore(endDate)) {
            // Early return - calculate based on actual days
            long actualDays = ChronoUnit.DAYS.between(startDate, returnDate) + 1;
            this.totalCost = vehicle.calculateRentalPrice((int)actualDays).doubleValue();
        } else if (returnDate.isAfter(endDate)) {
            // Late return - add late fee
            long extraDays = ChronoUnit.DAYS.between(endDate, returnDate);
            double lateFee = vehicle.calculateRentalPrice((int)extraDays).doubleValue() * 1.2; // 20% extra for late days
            this.totalCost += lateFee;
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        calculateTotalCost();
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        calculateTotalCost();
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    /**
     * Calculate the number of days for this rental
     * @return Number of days between start and end dates (inclusive)
     */
    public long getRentalDays() {
        if (startDate != null && endDate != null) {
            return ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }
        return 0;
    }
    
    /**
     * Check if the rental is currently active
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
    
    /**
     * Check if the rental is completed
     * @return true if completed, false otherwise
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
    
    /**
     * Check if the rental is cancelled
     * @return true if cancelled, false otherwise
     */
    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }
    
    /**
     * Check if the rental is pending
     * @return true if pending, false otherwise
     */
    public boolean isPending() {
        return "PENDING".equals(status);
    }
    
    /**
     * Check if payment is completed
     * @return true if paid, false otherwise
     */
    public boolean isPaid() {
        return "PAID".equals(paymentStatus);
    }
}
