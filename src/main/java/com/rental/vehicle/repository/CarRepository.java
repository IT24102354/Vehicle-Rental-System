package com.rental.vehicle.repository;

import com.rental.vehicle.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Car entity
 */
@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    // Custom query methods can be added here if needed
}
