package com.rental.vehicle.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Invoice entity class for storing invoice information
 * Implements encapsulation by storing invoice details
 */
@Entity
@Table(name = "invoices")
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String invoiceNumber;
    
    @ManyToOne
    @JoinColumn(name = "rental_id", nullable = false)
    @NotNull(message = "Rental is required")
    private Rental rental;
    
    @OneToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;
    
    @NotNull(message = "Invoice date is required")
    private LocalDateTime invoiceDate;
    
    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;
    
    @NotNull(message = "Subtotal is required")
    @Positive(message = "Subtotal must be positive")
    private BigDecimal subtotal;
    
    private BigDecimal taxAmount;
    
    private BigDecimal discountAmount;
    
    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private BigDecimal totalAmount;
    
    private String status; // PENDING, PAID, OVERDUE, CANCELLED
    
    private String notes;
    
    // Constructor with required fields
    public Invoice(Rental rental) {
        this.rental = rental;
        this.invoiceNumber = generateInvoiceNumber();
        this.invoiceDate = LocalDateTime.now();
        this.dueDate = rental.getStartDate().atStartOfDay();
        this.status = "PENDING";
        calculateAmounts();
    }
    
    // Default constructor required by JPA
    public Invoice() {
    }
    
    /**
     * Generate a unique invoice number
     * @return a formatted invoice number
     */
    private String generateInvoiceNumber() {
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "INV-" + uuid;
    }
    
    /**
     * Calculate invoice amounts (subtotal, tax, discount, total)
     * Demonstrates encapsulation and data hiding
     */
    public void calculateAmounts() {
        if (rental != null) {
            // Subtotal is the rental cost
            this.subtotal = BigDecimal.valueOf(rental.getTotalCost());
            
            // Apply tax (10% for example)
            this.taxAmount = subtotal.multiply(new BigDecimal("0.10"));
            
            // No discount by default
            this.discountAmount = BigDecimal.ZERO;
            
            // Calculate total
            this.totalAmount = subtotal.add(taxAmount).subtract(discountAmount);
        }
    }
    
    /**
     * Apply a discount to the invoice
     * @param discountPercent discount percentage as a decimal (e.g., 0.10 for 10%)
     * @param reason reason for the discount
     */
    public void applyDiscount(BigDecimal discountPercent, String reason) {
        if (subtotal != null) {
            this.discountAmount = subtotal.multiply(discountPercent);
            this.totalAmount = subtotal.add(taxAmount).subtract(discountAmount);
            this.notes = (notes != null ? notes + "; " : "") + "Discount applied: " + 
                        discountPercent.multiply(new BigDecimal("100")) + "% - " + reason;
        }
    }
    
    /**
     * Mark invoice as paid
     * @param payment the payment associated with this invoice
     */
    public void markAsPaid(Payment payment) {
        this.payment = payment;
        this.status = "PAID";
    }
    
    /**
     * Check if payment is overdue
     * @return true if payment is overdue, false otherwise
     */
    public boolean isOverdue() {
        return LocalDateTime.now().isAfter(dueDate) && !"PAID".equals(status);
    }
    
    /**
     * Update invoice status based on current state
     */
    public void updateStatus() {
        if (payment != null && "SUCCESS".equals(payment.getStatus())) {
            this.status = "PAID";
        } else if (isOverdue()) {
            this.status = "OVERDUE";
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Rental getRental() {
        return rental;
    }

    public void setRental(Rental rental) {
        this.rental = rental;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public LocalDateTime getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDateTime invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", invoiceDate=" + invoiceDate +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                '}';
    }
}
