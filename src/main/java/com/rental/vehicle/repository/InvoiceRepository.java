package com.rental.vehicle.repository;

import com.rental.vehicle.model.Invoice;
import com.rental.vehicle.model.Rental;
import com.rental.vehicle.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Invoice entity
 * Handles database operations for invoices
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    /**
     * Find invoice by invoice number
     * @param invoiceNumber the unique invoice number
     * @return optional invoice
     */
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    /**
     * Find invoices by rental
     * @param rental the rental associated with the invoices
     * @return list of invoices for the rental
     */
    List<Invoice> findByRental(Rental rental);
    
    /**
     * Find invoices by rental's user
     * @param user the user who made the rentals
     * @return list of invoices for the user
     */
    List<Invoice> findByRentalUser(User user);
    
    /**
     * Find invoices by status
     * @param status the invoice status
     * @return list of invoices with the specified status
     */
    List<Invoice> findByStatus(String status);
}
