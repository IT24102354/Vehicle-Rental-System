package com.rental.vehicle.repository;

import com.rental.vehicle.model.Bike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Bike entity
 */
@Repository
public interface BikeRepository extends JpaRepository<Bike, Long> {
    // Custom query methods can be added here if needed
}
