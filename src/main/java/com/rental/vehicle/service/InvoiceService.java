package com.rental.vehicle.service;

import com.rental.vehicle.model.Invoice;
import com.rental.vehicle.model.Payment;
import com.rental.vehicle.model.Rental;
import com.rental.vehicle.model.User;
import com.rental.vehicle.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Invoice management
 * Handles business logic related to invoices
 */
@Service
public class InvoiceService {
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    /**
     * Generate a new invoice for a rental
     * @param rental the rental to generate invoice for
     * @return the generated invoice
     */
    public Invoice generateInvoice(Rental rental) {
        Invoice invoice = new Invoice(rental);
        invoice.calculateAmounts();
        return invoiceRepository.save(invoice);
    }
    
    /**
     * Apply a discount to an invoice
     * @param invoiceId invoice id
     * @param discountPercent discount percentage as a decimal (e.g., 0.10 for 10%)
     * @param reason reason for the discount
     * @return the updated invoice
     */
    public Optional<Invoice> applyDiscount(Long invoiceId, BigDecimal discountPercent, String reason) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isPresent()) {
            Invoice invoice = invoiceOpt.get();
            invoice.applyDiscount(discountPercent, reason);
            return Optional.of(invoiceRepository.save(invoice));
        }
        return Optional.empty();
    }
    
    /**
     * Mark an invoice as paid
     * @param invoiceId invoice id
     * @param payment the payment associated with this invoice
     * @return the updated invoice
     */
    public Optional<Invoice> markAsPaid(Long invoiceId, Payment payment) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isPresent()) {
            Invoice invoice = invoiceOpt.get();
            invoice.markAsPaid(payment);
            return Optional.of(invoiceRepository.save(invoice));
        }
        return Optional.empty();
    }
    
    /**
     * Update invoice status
     * @param invoiceId invoice id
     * @return the updated invoice
     */
    public Optional<Invoice> updateInvoiceStatus(Long invoiceId) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isPresent()) {
            Invoice invoice = invoiceOpt.get();
            invoice.updateStatus();
            return Optional.of(invoiceRepository.save(invoice));
        }
        return Optional.empty();
    }
    
    /**
     * Get all invoices
     * @return list of all invoices
     */
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }
    
    /**
     * Get invoice by id
     * @param id invoice id
     * @return optional invoice
     */
    public Optional<Invoice> getInvoiceById(Long id) {
        return invoiceRepository.findById(id);
    }
    
    /**
     * Get invoice by invoice number
     * @param invoiceNumber the unique invoice number
     * @return optional invoice
     */
    public Optional<Invoice> getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber);
    }
    
    /**
     * Get invoices by user
     * @param user the user who made the rentals
     * @return list of invoices for the user
     */
    public List<Invoice> getInvoicesByUser(User user) {
        return invoiceRepository.findByRentalUser(user);
    }
    
    /**
     * Get invoices by rental
     * @param rental the rental associated with the invoices
     * @return list of invoices for the rental
     */
    public List<Invoice> getInvoicesByRental(Rental rental) {
        return invoiceRepository.findByRental(rental);
    }
    
    /**
     * Get invoices by status
     * @param status the invoice status
     * @return list of invoices with the specified status
     */
    public List<Invoice> getInvoicesByStatus(String status) {
        return invoiceRepository.findByStatus(status);
    }
    
    /**
     * Delete invoice by id
     * @param id invoice id
     */
    public void deleteInvoice(Long id) {
        invoiceRepository.deleteById(id);
    }
}
