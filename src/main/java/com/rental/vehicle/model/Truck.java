package com.rental.vehicle.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Truck entity class that inherits from Vehicle
 * Implements specific truck attributes and pricing model
 */
@Entity
@Table(name = "trucks")
@PrimaryKeyJoinColumn(name = "vehicle_id")
@DiscriminatorValue("TRUCK")
public class Truck extends Vehicle {
    
    @NotNull(message = "Payload capacity is required")
    @Min(value = 0, message = "Payload capacity must be positive")
    private Double payloadCapacity; // in tons
    
    @NotNull(message = "Volume capacity is required")
    @Min(value = 0, message = "Volume capacity must be positive")
    private Double volumeCapacity; // in cubic meters
    
    private String truckType; // Pickup, Van, Box, etc.
    
    private Boolean hasTailLift;
    
    private Boolean hasRefrigeration;
    
    @NotNull(message = "Number of axles is required")
    @Min(value = 2, message = "Number of axles must be at least 2")
    private Integer numberOfAxles;
    
    private String licenseRequirement; // B, C1, C, etc.
    
    // Default constructor
    public Truck() {
        super();
    }
    
    // Constructor with parameters
    public Truck(String registrationNumber, String make, String model, Integer year, 
                String color, BigDecimal dailyRate, Boolean available, String imageUrl, 
                String description, LocalDate lastMaintenanceDate, Integer mileage, 
                String fuelType, String transmissionType, Double payloadCapacity, 
                Double volumeCapacity, String truckType, Boolean hasTailLift, 
                Boolean hasRefrigeration, Integer numberOfAxles, String licenseRequirement) {
        super(registrationNumber, make, model, year, color, dailyRate, available, 
             imageUrl, description, lastMaintenanceDate, mileage, fuelType, transmissionType);
        this.payloadCapacity = payloadCapacity;
        this.volumeCapacity = volumeCapacity;
        this.truckType = truckType;
        this.hasTailLift = hasTailLift;
        this.hasRefrigeration = hasRefrigeration;
        this.numberOfAxles = numberOfAxles;
        this.licenseRequirement = licenseRequirement;
    }
    
    // Basic constructor with minimal parameters
    public Truck(String registrationNumber, String make, String model, Integer year, 
                String color, BigDecimal dailyRate, Double payloadCapacity, 
                Double volumeCapacity, Integer numberOfAxles) {
        super(registrationNumber, make, model, year, color, dailyRate);
        this.payloadCapacity = payloadCapacity;
        this.volumeCapacity = volumeCapacity;
        this.numberOfAxles = numberOfAxles;
    }
    
    // Getters and Setters
    public Double getPayloadCapacity() {
        return payloadCapacity;
    }
    
    public void setPayloadCapacity(Double payloadCapacity) {
        this.payloadCapacity = payloadCapacity;
    }
    
    public Double getVolumeCapacity() {
        return volumeCapacity;
    }
    
    public void setVolumeCapacity(Double volumeCapacity) {
        this.volumeCapacity = volumeCapacity;
    }
    
    public String getTruckType() {
        return truckType;
    }
    
    public void setTruckType(String truckType) {
        this.truckType = truckType;
    }
    
    public Boolean getHasTailLift() {
        return hasTailLift;
    }
    
    public void setHasTailLift(Boolean hasTailLift) {
        this.hasTailLift = hasTailLift;
    }
    
    public Boolean getHasRefrigeration() {
        return hasRefrigeration;
    }
    
    public void setHasRefrigeration(Boolean hasRefrigeration) {
        this.hasRefrigeration = hasRefrigeration;
    }
    
    public Integer getNumberOfAxles() {
        return numberOfAxles;
    }
    
    public void setNumberOfAxles(Integer numberOfAxles) {
        this.numberOfAxles = numberOfAxles;
    }
    
    public String getLicenseRequirement() {
        return licenseRequirement;
    }
    
    public void setLicenseRequirement(String licenseRequirement) {
        this.licenseRequirement = licenseRequirement;
    }
    
    /**
     * Implements polymorphic pricing model for trucks
     * Trucks with higher payload capacity and special features get a higher rate
     */
    @Override
    public BigDecimal calculateRentalPrice(int days) {
        BigDecimal basePrice = getDailyRate().multiply(BigDecimal.valueOf(days));
        
        // Apply premium based on payload capacity
        if (payloadCapacity > 10.0) {
            basePrice = basePrice.multiply(BigDecimal.valueOf(1.3)); // 30% premium
        } else if (payloadCapacity > 5.0) {
            basePrice = basePrice.multiply(BigDecimal.valueOf(1.15)); // 15% premium
        }
        
        // Additional premium for refrigerated trucks
        if (hasRefrigeration != null && hasRefrigeration) {
            basePrice = basePrice.multiply(BigDecimal.valueOf(1.1)); // 10% premium
        }
        
        return basePrice;
    }
}
