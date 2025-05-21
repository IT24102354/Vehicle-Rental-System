package com.rental.vehicle.controller;

import com.rental.vehicle.model.User;
import com.rental.vehicle.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller class for User Management
 * Handles HTTP requests related to user operations
 */
@Controller
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Display home page
     * @param model Model for view
     * @return Home page view
     */
    @GetMapping("/")
    public String home(Model model) {
        return "index";
    }
    
    /**
     * Display registration page
     * @param model Model for view
     * @return Registration page view
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
    
    /**
     * Process user registration
     * @param user User to be registered
     * @param result Binding result for validation
     * @param redirectAttributes Redirect attributes
     * @return Redirect to login page or back to registration form
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }
        
        try {
            // Set default role to USER for new registrations
            user.setRole("USER");
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success", "Registration successful. Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
    
    /**
     * Display login page
     * @param model Model for view
     * @return Login page view
     */
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "login";
    }
    
    /**
     * Process user login
     * @param username Username
     * @param password Password
     * @param session HTTP session
     * @param redirectAttributes Redirect attributes
     * @return Redirect to dashboard or back to login form
     */
    @PostMapping("/login")
    public String loginUser(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        try {
            User user = userService.authenticateUser(username, password);
            session.setAttribute("user", user);
            
            // Redirect based on role
            if ("ADMIN".equals(user.getRole())) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/dashboard";
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }
    
    /**
     * Display user dashboard
     * @param model Model for view
     * @param session HTTP session
     * @return Dashboard page view or redirect to login
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return "redirect:/login";
        }
        
        // Redirect admin to admin dashboard
        if ("ADMIN".equals(user.getRole())) {
            return "redirect:/admin/dashboard";
        }
        
        model.addAttribute("user", user);
        return "dashboard";
    }
    
    /**
     * Display profile page
     * @param model Model for view
     * @param session HTTP session
     * @return Profile page view or redirect to login
     */
    @GetMapping("/profile")
    public String showProfile(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        return "profile";
    }
    
    /**
     * Process profile update
     * @param user Updated user details
     * @param result Binding result for validation
     * @param session HTTP session
     * @param redirectAttributes Redirect attributes
     * @return Redirect to profile page
     */
    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "profile";
        }
        
        User currentUser = (User) session.getAttribute("user");
        
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        try {
            User updatedUser = userService.updateUser(currentUser.getId(), user);
            session.setAttribute("user", updatedUser);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/profile";
    }
    
    /**
     * Process user logout
     * @param session HTTP session
     * @return Redirect to home page
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
    
    /**
     * Display users list with search functionality
     * @param keyword Search keyword
     * @param model Model for view
     * @param session HTTP session
     * @return Users list view or redirect to login
     */
    @GetMapping("/users")
    public String listUsers(@RequestParam(required = false) String keyword, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return "redirect:/login";
        }
        
        List<User> users;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Search users by keyword
            users = searchUsers(keyword.trim());
            model.addAttribute("keyword", keyword);
        } else {
            // Get all users if no search keyword
            users = userService.getAllUsers();
        }
        
        model.addAttribute("users", users);
        model.addAttribute("user", user);
        return "users";
    }
    
    /**
     * Search users by keyword
     * @param keyword Search keyword
     * @return List of matching users
     */
    private List<User> searchUsers(String keyword) {
        List<User> allUsers = userService.getAllUsers();
        String lowercaseKeyword = keyword.toLowerCase();
        
        return allUsers.stream()
            .filter(user -> 
                user.getUsername().toLowerCase().contains(lowercaseKeyword) ||
                user.getFullName().toLowerCase().contains(lowercaseKeyword) ||
                user.getEmail().toLowerCase().contains(lowercaseKeyword))
            .toList();
    }
    
    /**
     * Create initial admin user (for setup only)
     * This would typically be removed or secured in a production environment
     */
    @GetMapping("/setup-admin")
    public String setupAdmin(RedirectAttributes redirectAttributes) {
        try {
            // Check if admin already exists
            List<User> users = userService.getAllUsers();
            boolean adminExists = users.stream()
                .anyMatch(user -> "ADMIN".equals(user.getRole()));
            
            if (!adminExists) {
                User adminUser = new User(
                    "admin",
                    "admin123",
                    "admin@rentalservice.lk",
                    "System Administrator",
                    "+94 71 123 4567"
                );
                adminUser.setRole("ADMIN");
                adminUser.setNicNumber("123456789V"); // Placeholder NIC
                adminUser.setDrivingLicenseNumber("DL12345678"); // Placeholder driving license
                adminUser.setAddress("123 Admin Street, Colombo, Sri Lanka");
                
                userService.registerUser(adminUser);
                redirectAttributes.addFlashAttribute("success", "Admin user created successfully. Username: admin, Password: admin123");
            } else {
                redirectAttributes.addFlashAttribute("info", "Admin user already exists");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create admin user: " + e.getMessage());
        }
        
        return "redirect:/login";
    }
}
