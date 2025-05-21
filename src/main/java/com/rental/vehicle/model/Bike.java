package com.rental.vehicle.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Bike entity class that inherits from Vehicle
 * Implements specific bike attributes and pricing model
 */
@Entity
@Table(name = "bikes")
@PrimaryKeyJoinColumn(name = "vehicle_id")
@DiscriminatorValue("BIKE")
public class Bike extends Vehicle {
    
    @NotNull(message = "Engine capacity is required")
    @Min(value = 0, message = "Engine capacity must be positive")
    private Integer engineCapacity; // in cc
    
    private String bikeType; // Sport, Cruiser, Touring, etc.
    
    private Boolean hasABS;
    
    private Boolean hasBluetooth;
    
    private String licenseRequirement; // A1, A2, A, etc.
    
    // Default constructor
    public Bike() {
        super();
    }
    
    // Constructor with parameters
    public Bike(String registrationNumber, String make, String model, Integer year, 
               String color, BigDecimal dailyRate, Boolean available, String imageUrl, 
               String description, LocalDate lastMaintenanceDate, Integer mileage, 
               String fuelType, String transmissionType, Integer engineCapacity, 
               String bikeType, Boolean hasABS, Boolean hasBluetooth, String licenseRequirement) {
        super(registrationNumber, make, model, year, color, dailyRate, available, 
             imageUrl, description, lastMaintenanceDate, mileage, fuelType, transmissionType);
        this.engineCapacity = engineCapacity;
        this.bikeType = bikeType;
        this.hasABS = hasABS;
        this.hasBluetooth = hasBluetooth;
        this.licenseRequirement = licenseRequirement;
    }
    
    // Basic constructor with minimal parameters
    public Bike(String registrationNumber, String make, String model, Integer year, 
               String color, BigDecimal dailyRate, Integer engineCapacity) {
        super(registrationNumber, make, model, year, color, dailyRate);
        this.engineCapacity = engineCapacity;
    }
    
    // Getters and Setters
    public Integer getEngineCapacity() {
        return engineCapacity;
    }
    
    public void setEngineCapacity(Integer engineCapacity) {
        this.engineCapacity = engineCapacity;
    }
    
    public String getBikeType() {
        return bikeType;
    }
    
    public void setBikeType(String bikeType) {
        this.bikeType = bikeType;
    }
    
    public Boolean getHasABS() {
        return hasABS;
    }
    
    public void setHasABS(Boolean hasABS) {
        this.hasABS = hasABS;
    }
    
    public Boolean getHasBluetooth() {
        return hasBluetooth;
    }
    
    public void setHasBluetooth(Boolean hasBluetooth) {
        this.hasBluetooth = hasBluetooth;
    }
    
    public String getLicenseRequirement() {
        return licenseRequirement;
    }
    
    public void setLicenseRequirement(String licenseRequirement) {
        this.licenseRequirement = licenseRequirement;
    }
    
    /**
     * Implements polymorphic pricing model for bikes
     * Higher engine capacity bikes get a higher rate
     */
    @Override
    public BigDecimal calculateRentalPrice(int days) {
        BigDecimal basePrice = getDailyRate().multiply(BigDecimal.valueOf(days));
        
        // Apply premium for high-capacity bikes
        if (engineCapacity > 500) {
            return basePrice.multiply(BigDecimal.valueOf(1.2)); // 20% premium
        } else if (engineCapacity > 250) {
            return basePrice.multiply(BigDecimal.valueOf(1.1)); // 10% premium
        }
        
        return basePrice;
    }
}
