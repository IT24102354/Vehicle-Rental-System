package com.rental.vehicle.repository;

import com.rental.vehicle.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Vehicle entity
 * Provides CRUD operations and custom queries
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    /**
     * Find vehicle by registration number
     * @param registrationNumber Registration number to search for
     * @return Optional containing the vehicle if found
     */
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);
    
    /**
     * Find all available vehicles
     * @return List of available vehicles
     */
    List<Vehicle> findByAvailableTrue();
    
    /**
     * Find vehicles by make
     * @param make Make to search for
     * @return List of vehicles with the specified make
     */
    List<Vehicle> findByMakeContainingIgnoreCase(String make);
    
    /**
     * Find vehicles by model
     * @param model Model to search for
     * @return List of vehicles with the specified model
     */
    List<Vehicle> findByModelContainingIgnoreCase(String model);
    
    /**
     * Find vehicles by color
     * @param color Color to search for
     * @return List of vehicles with the specified color
     */
    List<Vehicle> findByColorContainingIgnoreCase(String color);
    
    /**
     * Find vehicles by year
     * @param year Year to search for
     * @return List of vehicles with the specified year
     */
    List<Vehicle> findByYear(Integer year);
    
    /**
     * Find vehicles by type (discriminator value)
     * @param vehicleType Type to search for (CAR, BIKE, TRUCK)
     * @return List of vehicles of the specified type
     */
    @Query("SELECT v FROM Vehicle v WHERE TYPE(v) = :vehicleType")
    List<Vehicle> findByVehicleType(@Param("vehicleType") Class<? extends Vehicle> vehicleType);
    
    /**
     * Find vehicles by price range
     * @param minPrice Minimum daily rate
     * @param maxPrice Maximum daily rate
     * @return List of vehicles within the specified price range
     */
    List<Vehicle> findByDailyRateBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * Search vehicles by multiple criteria
     * @param keyword Keyword to search in make, model, or color
     * @return List of vehicles matching the search criteria
     */
    @Query("SELECT v FROM Vehicle v WHERE " +
           "LOWER(v.make) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.model) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.color) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Vehicle> searchVehicles(@Param("keyword") String keyword);
}
