package com.rental.vehicle.controller;

import com.rental.vehicle.model.Invoice;
import com.rental.vehicle.model.Payment;
import com.rental.vehicle.model.Rental;
import com.rental.vehicle.model.User;
import com.rental.vehicle.service.InvoiceService;
import com.rental.vehicle.service.PaymentService;
import com.rental.vehicle.service.RentalService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Controller class for Payment and Invoice Management
 * Handles HTTP requests related to payments and invoices
 */
@Controller
@RequestMapping("/payments")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private InvoiceService invoiceService;
    
    @Autowired
    private RentalService rentalService;
    
    /**
     * Check if the user is admin
     * @param session HTTP session
     * @return true if admin, false otherwise
     */
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN".equals(user.getRole());
    }
    
    /**
     * Get current user from session
     * @param session HTTP session
     * @return user from session or null if not logged in
     */
    private User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("user");
    }
    
    /**
     * Display payment processing page
     * @param rentalId rental ID
     * @param model Model for view
     * @return payment processing page view
     */
    @GetMapping("/process/{rentalId}")
    public String showPaymentForm(@PathVariable Long rentalId, Model model, HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to process a payment");
            return "redirect:/login";
        }
        
        Rental rental;
        try {
            rental = rentalService.getRentalById(rentalId);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Rental not found");
            return "redirect:/rentals/my";
        }
        
        // Check if the current user is the rental owner or an admin
        if (!rental.getUser().getId().equals(currentUser.getId()) && !isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to process this payment");
            return "redirect:/rentals/my";
        }
        
        model.addAttribute("rental", rental);
        model.addAttribute("paymentMethods", List.of("CREDIT_CARD", "DEBIT_CARD", "CASH", "BANK_TRANSFER"));
        return "payments/process";
    }
    
    /**
     * Process payment submission
     * @param rentalId rental ID
     * @param paymentMethod payment method
     * @param cardLast4Digits last 4 digits of the card (for card payments)
     * @return redirect to payment confirmation page
     */
    @PostMapping("/process/{rentalId}")
    public String processPayment(@PathVariable Long rentalId,
                                @RequestParam String paymentMethod,
                                @RequestParam(required = false) String cardLast4Digits,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to process a payment");
            return "redirect:/login";
        }
        
        Rental rental;
        try {
            rental = rentalService.getRentalById(rentalId);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Rental not found");
            return "redirect:/rentals/my";
        }
        
        // Check if the current user is the rental owner or an admin
        if (!rental.getUser().getId().equals(currentUser.getId()) && !isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to process this payment");
            return "redirect:/rentals/my";
        }
        
        // Create payment
        Payment payment;
        if ("CREDIT_CARD".equals(paymentMethod) || "DEBIT_CARD".equals(paymentMethod)) {
            if (cardLast4Digits == null || cardLast4Digits.length() != 4) {
                redirectAttributes.addFlashAttribute("error", "Invalid card information");
                return "redirect:/payments/process/" + rentalId;
            }
            payment = paymentService.createCardPayment(rental, paymentMethod, cardLast4Digits);
        } else {
            payment = paymentService.createPayment(rental, paymentMethod);
        }
        
        // Process the payment
        boolean success = paymentService.processPayment(payment);
        
        if (success) {
            // Generate or update invoice
            Invoice invoice;
            List<Invoice> existingInvoices = invoiceService.getInvoicesByRental(rental);
            if (existingInvoices.isEmpty()) {
                invoice = invoiceService.generateInvoice(rental);
            } else {
                invoice = existingInvoices.get(0);
            }
            
            // Mark invoice as paid
            invoiceService.markAsPaid(invoice.getId(), payment);
            
            // Update rental payment status
            rental.setPaymentStatus("PAID");
            rentalService.updateRental(rental.getId(), rental);
            
            redirectAttributes.addFlashAttribute("success", "Payment processed successfully");
            return "redirect:/payments/confirmation/" + payment.getId();
        } else {
            redirectAttributes.addFlashAttribute("error", "Payment processing failed. Please try again.");
            return "redirect:/payments/process/" + rentalId;
        }
    }
    
    /**
     * Display payment confirmation page
     * @param paymentId payment ID
     * @param model Model for view
     * @return payment confirmation page view
     */
    @GetMapping("/confirmation/{paymentId}")
    public String showConfirmation(@PathVariable Long paymentId, Model model, HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to view payment confirmation");
            return "redirect:/login";
        }
        
        Optional<Payment> paymentOpt = paymentService.getPaymentById(paymentId);
        if (paymentOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Payment not found");
            return "redirect:/rentals/my";
        }
        
        Payment payment = paymentOpt.get();
        Rental rental = payment.getRental();
        
        // Check if the current user is the rental owner or an admin
        if (!rental.getUser().getId().equals(currentUser.getId()) && !isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to view this payment");
            return "redirect:/rentals/my";
        }
        
        // Get the invoice
        List<Invoice> invoices = invoiceService.getInvoicesByRental(rental);
        if (!invoices.isEmpty()) {
            model.addAttribute("invoice", invoices.get(0));
        }
        
        model.addAttribute("payment", payment);
        model.addAttribute("rental", rental);
        return "payments/confirmation";
    }
    
    /**
     * Display payment history page
     * @param model Model for view
     * @return payment history page view
     */
    @GetMapping("/history")
    public String showPaymentHistory(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to view payment history");
            return "redirect:/login";
        }
        
        List<Payment> payments;
        if (isAdmin(session)) {
            // Admin sees all payments
            payments = paymentService.getAllPayments();
        } else {
            // Regular user sees only their payments
            payments = paymentService.getPaymentsByUser(currentUser);
        }
        
        model.addAttribute("payments", payments);
        model.addAttribute("isAdmin", isAdmin(session));
        return "payments/history";
    }
    
    /**
     * Display invoice details
     * @param invoiceId invoice ID
     * @param model Model for view
     * @return invoice details page view
     */
    @GetMapping("/invoice/{invoiceId}")
    public String viewInvoice(@PathVariable Long invoiceId, Model model, HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to view an invoice");
            return "redirect:/login";
        }
        
        Optional<Invoice> invoiceOpt = invoiceService.getInvoiceById(invoiceId);
        if (invoiceOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invoice not found");
            return "redirect:/payments/history";
        }
        
        Invoice invoice = invoiceOpt.get();
        Rental rental = invoice.getRental();
        
        // Check if the current user is the rental owner or an admin
        if (!rental.getUser().getId().equals(currentUser.getId()) && !isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "You do not have permission to view this invoice");
            return "redirect:/payments/history";
        }
        
        model.addAttribute("invoice", invoice);
        model.addAttribute("rental", rental);
        return "payments/invoice";
    }
    
    /**
     * Display all invoices (admin only)
     * @param model Model for view
     * @return invoice list page view
     */
    @GetMapping("/invoices")
    public String listInvoices(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Only administrators can view all invoices");
            return "redirect:/payments/history";
        }
        
        List<Invoice> invoices = invoiceService.getAllInvoices();
        model.addAttribute("invoices", invoices);
        return "payments/invoices";
    }
    
    /**
     * Apply discount to invoice (admin only)
     * @param invoiceId invoice ID
     * @param discountPercent discount percentage (0-100)
     * @param reason reason for discount
     * @return redirect to invoice details
     */
    @PostMapping("/invoice/{invoiceId}/discount")
    public String applyDiscount(@PathVariable Long invoiceId,
                               @RequestParam Double discountPercent,
                               @RequestParam String reason,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Only administrators can apply discounts");
            return "redirect:/payments/invoice/" + invoiceId;
        }
        
        if (discountPercent <= 0 || discountPercent > 100) {
            redirectAttributes.addFlashAttribute("error", "Discount percentage must be between 0 and 100");
            return "redirect:/payments/invoice/" + invoiceId;
        }
        
        // Convert percentage to decimal (e.g., 10% -> 0.10)
        BigDecimal discountDecimal = BigDecimal.valueOf(discountPercent / 100.0);
        
        Optional<Invoice> result = invoiceService.applyDiscount(invoiceId, discountDecimal, reason);
        if (result.isPresent()) {
            redirectAttributes.addFlashAttribute("success", "Discount applied successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to apply discount");
        }
        
        return "redirect:/payments/invoice/" + invoiceId;
    }
    
    /**
     * Process refund for a payment (admin only)
     * @param paymentId payment ID
     * @param reason reason for refund
     * @return redirect to payment history
     */
    @PostMapping("/{paymentId}/refund")
    public String processRefund(@PathVariable Long paymentId,
                               @RequestParam String reason,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Only administrators can process refunds");
            return "redirect:/payments/history";
        }
        
        boolean success = paymentService.refundPayment(paymentId, reason);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Refund processed successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to process refund");
        }
        
        return "redirect:/payments/history";
    }
}
