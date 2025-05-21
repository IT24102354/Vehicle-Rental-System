package com.rental.vehicle.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Car entity class that inherits from Vehicle
 * Implements specific car attributes and pricing model
 */
@Entity
@Table(name = "cars")
@PrimaryKeyJoinColumn(name = "vehicle_id")
@DiscriminatorValue("CAR")
public class Car extends Vehicle {
    
    @NotNull(message = "Number of doors is required")
    @Min(value = 2, message = "Number of doors must be at least 2")
    private Integer numberOfDoors;
    
    @NotNull(message = "Number of seats is required")
    @Min(value = 1, message = "Number of seats must be at least 1")
    private Integer numberOfSeats;
    
    private Boolean hasAirConditioning;
    
    private Boolean hasNavigation;
    
    private String carType; // Sedan, SUV, Hatchback, etc.
    
    private Boolean hasAutomaticTransmission;
    
    // Default constructor
    public Car() {
        super();
    }
    
    // Constructor with parameters
    public Car(String registrationNumber, String make, String model, Integer year, 
              String color, BigDecimal dailyRate, Boolean available, String imageUrl, 
              String description, LocalDate lastMaintenanceDate, Integer mileage, 
              String fuelType, String transmissionType, Integer numberOfDoors, 
              Integer numberOfSeats, Boolean hasAirConditioning, Boolean hasNavigation, 
              String carType, Boolean hasAutomaticTransmission) {
        super(registrationNumber, make, model, year, color, dailyRate, available, 
             imageUrl, description, lastMaintenanceDate, mileage, fuelType, transmissionType);
        this.numberOfDoors = numberOfDoors;
        this.numberOfSeats = numberOfSeats;
        this.hasAirConditioning = hasAirConditioning;
        this.hasNavigation = hasNavigation;
        this.carType = carType;
        this.hasAutomaticTransmission = hasAutomaticTransmission;
    }
    
    // Basic constructor with minimal parameters
    public Car(String registrationNumber, String make, String model, Integer year, 
              String color, BigDecimal dailyRate, Integer numberOfDoors, Integer numberOfSeats) {
        super(registrationNumber, make, model, year, color, dailyRate);
        this.numberOfDoors = numberOfDoors;
        this.numberOfSeats = numberOfSeats;
    }
    
    // Getters and Setters
    public Integer getNumberOfDoors() {
        return numberOfDoors;
    }
    
    public void setNumberOfDoors(Integer numberOfDoors) {
        this.numberOfDoors = numberOfDoors;
    }
    
    public Integer getNumberOfSeats() {
        return numberOfSeats;
    }
    
    public void setNumberOfSeats(Integer numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }
    
    public Boolean getHasAirConditioning() {
        return hasAirConditioning;
    }
    
    public void setHasAirConditioning(Boolean hasAirConditioning) {
        this.hasAirConditioning = hasAirConditioning;
    }
    
    public Boolean getHasNavigation() {
        return hasNavigation;
    }
    
    public void setHasNavigation(Boolean hasNavigation) {
        this.hasNavigation = hasNavigation;
    }
    
    public String getCarType() {
        return carType;
    }
    
    public void setCarType(String carType) {
        this.carType = carType;
    }
    
    public Boolean getHasAutomaticTransmission() {
        return hasAutomaticTransmission;
    }
    
    public void setHasAutomaticTransmission(Boolean hasAutomaticTransmission) {
        this.hasAutomaticTransmission = hasAutomaticTransmission;
    }
    
    /**
     * Implements polymorphic pricing model for cars
     * Cars with premium features get a higher rate
     */
    @Override
    public BigDecimal calculateRentalPrice(int days) {
        BigDecimal basePrice = getDailyRate().multiply(BigDecimal.valueOf(days));
        
        // Apply premium for cars with navigation and air conditioning
        if (hasNavigation != null && hasNavigation && 
            hasAirConditioning != null && hasAirConditioning) {
            return basePrice.multiply(BigDecimal.valueOf(1.15)); // 15% premium
        }
        
        return basePrice;
    }
}
