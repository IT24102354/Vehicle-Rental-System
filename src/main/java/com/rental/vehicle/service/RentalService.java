package com.rental.vehicle.service;

import com.rental.vehicle.model.Rental;
import com.rental.vehicle.model.User;
import com.rental.vehicle.model.Vehicle;
import com.rental.vehicle.repository.RentalRepository;
import com.rental.vehicle.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Rental Management
 * Handles business logic for rental operations
 */
@Service
public class RentalService {
    
    @Autowired
    private RentalRepository rentalRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    /**
     * Create a new rental booking
     * @param rental Rental to be created
     * @return Created rental
     */
    @Transactional
    public Rental createRental(Rental rental) {
        // Check if the vehicle is available for the requested dates
        if (!isVehicleAvailable(rental.getVehicle(), rental.getStartDate(), rental.getEndDate())) {
            throw new RuntimeException("Vehicle is not available for the selected dates");
        }
        
        // Always set status to PENDING regardless of start date, so admin approval is required
        rental.setStatus("PENDING");
        
        // Note: The vehicle will remain available until the admin approves the rental
        
        // Calculate the total cost
        rental.calculateTotalCost();
        
        // Save the rental
        return rentalRepository.save(rental);
    }
    
    /**
     * Get a rental by ID
     * @param id Rental ID
     * @return Rental if found
     */
    public Rental getRentalById(Long id) {
        Optional<Rental> rental = rentalRepository.findById(id);
        if (rental.isPresent()) {
            return rental.get();
        }
        throw new RuntimeException("Rental not found with ID: " + id);
    }
    
