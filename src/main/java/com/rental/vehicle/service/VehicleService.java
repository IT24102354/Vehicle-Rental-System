package com.rental.vehicle.service;

import com.rental.vehicle.model.Bike;
import com.rental.vehicle.model.Car;
import com.rental.vehicle.model.Truck;
import com.rental.vehicle.model.Vehicle;
import com.rental.vehicle.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Vehicle Management
 * Implements business logic for CRUD operations on vehicles
 */
@Service
public class VehicleService {
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    /**
     * Get all vehicles
     * @return List of all vehicles
     */
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }
    
    /**
     * Get vehicle by ID
     * @param id Vehicle ID
     * @return Optional containing the vehicle if found
     */
    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }
    
    /**
     * Get vehicle by registration number
     * @param registrationNumber Registration number
     * @return Optional containing the vehicle if found
     */
    public Optional<Vehicle> getVehicleByRegistrationNumber(String registrationNumber) {
        return vehicleRepository.findByRegistrationNumber(registrationNumber);
    }
    
    /**
     * Save a vehicle
     * @param vehicle Vehicle to save
     * @return Saved vehicle
     * @throws IllegalArgumentException if registration number already exists
     */
    public Vehicle saveVehicle(Vehicle vehicle) {
        Optional<Vehicle> existingVehicle = vehicleRepository.findByRegistrationNumber(vehicle.getRegistrationNumber());
        if (existingVehicle.isPresent() && !existingVehicle.get().getId().equals(vehicle.getId())) {
            throw new IllegalArgumentException("Vehicle with registration number " + vehicle.getRegistrationNumber() + " already exists");
        }
        return vehicleRepository.save(vehicle);
    }
    
    /**
     * Update a vehicle
     * @param id Vehicle ID
     * @param updatedVehicle Updated vehicle details
     * @return Updated vehicle
     * @throws IllegalArgumentException if vehicle not found or registration number already exists
     */
    public Vehicle updateVehicle(Long id, Vehicle updatedVehicle) {
        Optional<Vehicle> existingVehicleOpt = vehicleRepository.findById(id);
        if (existingVehicleOpt.isEmpty()) {
            throw new IllegalArgumentException("Vehicle with ID " + id + " not found");
        }
        
        Vehicle existingVehicle = existingVehicleOpt.get();
        
        // Check if registration number is being changed and if it already exists
        if (!existingVehicle.getRegistrationNumber().equals(updatedVehicle.getRegistrationNumber())) {
            Optional<Vehicle> vehicleWithSameReg = vehicleRepository.findByRegistrationNumber(updatedVehicle.getRegistrationNumber());
            if (vehicleWithSameReg.isPresent()) {
                throw new IllegalArgumentException("Vehicle with registration number " + updatedVehicle.getRegistrationNumber() + " already exists");
            }
        }
        
        // Update the vehicle ID
        updatedVehicle.setId(id);
        
        return vehicleRepository.save(updatedVehicle);
    }
    
    /**
     * Delete a vehicle
     * @param id Vehicle ID
     * @throws IllegalArgumentException if vehicle not found
     */
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new IllegalArgumentException("Vehicle with ID " + id + " not found");
        }
        vehicleRepository.deleteById(id);
    }
    
    /**
     * Get all available vehicles
     * @return List of available vehicles
     */
    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findByAvailableTrue();
    }
    
    /**
     * Search vehicles by keyword
     * @param keyword Keyword to search for
     * @return List of vehicles matching the search criteria
     */
    public List<Vehicle> searchVehicles(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllVehicles();
        }
        return vehicleRepository.searchVehicles(keyword);
    }
    
    /**
     * Get vehicles by type
     * @param vehicleType Type of vehicle (Car.class, Bike.class, Truck.class)
     * @return List of vehicles of the specified type
     */
    public List<Vehicle> getVehiclesByType(Class<? extends Vehicle> vehicleType) {
        return vehicleRepository.findByVehicleType(vehicleType);
    }
    
    /**
     * Get all cars
     * @return List of all cars
     */
    public List<Vehicle> getAllCars() {
        return vehicleRepository.findByVehicleType(Car.class);
    }
    
    /**
     * Get all bikes
     * @return List of all bikes
     */
    public List<Vehicle> getAllBikes() {
        return vehicleRepository.findByVehicleType(Bike.class);
    }
    
    /**
     * Get all trucks
     * @return List of all trucks
     */
    public List<Vehicle> getAllTrucks() {
        return vehicleRepository.findByVehicleType(Truck.class);
    }
    
    /**
     * Get vehicles by price range
     * @param minPrice Minimum daily rate
     * @param maxPrice Maximum daily rate
     * @return List of vehicles within the specified price range
     */
    public List<Vehicle> getVehiclesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return vehicleRepository.findByDailyRateBetween(minPrice, maxPrice);
    }
    
    /**
     * Calculate rental price for a vehicle
     * @param vehicleId Vehicle ID
     * @param days Number of rental days
     * @return Calculated rental price
     * @throws IllegalArgumentException if vehicle not found
     */
    public BigDecimal calculateRentalPrice(Long vehicleId, int days) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(vehicleId);
        if (vehicleOpt.isEmpty()) {
            throw new IllegalArgumentException("Vehicle with ID " + vehicleId + " not found");
        }
        
        Vehicle vehicle = vehicleOpt.get();
        return vehicle.calculateRentalPrice(days);
    }
    
    /**
     * Get featured vehicles for the dashboard
     * Shows the latest uploaded vehicles that are available
     * @param limit Maximum number of vehicles to return
     * @return List of latest uploaded vehicles
     */
    public List<Vehicle> getFeaturedVehicles(int limit) {
        // Get available vehicles sorted by ID in descending order (assuming higher ID = newer vehicle)
        // This will give us the most recently added vehicles first
        List<Vehicle> latestVehicles = vehicleRepository.findByAvailableTrueOrderByIdDesc();
        
        // If we have fewer vehicles than the limit, return all available vehicles
        if (latestVehicles.size() <= limit) {
            return latestVehicles;
        }
        
        // Otherwise, return only the requested number of latest vehicles
        return latestVehicles.subList(0, limit);
    }
}
