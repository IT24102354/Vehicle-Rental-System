package com.rental.vehicle.repository;

import com.rental.vehicle.model.Rental;
import com.rental.vehicle.model.User;
import com.rental.vehicle.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Rental entity
 * Provides CRUD operations and custom queries for Rental Management
 */
@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    
    // Find rentals by user
    List<Rental> findByUser(User user);
    
    // Find rentals by vehicle
    List<Rental> findByVehicle(Vehicle vehicle);
    
    // Find active rentals (status = "ACTIVE")
    List<Rental> findByStatus(String status);
    
    // Find rentals by user and status
    List<Rental> findByUserAndStatus(User user, String status);
    
    // Find rentals by vehicle and status
    List<Rental> findByVehicleAndStatus(Vehicle vehicle, String status);
    
    // Find rentals that overlap with a date range
    @Query("SELECT r FROM Rental r WHERE r.vehicle = ?1 AND r.status NOT IN ('CANCELLED', 'COMPLETED') " +
           "AND ((r.startDate BETWEEN ?2 AND ?3) OR (r.endDate BETWEEN ?2 AND ?3) OR " +
           "(?2 BETWEEN r.startDate AND r.endDate) OR (?3 BETWEEN r.startDate AND r.endDate))")
    List<Rental> findOverlappingRentals(Vehicle vehicle, LocalDate startDate, LocalDate endDate);
    
    // Check if a vehicle is available for a specific date range
    default boolean isVehicleAvailable(Vehicle vehicle, LocalDate startDate, LocalDate endDate) {
        return findOverlappingRentals(vehicle, startDate, endDate).isEmpty();
    }
    
    // Find upcoming rentals (status = "PENDING" and start date in the future)
    @Query("SELECT r FROM Rental r WHERE r.status = 'PENDING' AND r.startDate >= ?1")
    List<Rental> findUpcomingRentals(LocalDate currentDate);
    
    // Find rentals that should be active today (status = "PENDING" and start date <= today <= end date)
    @Query("SELECT r FROM Rental r WHERE r.status = 'PENDING' AND r.startDate <= ?1 AND r.endDate >= ?1")
    List<Rental> findRentalsToActivateToday(LocalDate currentDate);
    
    // Find rentals that should be completed today (status = "ACTIVE" and end date = today)
    @Query("SELECT r FROM Rental r WHERE r.status = 'ACTIVE' AND r.endDate = ?1")
    List<Rental> findRentalsToCompleteToday(LocalDate currentDate);
    
    // Find overdue rentals (status = "ACTIVE" and end date < today)
    @Query("SELECT r FROM Rental r WHERE r.status = 'ACTIVE' AND r.endDate < ?1")
    List<Rental> findOverdueRentals(LocalDate currentDate);
}
