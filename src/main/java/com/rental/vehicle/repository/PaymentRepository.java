package com.rental.vehicle.repository;

import com.rental.vehicle.model.Payment;
import com.rental.vehicle.model.Rental;
import com.rental.vehicle.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Payment entity
 * Handles database operations for payments
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Find payments by rental
     * @param rental the rental associated with the payments
     * @return list of payments for the rental
     */
    List<Payment> findByRental(Rental rental);
    
    /**
     * Find payments by rental's user
     * @param user the user who made the rentals
     * @return list of payments made by the user
     */
    List<Payment> findByRentalUser(User user);
    
    /**
     * Find payments by status
     * @param status the payment status
     * @return list of payments with the specified status
     */
    List<Payment> findByStatus(String status);
    
    /**
     * Find payments by payment method
     * @param paymentMethod the payment method
     * @return list of payments made with the specified method
     */
    List<Payment> findByPaymentMethod(String paymentMethod);
}
