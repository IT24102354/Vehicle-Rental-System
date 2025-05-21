package com.rental.vehicle.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity class for storing payment information
 * Implements encapsulation by storing payment details
 */
@Entity
@Table(name = "payments")
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "rental_id", nullable = false)
    @NotNull(message = "Rental is required")
    private Rental rental;
    
    @NotNull(message = "Payment amount is required")
    @Positive(message = "Payment amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Payment date is required")
    private LocalDateTime paymentDate;
    
    @NotNull(message = "Payment method is required")
    private String paymentMethod; // CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER
    
    private String transactionReference;
    
    private String status; // SUCCESS, FAILED, PENDING, REFUNDED
    
    // For credit card payments
    private String cardLast4Digits;
    
    // Constructor with required fields
    public Payment(Rental rental, BigDecimal amount, String paymentMethod) {
        this.rental = rental;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = LocalDateTime.now();
        this.status = "PENDING";
    }
    
    // Default constructor
    public Payment() {
    }
    
    /**
     * Process the payment
     * Demonstrates polymorphic behavior with different payment methods
     * @return true if payment is successful, false otherwise
     */
    public boolean processPayment() {
        // In a real application, this would integrate with a payment gateway
        // For demonstration, we'll simulate different payment behaviors
        
        switch (paymentMethod) {
            case "CREDIT_CARD":
            case "DEBIT_CARD":
                // Simulate card processing
                if (cardLast4Digits != null && !cardLast4Digits.isEmpty()) {
                    this.status = "SUCCESS";
                    return true;
                }
                break;
            case "CASH":
                // Cash payments are immediately successful
                this.status = "SUCCESS";
                return true;
            case "BANK_TRANSFER":
                // Bank transfers are pending until confirmed
                this.status = "PENDING";
                return true;
        }
        
        this.status = "FAILED";
        return false;
    }
    
    /**
     * Calculate any applicable discount
     * @return the discount amount
     */
    public BigDecimal calculateDiscount() {
        BigDecimal discount = BigDecimal.ZERO;
        
        // Early payment discount (if paid before rental starts)
        if (paymentDate.isBefore(rental.getStartDate().atStartOfDay())) {
            discount = amount.multiply(new BigDecimal("0.05")); // 5% discount
        }
        
        return discount;
    }
    
    /**
     * Refund the payment
     * @param reason reason for refund
     * @return true if refund is successful, false otherwise
     */
    public boolean refundPayment(String reason) {
        if ("SUCCESS".equals(status)) {
            this.status = "REFUNDED";
            return true;
        }
        return false;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Rental getRental() {
        return rental;
    }

    public void setRental(Rental rental) {
        this.rental = rental;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCardLast4Digits() {
        return cardLast4Digits;
    }

    public void setCardLast4Digits(String cardLast4Digits) {
        this.cardLast4Digits = cardLast4Digits;
    }
    
    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", amount=" + amount +
                ", paymentDate=" + paymentDate +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
