package com.rental.vehicle.service;

import com.rental.vehicle.datastructure.UserLinkedList;
import com.rental.vehicle.model.Invoice;
import com.rental.vehicle.model.Payment;
import com.rental.vehicle.model.Rental;
import com.rental.vehicle.model.Review;
import com.rental.vehicle.model.User;
import com.rental.vehicle.model.VerifiedReview;
import com.rental.vehicle.repository.InvoiceRepository;
import com.rental.vehicle.repository.PaymentRepository;
import com.rental.vehicle.repository.RentalRepository;
import com.rental.vehicle.repository.ReviewRepository;
import com.rental.vehicle.repository.UserRepository;
import com.rental.vehicle.repository.VerifiedReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service class for User Management
 * Handles business logic for user operations
 */
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RentalRepository rentalRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private VerifiedReviewRepository verifiedReviewRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    // Using linked list as a data structure for in-memory operations
    private UserLinkedList userList = new UserLinkedList();
    
    /**
     * Register a new user
     * @param user User to be registered
     * @return Registered user
     */
    public User registerUser(User user) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Save user in database
        User savedUser = userRepository.save(user);
        
        // Add user to linked list
        userList.add(savedUser);
        
        return savedUser;
    }
    
    /**
     * Get a user by ID
     * @param id User ID
     * @return User if found
     */
    public User getUserById(Long id) {
        // First check in linked list for faster retrieval
        User user = userList.findById(id);
        
        if (user != null) {
            return user;
        }
        
        // If not found in linked list, check in database
        Optional<User> optionalUser = userRepository.findById(id);
        
        if (optionalUser.isPresent()) {
            // Add to linked list for future faster retrieval
            User foundUser = optionalUser.get();
            userList.add(foundUser);
            return foundUser;
        }
        
        throw new RuntimeException("User not found with ID: " + id);
    }
    
    /**
     * Get a user by username
     * @param username Username
     * @return User if found
     */
    public User getUserByUsername(String username) {
        // First check in linked list for faster retrieval
        User user = userList.findByUsername(username);
        
        if (user != null) {
            return user;
        }
        
        // If not found in linked list, check in database
        User foundUser = userRepository.findByUsername(username);
        
        if (foundUser != null) {
            // Add to linked list for future faster retrieval
            userList.add(foundUser);
            return foundUser;
        }
        
        throw new RuntimeException("User not found with username: " + username);
    }
    
    /**
     * Get all users
     * @return List of all users
     */
    public List<User> getAllUsers() {
        // Refresh linked list data from database
        List<User> users = userRepository.findAll();
        
        // Clear and rebuild linked list
        userList = new UserLinkedList();
        for (User user : users) {
            userList.add(user);
        }
        
        return users;
    }
    
    /**
     * Update user information
     * @param id User ID
     * @param userDetails Updated user details
     * @return Updated user
     */
    public User updateUser(Long id, User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);
        
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            
            // Update user information
            user.setFullName(userDetails.getFullName());
            user.setPhone(userDetails.getPhone());
            
            // Email change requires checking if the new email is available
            if (!user.getEmail().equals(userDetails.getEmail()) && 
                userRepository.existsByEmail(userDetails.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(userDetails.getEmail());
            
            // Save updated user
            User updatedUser = userRepository.save(user);
            
            // Update linked list
            userList.update(updatedUser);
            
            return updatedUser;
        }
        
        throw new RuntimeException("User not found with ID: " + id);
    }
    
    /**
     * Delete a user and all associated records to maintain referential integrity
     * @param id User ID
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        
        // Get all rentals for this user
        List<Rental> userRentals = rentalRepository.findByUser(user);
        
        // For each rental, handle all dependencies in proper order
        for (Rental rental : userRentals) {
            // 1. First, handle payments (no other entities depend on payments)
            List<Payment> payments = paymentRepository.findByRental(rental);
            for (Payment payment : payments) {
                paymentRepository.delete(payment);
            }
            
            // 2. Handle invoices (which may reference payments, but payments are already deleted)
            List<Invoice> invoices = invoiceRepository.findByRental(rental);
            for (Invoice invoice : invoices) {
                invoiceRepository.delete(invoice);
            }
            
            // 3. Handle verified reviews
            List<VerifiedReview> verifiedReviews = verifiedReviewRepository.findByRental(rental);
            for (VerifiedReview review : verifiedReviews) {
                verifiedReviewRepository.delete(review);
            }
            
            // 4. Now it's safe to delete the rental
            rentalRepository.delete(rental);
        }
        
        // Delete any remaining reviews by this user
        List<Review> userReviews = reviewRepository.findByUser(user);
        for (Review review : userReviews) {
            reviewRepository.delete(review);
        }
        
        // Now delete the user
        userRepository.delete(user);
        
        // Remove from linked list
        userList.delete(id);
    }
    
    /**
     * Authenticate user login
     * @param username Username
     * @param password Password
     * @return User if authentication is successful
     */
    public User authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        
        throw new RuntimeException("Invalid username or password");
    }
}