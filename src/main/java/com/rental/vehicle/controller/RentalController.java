package com.rental.vehicle.controller;

import com.rental.vehicle.model.Rental;
import com.rental.vehicle.model.User;
import com.rental.vehicle.model.Vehicle;
import com.rental.vehicle.service.RentalService;
import com.rental.vehicle.service.UserService;
import com.rental.vehicle.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Controller for Rental Management
 * Handles HTTP requests related to rental operations
 */
@Controller
@RequestMapping("/rentals")
public class RentalController {
    
    @Autowired
    private RentalService rentalService;
    
    @Autowired
    private VehicleService vehicleService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Check if the user is logged in
     * @param session HTTP session
     * @return true if logged in, false otherwise
     */
    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("user") != null;
    }
    
    /**
     * Check if the user is an admin
     * @param session HTTP session
     * @return true if admin, false otherwise
     */
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN".equals(user.getRole());
    }
    
    /**
     * Display rental booking form
     * @param vehicleId Vehicle ID
     * @param model Model for view
     * @param session HTTP session
     * @return Booking form view or redirect to login
     */
    @GetMapping("/book/{vehicleId}")
    public String showBookingForm(@PathVariable Long vehicleId, Model model, HttpSession session, 
                                 RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        try {
            // Get the vehicle by ID and handle the Optional properly
            Vehicle vehicle = vehicleService.getVehicleById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
            
            // Check if vehicle is available using the correct getter method
            if (!vehicle.getAvailable()) {
                redirectAttributes.addFlashAttribute("error", "Vehicle is not available for booking");
                return "redirect:/vehicles";
            }
            
            model.addAttribute("vehicle", vehicle);
            model.addAttribute("rental", new Rental());
            model.addAttribute("minDate", LocalDate.now());
            model.addAttribute("maxDate", LocalDate.now().plusMonths(3)); // Allow booking up to 3 months in advance
            model.addAttribute("user", session.getAttribute("user"));
            
            return "rentals/booking-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vehicles";
        }
    }
    
    /**
     * Process rental booking
     * @param vehicleId Vehicle ID
     * @param rental Rental to be created
     * @param result Binding result for validation
     * @param session HTTP session
     * @param redirectAttributes Redirect attributes
     * @return Redirect to rental confirmation or back to booking form
     */
    @PostMapping("/book/{vehicleId}")
    public String bookVehicle(
            @PathVariable Long vehicleId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String notes,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // Log parameters for debugging
        System.out.println("Booking vehicle: " + vehicleId);
        System.out.println("Start date: " + startDate);
        System.out.println("End date: " + endDate);
        System.out.println("Notes: " + notes);
        
        // Check if user is logged in
        if (!isLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("error", "Please login to book a vehicle");
            return "redirect:/login";
        }
        
        // Get vehicle first to ensure it exists and is available
        Vehicle vehicle;
        try {
            vehicle = vehicleService.getVehicleById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
            
            // Check if vehicle is available
            if (!vehicle.getAvailable()) {
                redirectAttributes.addFlashAttribute("error", "Vehicle is not available for booking");
                return "redirect:/vehicles";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vehicles";
        }
        
        // Validate dates
        LocalDate today = LocalDate.now();
        if (startDate == null) {
            redirectAttributes.addFlashAttribute("error", "Start date is required");
            return "redirect:/rentals/book/" + vehicleId;
        } else if (startDate.isBefore(today)) {
            redirectAttributes.addFlashAttribute("error", "Start date cannot be in the past");
            return "redirect:/rentals/book/" + vehicleId;
        }
        
        if (endDate == null) {
            redirectAttributes.addFlashAttribute("error", "End date is required");
            return "redirect:/rentals/book/" + vehicleId;
        } else if (endDate.isBefore(startDate)) {
            redirectAttributes.addFlashAttribute("error", "End date cannot be before start date");
            return "redirect:/rentals/book/" + vehicleId;
        }
        
        try {
            User user = (User) session.getAttribute("user");
            
            // Create a new rental object
            Rental rental = new Rental();
            rental.setVehicle(vehicle);
            rental.setUser(user);
            rental.setStartDate(startDate);
            rental.setEndDate(endDate);
            rental.setNotes(notes);
            rental.setStatus("PENDING");
            rental.setPaymentStatus("PENDING");
            
            // Calculate the total cost server-side
            long days = ChronoUnit.DAYS.between(startDate, endDate);
            if (days <= 0) days = 1; // Minimum 1 day rental
            
            // Convert to double before multiplication
            double dailyRate = vehicle.getDailyRate().doubleValue();
            double totalCost = dailyRate * days;
            rental.setTotalCost(totalCost);
            
            System.out.println("Calculated days: " + days);
            System.out.println("Calculated total cost: " + totalCost);
            
            // Create the rental in the database
            Rental createdRental = rentalService.createRental(rental);
            
            redirectAttributes.addFlashAttribute("success", "Vehicle booked successfully");
            return "redirect:/rentals/confirmation/" + createdRental.getId();
        } catch (Exception e) {
            // Log the exception for debugging
            System.err.println("Error creating rental: " + e.getMessage());
            e.printStackTrace();
            
            // Add error message and return to form
            redirectAttributes.addFlashAttribute("error", "Failed to book vehicle: " + e.getMessage());
            return "redirect:/rentals/book/" + vehicleId;
        }
    }
    
    /**
     * Display rental confirmation
     * @param rentalId Rental ID
     * @param model Model for view
     * @param session HTTP session
     * @return Confirmation view or redirect to login
     */
    @GetMapping("/confirmation/{rentalId}")
    public String showConfirmation(@PathVariable Long rentalId, Model model, HttpSession session) {
        // Check if user is logged in
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        try {
            Rental rental = rentalService.getRentalById(rentalId);
            User currentUser = (User) session.getAttribute("user");
            
            // Check if the rental belongs to the current user or if the user is an admin
            if (!rental.getUser().getId().equals(currentUser.getId()) && !isAdmin(session)) {
                return "redirect:/rentals/my";
            }
            
            model.addAttribute("rental", rental);
            model.addAttribute("user", currentUser);
            
            return "rentals/confirmation";
        } catch (Exception e) {
            return "redirect:/rentals/my";
        }
    }
    
    /**
     * Display user's rentals
     * @param model Model for view
     * @param session HTTP session
     * @return User's rentals view or redirect to login
     */
    @GetMapping("/my")
    public String showMyRentals(Model model, HttpSession session) {
        // Check if user is logged in
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        User user = (User) session.getAttribute("user");
        
        // Get user's rentals
        List<Rental> activeRentals = rentalService.getActiveRentalsByUser(user);
        List<Rental> pendingRentals = rentalService.getPendingRentalsByUser(user);
        List<Rental> completedRentals = rentalService.getCompletedRentalsByUser(user);
        
        model.addAttribute("activeRentals", activeRentals);
        model.addAttribute("pendingRentals", pendingRentals);
        model.addAttribute("completedRentals", completedRentals);
        model.addAttribute("user", user);
        
        return "rentals/my-rentals";
    }
    
    /**
     * Display rental details
     * @param rentalId Rental ID
     * @param model Model for view
     * @param session HTTP session
     * @return Rental details view or redirect to login
     */
    @GetMapping({"/details/{rentalId}", "/{rentalId}"})
    public String showRentalDetails(@PathVariable Long rentalId, Model model, HttpSession session) {
        // Check if user is logged in
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        try {
            System.out.println("Showing rental details for ID: " + rentalId);
            Rental rental = rentalService.getRentalById(rentalId);
            User currentUser = (User) session.getAttribute("user");
            
            // Check if the rental belongs to the current user or if the user is an admin
            if (!rental.getUser().getId().equals(currentUser.getId()) && !isAdmin(session)) {
                return "redirect:/rentals/my";
            }
            
            model.addAttribute("rental", rental);
            model.addAttribute("user", currentUser);
            model.addAttribute("isAdmin", isAdmin(session));
            
            return "rentals/details";
        } catch (Exception e) {
            System.err.println("Error showing rental details: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/rentals/my";
        }
    }
    
    /**
     * Cancel a rental
     * @param rentalId Rental ID
     * @param session HTTP session
     * @param redirectAttributes Redirect attributes
     * @return Redirect to user's rentals
     */
    @GetMapping("/cancel/{rentalId}")
    public String cancelRental(@PathVariable Long rentalId, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        try {
            Rental rental = rentalService.getRentalById(rentalId);
            User currentUser = (User) session.getAttribute("user");
            
            // Check if the rental belongs to the current user or if the user is an admin
            if (!rental.getUser().getId().equals(currentUser.getId()) && !isAdmin(session)) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to cancel this rental");
                return "redirect:/rentals/my";
            }
            
            // Check if the rental can be cancelled
            if (!"PENDING".equals(rental.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "Only pending rentals can be cancelled");
                return "redirect:/rentals/" + rentalId;
            }
            
            rentalService.cancelRental(rentalId);
            redirectAttributes.addFlashAttribute("success", "Rental cancelled successfully");
            
            return "redirect:/rentals/my";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/rentals/my";
        }
    }
    
    /**
     * Display all rentals (admin only)
     * @param model Model for view
     * @param session HTTP session
     * @return All rentals view or redirect to login
     */
    @GetMapping("/all")
    public String showAllRentals(Model model, HttpSession session) {
        // Check if user is logged in and is an admin
        if (!isLoggedIn(session) || !isAdmin(session)) {
            return "redirect:/login";
        }
        
        // Get all rentals
        List<Rental> activeRentals = rentalService.getActiveRentals();
        List<Rental> pendingRentals = rentalService.getPendingRentals();
        List<Rental> completedRentals = rentalService.getCompletedRentals();
        
        model.addAttribute("activeRentals", activeRentals);
        model.addAttribute("pendingRentals", pendingRentals);
        model.addAttribute("completedRentals", completedRentals);
        model.addAttribute("user", session.getAttribute("user"));
        
        return "rentals/all-rentals";
    }
    
    /**
     * Display return vehicle form
     * @param rentalId Rental ID
     * @param model Model for view
     * @param session HTTP session
     * @return Return form view or redirect to login
     */
    @GetMapping("/return/{rentalId}")
    public String showReturnForm(@PathVariable Long rentalId, Model model, HttpSession session) {
        // Check if user is logged in and is an admin
        if (!isLoggedIn(session) || !isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            Rental rental = rentalService.getRentalById(rentalId);
            
            // Check if the rental is active
            if (!"ACTIVE".equals(rental.getStatus())) {
                return "redirect:/rentals/all";
            }
            
            model.addAttribute("rental", rental);
            model.addAttribute("returnDate", LocalDate.now());
            model.addAttribute("user", session.getAttribute("user"));
            
            return "rentals/return-form";
        } catch (Exception e) {
            return "redirect:/rentals/all";
        }
    }
    
    /**
     * Process vehicle return
     * @param rentalId Rental ID
     * @param returnDate Return date
     * @param notes Return notes
     * @param session HTTP session
     * @param redirectAttributes Redirect attributes
     * @return Redirect to all rentals
     */
    @PostMapping("/return/{rentalId}")
    public String processReturn(
            @PathVariable Long rentalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
            @RequestParam(required = false) String notes,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // Check if user is logged in and is an admin
        if (!isLoggedIn(session) || !isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            rentalService.processReturn(rentalId, returnDate, notes);
            redirectAttributes.addFlashAttribute("success", "Vehicle returned successfully");
            
            return "redirect:/rentals/all";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/rentals/return/" + rentalId;
        }
    }
    
    /**
     * Delete a rental (admin or rental owner)
     * @param rentalId Rental ID
     * @param session HTTP session
     * @param redirectAttributes Redirect attributes
     * @return Redirect to appropriate rentals page
     */
    @GetMapping("/delete/{rentalId}")
    public String deleteRental(@PathVariable Long rentalId, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        try {
            Rental rental = rentalService.getRentalById(rentalId);
            User currentUser = (User) session.getAttribute("user");
            
            // Check if the rental belongs to the current user or if the user is an admin
            if (!rental.getUser().getId().equals(currentUser.getId()) && !isAdmin(session)) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to delete this rental");
                return "redirect:/rentals/my";
            }
            
            rentalService.deleteRental(rentalId);
            redirectAttributes.addFlashAttribute("success", "Rental deleted successfully");
            
            // Redirect to appropriate page based on user role
            if (isAdmin(session)) {
                return "redirect:/rentals/all";
            } else {
                return "redirect:/rentals/my";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            if (isAdmin(session)) {
                return "redirect:/rentals/all";
            } else {
                return "redirect:/rentals/my";
            }
        }
    }
    
    /**
     * Display edit rental form
     * @param rentalId Rental ID
     * @param model Model for view
     * @param session HTTP session
     * @return Edit form view or redirect to login
     */
    @GetMapping("/edit/{rentalId}")
    public String showEditForm(@PathVariable Long rentalId, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        try {
            Rental rental = rentalService.getRentalById(rentalId);
            User currentUser = (User) session.getAttribute("user");
            
            // Check if the rental belongs to the current user or if the user is an admin
            if (!rental.getUser().getId().equals(currentUser.getId()) && !isAdmin(session)) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this rental");
                return "redirect:/rentals/my";
            }
            
            // Check if the rental can be edited (only pending rentals can be edited)
            if (!"PENDING".equals(rental.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "Only pending rentals can be edited");
                return "redirect:/rentals/details/" + rentalId;
            }
            
            model.addAttribute("rental", rental);
            model.addAttribute("vehicle", rental.getVehicle());
            model.addAttribute("user", currentUser);
            
            return "rentals/edit-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/rentals/my";
        }
    }
    
    /**
     * Process rental edit
     * @param rentalId Rental ID
     * @param startDate New start date
     * @param endDate New end date
     * @param notes New notes
     * @param session HTTP session
     * @param redirectAttributes Redirect attributes
     * @return Redirect to rental details
     */
    @PostMapping("/edit/{rentalId}")
    public String processEdit(
            @PathVariable Long rentalId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String notes,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        // Check if user is logged in
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        
        try {
            Rental rental = rentalService.getRentalById(rentalId);
            User currentUser = (User) session.getAttribute("user");
            
            // Check if the rental belongs to the current user or if the user is an admin
            if (!rental.getUser().getId().equals(currentUser.getId()) && !isAdmin(session)) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this rental");
                return "redirect:/rentals/my";
            }
            
            // Validate dates
            if (startDate.isBefore(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("error", "Start date cannot be in the past");
                return "redirect:/rentals/edit/" + rentalId;
            }
            
            if (endDate.isBefore(startDate)) {
                redirectAttributes.addFlashAttribute("error", "End date cannot be before start date");
                return "redirect:/rentals/edit/" + rentalId;
            }
            
            // Calculate total cost
            long days = ChronoUnit.DAYS.between(startDate, endDate);
            double dailyRate = rental.getVehicle().getDailyRate().doubleValue();
            double totalCost = dailyRate * days;
            
            // Update rental
            rentalService.updateRental(rentalId, startDate, endDate, notes, totalCost);
            redirectAttributes.addFlashAttribute("success", "Rental updated successfully");
            
            return "redirect:/rentals/details/" + rentalId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/rentals/edit/" + rentalId;
        }
    }
    
    /**
     * Activate a pending rental
     * @param rentalId Rental ID
     * @param session HTTP session
     * @param redirectAttributes Redirect attributes
     * @return Redirect to rental details
     */
    @GetMapping("/activate/{rentalId}")
    public String activateRental(@PathVariable Long rentalId, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in and is an admin
        if (!isLoggedIn(session) || !isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            rentalService.activateRentalById(rentalId);
            redirectAttributes.addFlashAttribute("success", "Rental activated successfully");
            
            return "redirect:/rentals/details/" + rentalId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/rentals/details/" + rentalId;
        }
    }
}
