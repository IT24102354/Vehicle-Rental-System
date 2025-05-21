package com.rental.vehicle.controller;

import com.rental.vehicle.model.User;
import com.rental.vehicle.model.Vehicle;
import com.rental.vehicle.model.Rental;
import com.rental.vehicle.model.Review;
import com.rental.vehicle.model.PublicReview;
import com.rental.vehicle.model.VerifiedReview;

import com.rental.vehicle.service.VehicleService;
import com.rental.vehicle.service.RentalService;
import com.rental.vehicle.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;


/**
 * Controller class for Review Management
 * Handles HTTP requests related to review operations
 */
@Controller
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private VehicleService vehicleService;
    
    @Autowired
    private RentalService rentalService;
    
    /**
     * Display submit review page for a vehicle
     * @param vehicleId The ID of the vehicle to review
     * @param model Model for view
     * @param session HTTP session
     * @return Review submission page view or redirect to login
     */
    @GetMapping("/vehicles/{vehicleId}/review")
    public String showReviewForm(@PathVariable Long vehicleId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return "redirect:/login";
        }
        
        try {
            Optional<Vehicle> vehicleOpt = vehicleService.getVehicleById(vehicleId);
            if (!vehicleOpt.isPresent()) {
                model.addAttribute("error", "Vehicle not found with ID: " + vehicleId);
                return "redirect:/vehicles";
            }
            
            Vehicle vehicle = vehicleOpt.get();
            
            // Check if user has already reviewed this vehicle
            if (reviewService.hasUserReviewedVehicle(user, vehicle)) {
                model.addAttribute("error", "You have already reviewed this vehicle.");
                return "redirect:/vehicles/" + vehicleId;
            }
            
            // Check if user has rented this vehicle (for verified reviews)
            List<Rental> userRentals = rentalService.getRentalsByUser(user);
            Rental eligibleRental = null;
            
            for (Rental rental : userRentals) {
                if (rental.getVehicle().getId().equals(vehicleId) && 
                    rental.getStatus().equals("COMPLETED") && 
                    !reviewService.hasVerifiedReviewForRental(rental)) {
                    eligibleRental = rental;
                    break;
                }
            }
            
            model.addAttribute("user", user);
            model.addAttribute("vehicle", vehicle);
            model.addAttribute("eligibleRental", eligibleRental);
            
            if (eligibleRental != null) {
                model.addAttribute("review", new VerifiedReview());
                model.addAttribute("reviewType", "verified");
            } else {
                model.addAttribute("review", new PublicReview());
                model.addAttribute("reviewType", "public");
            }
            
            return "reviews/submit_review";
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/vehicles";
        }
    }
    
    /**
     * Process public review submission
     * @param vehicleId The ID of the vehicle being reviewed
     * @param publicReview The public review to submit
     * @param result Binding result for validation
     * @param session HTTP session
     * @param request HTTP request
     * @param redirectAttributes Redirect attributes
     * @return Redirect to vehicle details or review form
     */
    @PostMapping("/vehicles/{vehicleId}/public-review")
    public String submitPublicReview(@PathVariable Long vehicleId,
                                    @Valid @ModelAttribute("review") PublicReview publicReview,
                                    BindingResult result,
                                    HttpSession session,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return "redirect:/login";
        }
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please correct the errors in your review.");
            return "redirect:/vehicles/" + vehicleId + "/review";
        }
        
        try {
            Optional<Vehicle> vehicleOpt = vehicleService.getVehicleById(vehicleId);
            if (!vehicleOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Vehicle not found with ID: " + vehicleId);
                return "redirect:/vehicles";
            }
            
            Vehicle vehicle = vehicleOpt.get();
            
            // Set the review properties
            publicReview.setUser(user);
            publicReview.setVehicle(vehicle);
            publicReview.setUserIpAddress(request.getRemoteAddr());
            
            // Create the review
            reviewService.createPublicReview(publicReview);
            
            redirectAttributes.addFlashAttribute("success", "Your review has been submitted and is pending approval.");
            return "redirect:/vehicles/" + vehicleId;
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid review submission: " + e.getMessage());
            return "redirect:/vehicles/" + vehicleId + "/review";
        } catch (NullPointerException e) {
            redirectAttributes.addFlashAttribute("error", "Missing required information: " + e.getMessage());
            return "redirect:/vehicles/" + vehicleId + "/review";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/vehicles/" + vehicleId + "/review";
        }
    }
    
    /**
     * Process verified review submission
     * @param vehicleId The ID of the vehicle being reviewed
     * @param rentalId The ID of the rental associated with the review
     * @param verifiedReview The verified review to submit
     * @param result Binding result for validation
     * @param session HTTP session
     * @param redirectAttributes Redirect attributes
     * @return Redirect to vehicle details or review form
     */
    @PostMapping("/vehicles/{vehicleId}/verified-review")
    public String submitVerifiedReview(@PathVariable Long vehicleId,
                                      @RequestParam Long rentalId,
                                      @Valid @ModelAttribute("review") VerifiedReview verifiedReview,
                                      BindingResult result,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return "redirect:/login";
        }
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please correct the errors in your review.");
            return "redirect:/vehicles/" + vehicleId + "/review";
        }
        
        try {
            Optional<Vehicle> vehicleOpt = vehicleService.getVehicleById(vehicleId);
            if (!vehicleOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Vehicle not found with ID: " + vehicleId);
                return "redirect:/vehicles";
            }
            
            Vehicle vehicle = vehicleOpt.get();
            Rental rental = rentalService.getRentalById(rentalId);
            
            // Validate that the rental belongs to the user and vehicle
            if (!rental.getUser().getId().equals(user.getId()) ||
                !rental.getVehicle().getId().equals(vehicle.getId())) {
                throw new IllegalArgumentException("Invalid rental for this review.");
            }
            
            // Set the review properties
            verifiedReview.setUser(user);
            verifiedReview.setVehicle(vehicle);
            verifiedReview.setRental(rental);
            verifiedReview.setVerificationDetails("Verified rental #" + rental.getId() + " completed on " + rental.getEndDate());
            
            // Create the review
            reviewService.createVerifiedReview(verifiedReview);
            
            redirectAttributes.addFlashAttribute("success", "Your verified review has been submitted. Thank you for your feedback!");
            return "redirect:/vehicles/" + vehicleId;
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid review submission: " + e.getMessage());
            return "redirect:/vehicles/" + vehicleId + "/review";
        } catch (NullPointerException e) {
            redirectAttributes.addFlashAttribute("error", "Missing required information: " + e.getMessage());
            return "redirect:/vehicles/" + vehicleId + "/review";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
            return "redirect:/vehicles/" + vehicleId + "/review";
        }
    }
    
    /**
     * Display vehicle reviews
     * @param vehicleId The ID of the vehicle to display reviews for
     * @param model Model for view
     * @param session HTTP session
     * @return Vehicle reviews page view
     */
    @GetMapping("/vehicles/{vehicleId}/reviews")
    public String showVehicleReviews(@PathVariable Long vehicleId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        
        try {
            Optional<Vehicle> vehicleOpt = vehicleService.getVehicleById(vehicleId);
            if (!vehicleOpt.isPresent()) {
                return "redirect:/vehicles?error=Vehicle+not+found";
            }
            
            Vehicle vehicle = vehicleOpt.get();
            List<Review> approvedReviews = reviewService.getApprovedReviewsByVehicle(vehicle);
            double averageRating = reviewService.getAverageRatingForVehicle(vehicle);
            
            model.addAttribute("vehicle", vehicle);
            model.addAttribute("reviews", approvedReviews);
            model.addAttribute("averageRating", averageRating);
            model.addAttribute("user", user);
            
            // Check if user can review this vehicle
            boolean canReview = false;
            if (user != null) {
                canReview = !reviewService.hasUserReviewedVehicle(user, vehicle);
            }
            model.addAttribute("canReview", canReview);
            
            return "reviews/vehicle_reviews";
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/vehicles";
        }
    }
    
    /**
     * Display user's reviews
     * @param model Model for view
     * @param session HTTP session
     * @return User reviews page view or redirect to login
     */
    @GetMapping("/my-reviews")
    public String showUserReviews(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return "redirect:/login";
        }
        
        List<Review> userReviews = reviewService.getReviewsByUser(user);
        
        model.addAttribute("user", user);
        model.addAttribute("reviews", userReviews);
        
        return "reviews/my_reviews";
    }
    
    /**
     * Display edit review form
     * @param reviewId The ID of the review to edit
     * @param model Model for view
     * @param session HTTP session
     * @return Edit review form view or redirect to login
     */
    @GetMapping("/reviews/{reviewId}/edit")
    public String showEditReviewForm(@PathVariable Long reviewId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return "redirect:/login";
        }
        
        try {
            Review review = reviewService.getReviewById(reviewId);
            
            // Check if the review belongs to the user
            if (!review.getUser().getId().equals(user.getId())) {
                model.addAttribute("error", "You are not authorized to edit this review.");
                return "redirect:/my-reviews";
            }
            
            model.addAttribute("review", review);
            model.addAttribute("vehicle", review.getVehicle());
            
            return "reviews/edit_review";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Invalid review request: " + e.getMessage());
            return "redirect:/my-reviews";
        } catch (NullPointerException e) {
            model.addAttribute("error", "Review not found: " + reviewId);
            return "redirect:/my-reviews";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while retrieving the review: " + e.getMessage());
            return "redirect:/my-reviews";
        }
    }
    
    /**
     * Process review update
     * @param reviewId The ID of the review to update
     * @param updatedReview The updated review details
     * @param result Binding result for validation
     * @param session HTTP session
     * @param redirectAttributes Redirect attributes
     * @return Redirect to user reviews or edit form
     */
    @PostMapping("/reviews/{reviewId}/update")
    public String updateReview(@PathVariable Long reviewId,
                              @Valid @ModelAttribute("review") Review updatedReview,
                              BindingResult result,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return "redirect:/login";
        }
        
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please correct the errors in your review.");
            return "redirect:/reviews/" + reviewId + "/edit";
        }
        
        try {
            Review existingReview = reviewService.getReviewById(reviewId);
            
            // Check if the review belongs to the user
            if (!existingReview.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this review.");
                return "redirect:/my-reviews";
            }
            
            // Update the review
            reviewService.updateReview(reviewId, updatedReview);
            
            redirectAttributes.addFlashAttribute("success", "Your review has been updated successfully.");
            return "redirect:/my-reviews";
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid review data: " + e.getMessage());
            return "redirect:/reviews/" + reviewId + "/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reviews/" + reviewId + "/edit";
        }
    }
    /**
     * Process review deletion
     * @param reviewId The ID of the review to delete
     * @param session HTTP session
     * @param redirectAttributes Redirect attributes
     * @return Redirect to user reviews
     */
    @PostMapping("/reviews/{reviewId}/delete")
    public String deleteReview(@PathVariable Long reviewId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return "redirect:/login";
        }
        
        try {
            Review review = reviewService.getReviewById(reviewId);
            
            // Check if the review belongs to the user or user is admin
            if (!review.getUser().getId().equals(user.getId()) && !user.isAdmin()) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to delete this review.");
                return "redirect:/my-reviews";
            }
            
            // Delete the review
            reviewService.deleteReview(reviewId);
            
            redirectAttributes.addFlashAttribute("success", "Your review has been deleted successfully.");
            
            if (user.isAdmin()) {
                return "redirect:/admin/reviews";
            } else {
                return "redirect:/my-reviews";
            }
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid review request: " + e.getMessage());
            return "redirect:/my-reviews";
        } catch (NullPointerException e) {
            redirectAttributes.addFlashAttribute("error", "Review not found or already deleted.");
            return "redirect:/my-reviews";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while deleting the review: " + e.getMessage());
            return "redirect:/my-reviews";
        }
    }
    
    /**
     * Display admin review management page
     * @param model Model for view
     * @param session HTTP session
     * @return Admin review management page view or redirect to login
     */
    @GetMapping("/admin/reviews")
    public String showAdminReviewsPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return "redirect:/login";
        }
        
        if (!user.isAdmin()) {
            return "redirect:/dashboard";
        }
        
        List<Review> allReviews = reviewService.getAllReviews();
        List<Review> pendingReviews = reviewService.getReviewsPendingApproval();
        
        model.addAttribute("user", user);
        model.addAttribute("allReviews", allReviews);
        model.addAttribute("pendingReviews", pendingReviews);
        
        return "admin/reviews";
    }
    
    /**
     * Process review approval
     * @param reviewId The ID of the review to approve
     * @param session HTTP session
     * @param redirectAttributes Redirect attributes
     * @return Redirect to admin reviews page
     */
    @PostMapping("/admin/reviews/{reviewId}/approve")
    public String approveReview(@PathVariable Long reviewId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return "redirect:/login";
        }
        
        if (!user.isAdmin()) {
            return "redirect:/dashboard";
        }
        
        try {
            reviewService.approveReview(reviewId);
            redirectAttributes.addFlashAttribute("success", "Review approved successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid review approval request: " + e.getMessage());
        } catch (NullPointerException e) {
            redirectAttributes.addFlashAttribute("error", "Review not found or already processed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred during review approval: " + e.getMessage());
        }
        
        return "redirect:/admin/reviews";
    }
    
    /**
     * Process review rejection
     * @param reviewId The ID of the review to reject
     * @param session HTTP session
     * @param redirectAttributes Redirect attributes
     * @return Redirect to admin reviews page
     */
    @PostMapping("/admin/reviews/{reviewId}/reject")
    public String rejectReview(@PathVariable Long reviewId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return "redirect:/login";
        }
        
        if (!user.isAdmin()) {
            return "redirect:/dashboard";
        }
        
        try {
            reviewService.rejectReview(reviewId);
            redirectAttributes.addFlashAttribute("success", "Review rejected successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid review rejection request: " + e.getMessage());
        } catch (NullPointerException e) {
            redirectAttributes.addFlashAttribute("error", "Review not found or already processed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred during review rejection: " + e.getMessage());
        }
        
        return "redirect:/admin/reviews";
    }
}
