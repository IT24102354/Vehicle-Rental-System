package com.rental.vehicle.repository;

import com.rental.vehicle.model.Truck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Truck entity
 */
@Repository
public interface TruckRepository extends JpaRepository<Truck, Long> {
    // Custom query methods can be added here if needed
}
