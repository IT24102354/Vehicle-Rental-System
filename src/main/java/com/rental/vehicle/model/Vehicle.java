package com.rental.vehicle.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Base Vehicle entity class for storing vehicle information
 * Implements encapsulation by storing vehicle attributes
 * Parent class for inheritance hierarchy (Car, Bike, Truck)
 */
@Entity
@Table(name = "vehicles")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "vehicle_type")
public abstract class Vehicle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Registration number is required")
    @Column(unique = true)
    private String registrationNumber;
    
    @NotBlank(message = "Make is required")
    private String make;
    
    @NotBlank(message = "Model is required")
    private String model;
    
    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be at least 1900")
    @Column(name = "model_year") // Using a different column name to avoid reserved keyword
    private Integer year;
    
    @NotBlank(message = "Color is required")
    private String color;
    
    @NotNull(message = "Daily rate is required")
    @Min(value = 0, message = "Daily rate must be positive")
    private BigDecimal dailyRate;
    
    @NotNull(message = "Available status is required")
    private Boolean available = true;
    
    private String imageUrl;
    
    private String description;
    
    private LocalDate lastMaintenanceDate;
    
    private Integer mileage;
    
    private String fuelType;
    
    private String transmissionType;
    
    // Abstract method for polymorphic behavior
    public abstract BigDecimal calculateRentalPrice(int days);
    
    // Default constructor
    public Vehicle() {
    }
    
    // Constructor with parameters
    public Vehicle(String registrationNumber, String make, String model, Integer year, 
                  String color, BigDecimal dailyRate, Boolean available, String imageUrl, 
                  String description, LocalDate lastMaintenanceDate, Integer mileage, 
                  String fuelType, String transmissionType) {
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.year = year;
        this.color = color;
        this.dailyRate = dailyRate;
        this.available = available;
        this.imageUrl = imageUrl;
        this.description = description;
        this.lastMaintenanceDate = lastMaintenanceDate;
        this.mileage = mileage;
        this.fuelType = fuelType;
        this.transmissionType = transmissionType;
    }
    
    // Basic constructor with minimal parameters
    public Vehicle(String registrationNumber, String make, String model, Integer year, 
                  String color, BigDecimal dailyRate) {
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.year = year;
        this.color = color;
        this.dailyRate = dailyRate;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getRegistrationNumber() {
        return registrationNumber;
    }
    
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }
    
    public String getMake() {
        return make;
    }
    
    public void setMake(String make) {
        this.make = make;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public BigDecimal getDailyRate() {
        return dailyRate;
    }
    
    public void setDailyRate(BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
    }
    
    public Boolean getAvailable() {
        return available;
    }
    
    public void setAvailable(Boolean available) {
        this.available = available;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDate getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }
    
    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }
    
    public Integer getMileage() {
        return mileage;
    }
    
    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }
    
    public String getFuelType() {
        return fuelType;
    }
    
    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }
    
    public String getTransmissionType() {
        return transmissionType;
    }
    
    public void setTransmissionType(String transmissionType) {
        this.transmissionType = transmissionType;
    }
    
    @Override
    public String toString() {
        return make + " " + model + " (" + year + ")";
    }
    
    /**
     * Returns the type of vehicle (Car, Bike, Truck) based on the discriminator value
     * This is used in templates to display the vehicle type
     * @return String representing the vehicle type
     */
    public String getVehicleType() {
        // This method will be used instead of getClass().getSimpleName() in templates
        String className = this.getClass().getSimpleName();
        return className != null ? className : "Vehicle";
    }
}