    /**
     * Get all rentals
     * @return List of all rentals
     */
    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }
    
    /**
     * Get rentals by user
     * @param user User
     * @return List of rentals for the user
     */
    public List<Rental> getRentalsByUser(User user) {
        return rentalRepository.findByUser(user);
    }
    
    /**
     * Get active rentals by user
     * @param user User
     * @return List of active rentals for the user
     */
    public List<Rental> getActiveRentalsByUser(User user) {
        return rentalRepository.findByUserAndStatus(user, "ACTIVE");
    }
    
    /**
     * Get completed rentals by user
     * @param user User
     * @return List of completed rentals for the user
     */
    public List<Rental> getCompletedRentalsByUser(User user) {
        return rentalRepository.findByUserAndStatus(user, "COMPLETED");
    }
    
    /**
     * Get pending rentals by user
     * @param user User
     * @return List of pending rentals for the user
     */
    public List<Rental> getPendingRentalsByUser(User user) {
        return rentalRepository.findByUserAndStatus(user, "PENDING");
    }
    
    /**
     * Get active rentals
     * @return List of active rentals
     */
    public List<Rental> getActiveRentals() {
        return rentalRepository.findByStatus("ACTIVE");
    }
    
    /**
     * Get pending rentals
     * @return List of pending rentals
     */
    public List<Rental> getPendingRentals() {
        return rentalRepository.findByStatus("PENDING");
    }
    
    /**
     * Get completed rentals
     * @return List of completed rentals
     */
    public List<Rental> getCompletedRentals() {
        return rentalRepository.findByStatus("COMPLETED");
    }
    
    /**
     * Update a rental
     * @param id Rental ID
     * @param rentalDetails Updated rental details
     * @return Updated rental
     */
    @Transactional
    public Rental updateRental(Long id, Rental rentalDetails) {
        Rental rental = getRentalById(id);
        
        // Check if dates have changed and if the vehicle is available for the new dates
        if ((rentalDetails.getStartDate() != null && !rentalDetails.getStartDate().equals(rental.getStartDate())) ||
            (rentalDetails.getEndDate() != null && !rentalDetails.getEndDate().equals(rental.getEndDate()))) {
            
            LocalDate startDate = rentalDetails.getStartDate() != null ? rentalDetails.getStartDate() : rental.getStartDate();
            LocalDate endDate = rentalDetails.getEndDate() != null ? rentalDetails.getEndDate() : rental.getEndDate();
            
            // Check if the vehicle is available for the new dates (excluding the current rental)
            List<Rental> overlappingRentals = rentalRepository.findOverlappingRentals(rental.getVehicle(), startDate, endDate);
            overlappingRentals.removeIf(r -> r.getId().equals(id)); // Remove the current rental
            
            if (!overlappingRentals.isEmpty()) {
                throw new RuntimeException("Vehicle is not available for the selected dates");
            }
            
            rental.setStartDate(startDate);
            rental.setEndDate(endDate);
            rental.calculateTotalCost();
        }
        
        // Update other fields if provided
        if (rentalDetails.getStatus() != null) {
            rental.setStatus(rentalDetails.getStatus());
        }
        
        if (rentalDetails.getPaymentStatus() != null) {
            rental.setPaymentStatus(rentalDetails.getPaymentStatus());
        }
        
        if (rentalDetails.getNotes() != null) {
            rental.setNotes(rentalDetails.getNotes());
        }
        
        // If status changed to ACTIVE, mark the vehicle as unavailable
        if ("ACTIVE".equals(rental.getStatus()) && rental.getVehicle().getAvailable()) {
            Vehicle vehicle = rental.getVehicle();
            vehicle.setAvailable(false);
            vehicleRepository.save(vehicle);
        }
        
        // If status changed to COMPLETED, mark the vehicle as available
        if ("COMPLETED".equals(rental.getStatus()) || "CANCELLED".equals(rental.getStatus())) {
            Vehicle vehicle = rental.getVehicle();
            vehicle.setAvailable(true);
            vehicleRepository.save(vehicle);
        }
        
        return rentalRepository.save(rental);
    }
    
    /**
     * Process the return of a vehicle
     * @param id Rental ID
     * @param returnDate Return date
     * @param notes Return notes
     * @return Updated rental
     */
    @Transactional
    public Rental processReturn(Long id, LocalDate returnDate, String notes) {
        Rental rental = getRentalById(id);
        
        // Process the return
        rental.processReturn(returnDate, notes);
        
        // Mark the vehicle as available
        Vehicle vehicle = rental.getVehicle();
        vehicle.setAvailable(true);
        vehicleRepository.save(vehicle);
        
        return rentalRepository.save(rental);
    }
    
    /**
     * Cancel a rental
     * @param id Rental ID
     * @return Cancelled rental
     */
    @Transactional
    public Rental cancelRental(Long id) {
        Rental rental = getRentalById(id);
        
        // Only pending rentals can be cancelled
        if (!"PENDING".equals(rental.getStatus())) {
            throw new RuntimeException("Only pending rentals can be cancelled");
        }
        
        rental.setStatus("CANCELLED");
        
        return rentalRepository.save(rental);
    }
    
    /**
     * Delete a rental
     * @param id Rental ID
     */
    @Transactional
    public void deleteRental(Long id) {
        Rental rental = getRentalById(id);
        
        // Only completed or cancelled rentals can be deleted
        if (!"COMPLETED".equals(rental.getStatus()) && !"CANCELLED".equals(rental.getStatus()) && !"PENDING".equals(rental.getStatus())) {
            throw new RuntimeException("Only completed, cancelled, or pending rentals can be deleted");
        }
        
        rentalRepository.deleteById(id);
    }
    
    /**
     * Update a rental with specific fields for editing
     * @param id Rental ID
     * @param startDate New start date
     * @param endDate New end date
     * @param notes New notes
     * @param totalCost New total cost
     * @return Updated rental
     */
    @Transactional
    public Rental updateRental(Long id, LocalDate startDate, LocalDate endDate, String notes, double totalCost) {
        Rental rental = getRentalById(id);
        
        // Only pending rentals can be edited
        if (!"PENDING".equals(rental.getStatus())) {
            throw new RuntimeException("Only pending rentals can be edited");
        }
        
        // Check if the vehicle is available for the new dates (excluding the current rental)
        List<Rental> overlappingRentals = rentalRepository.findOverlappingRentals(rental.getVehicle(), startDate, endDate);
        overlappingRentals.removeIf(r -> r.getId().equals(id)); // Remove the current rental
        
        if (!overlappingRentals.isEmpty()) {
            throw new RuntimeException("Vehicle is not available for the selected dates");
        }
        
        // Update the rental
        rental.setStartDate(startDate);
        rental.setEndDate(endDate);
        rental.setNotes(notes);
        rental.setTotalCost(totalCost);
        
        return rentalRepository.save(rental);
    }
    
    /**
     * Check if a vehicle is available for the requested dates
     * @param vehicle Vehicle
     * @param startDate Start date
     * @param endDate End date
     * @return true if available, false otherwise
     */
    public boolean isVehicleAvailable(Vehicle vehicle, LocalDate startDate, LocalDate endDate) {
        return rentalRepository.isVehicleAvailable(vehicle, startDate, endDate);
    }
    
    /**
     * Get upcoming rentals
     * @return List of upcoming rentals
     */
    public List<Rental> getUpcomingRentals() {
        return rentalRepository.findUpcomingRentals(LocalDate.now());
    }
    
    /**
     * Get rentals that should be activated today
     * @return List of rentals to activate
     */
    public List<Rental> getRentalsToActivateToday() {
        return rentalRepository.findRentalsToActivateToday(LocalDate.now());
    }
    
    /**
     * Get rentals that should be completed today
     * @return List of rentals to complete
     */
    public List<Rental> getRentalsToCompleteToday() {
        return rentalRepository.findRentalsToCompleteToday(LocalDate.now());
    }
    
    /**
     * Get overdue rentals
     * @return List of overdue rentals
     */
    public List<Rental> getOverdueRentals() {
        return rentalRepository.findOverdueRentals(LocalDate.now());
    }
    
    /**
     * Process daily rental updates
     * This method should be scheduled to run daily
     */
    @Transactional
    public void processDailyRentalUpdates() {
        LocalDate today = LocalDate.now();
        
        // Activate rentals that start today
        List<Rental> rentalsToActivate = rentalRepository.findRentalsToActivateToday(today);
        for (Rental rental : rentalsToActivate) {
            activateRental(rental);
        }
        
        // Mark rentals as completed if they end today
        List<Rental> rentalsToComplete = rentalRepository.findRentalsToCompleteToday(today);
        for (Rental rental : rentalsToComplete) {
            rental.setStatus("COMPLETED");
            
            // Mark the vehicle as available
            Vehicle vehicle = rental.getVehicle();
            vehicle.setAvailable(true);
            vehicleRepository.save(vehicle);
            
            rentalRepository.save(rental);
        }
    }
    
    /**
     * Manually activate a pending rental
     * @param rentalId Rental ID to activate
     * @return The activated rental
     */
    @Transactional
    public Rental activateRentalById(Long rentalId) {
        Rental rental = getRentalById(rentalId);
        
        // Check if the rental is in pending status
        if (!"PENDING".equals(rental.getStatus())) {
            throw new RuntimeException("Only pending rentals can be activated");
        }
        
        return activateRental(rental);
    }
    
    /**
     * Helper method to activate a rental
     * @param rental Rental to activate
     * @return The activated rental
     */
    @Transactional
    private Rental activateRental(Rental rental) {
        rental.setStatus("ACTIVE");
        
        // Mark the vehicle as unavailable
        Vehicle vehicle = rental.getVehicle();
        vehicle.setAvailable(false);
        vehicleRepository.save(vehicle);
        
        return rentalRepository.save(rental);
    }
}
