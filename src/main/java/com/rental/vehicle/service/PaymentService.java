package com.rental.vehicle.service;

import com.rental.vehicle.model.Payment;
import com.rental.vehicle.model.Rental;
import com.rental.vehicle.model.User;
import com.rental.vehicle.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Payment management
 * Handles business logic related to payments
 */
@Service
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    /**
     * Create a new payment for a rental
     * @param rental the rental to pay for
     * @param paymentMethod the payment method
     * @return the created payment
     */
    public Payment createPayment(Rental rental, String paymentMethod) {
        Payment payment = new Payment(rental, BigDecimal.valueOf(rental.getTotalCost()), paymentMethod);
        payment.setPaymentDate(LocalDateTime.now());
        return paymentRepository.save(payment);
    }
    
    /**
     * Create a payment with card details
     * @param rental the rental to pay for
     * @param paymentMethod the payment method
     * @param cardLast4Digits last 4 digits of the card
     * @return the created payment
     */
    public Payment createCardPayment(Rental rental, String paymentMethod, String cardLast4Digits) {
        Payment payment = createPayment(rental, paymentMethod);
        payment.setCardLast4Digits(cardLast4Digits);
        return paymentRepository.save(payment);
    }
    
    /**
     * Process a payment
     * @param payment the payment to process
     * @return true if payment is successful, false otherwise
     */
    public boolean processPayment(Payment payment) {
        boolean result = payment.processPayment();
        paymentRepository.save(payment);
        return result;
    }
    
    /**
     * Refund a payment
     * @param id payment id
     * @param reason reason for refund
     * @return true if refund is successful, false otherwise
     */
    public boolean refundPayment(Long id, String reason) {
        Optional<Payment> paymentOpt = paymentRepository.findById(id);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            boolean result = payment.refundPayment(reason);
            if (result) {
                paymentRepository.save(payment);
            }
            return result;
        }
        return false;
    }
    
    /**
     * Get all payments
     * @return list of all payments
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
    /**
     * Get payment by id
     * @param id payment id
     * @return optional payment
     */
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }
    
    /**
     * Get payments by user
     * @param user the user who made the payments
     * @return list of payments made by the user
     */
    public List<Payment> getPaymentsByUser(User user) {
        return paymentRepository.findByRentalUser(user);
    }
    
    /**
     * Get payments by rental
     * @param rental the rental associated with the payments
     * @return list of payments for the rental
     */
    public List<Payment> getPaymentsByRental(Rental rental) {
        return paymentRepository.findByRental(rental);
    }
    
    /**
     * Get payments by status
     * @param status the payment status
     * @return list of payments with the specified status
     */
    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }
    
    /**
     * Delete payment by id
     * @param id payment id
     */
    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }
}
