package com.rental.vehicle.controller;

import com.rental.vehicle.model.User;
import com.rental.vehicle.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for admin-specific functionalities
 * Handles user management operations for admins
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    
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
     * Display admin dashboard with user management
     * @param model Model for view
     * @param session HTTP session
     * @return Admin dashboard view or redirect to login
     */
    @GetMapping("/dashboard")
    public String adminDashboard(Model model, HttpSession session) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        // Get all users
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("user", session.getAttribute("user"));
        
        return "admin/dashboard";
    }
    
    /**
     * Display user details for admin
     * @param id User ID
     * @param model Model for view
     * @param session HTTP session
     * @return User details view or redirect to admin dashboard
     */
    @GetMapping("/user/{id}")
    public String viewUser(@PathVariable Long id, Model model, HttpSession session) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            User targetUser = userService.getUserById(id);
            model.addAttribute("targetUser", targetUser);
            model.addAttribute("user", session.getAttribute("user"));
            return "admin/user-details";
        } catch (Exception e) {
            return "redirect:/admin/dashboard";
        }
    }
    
    /**
     * Delete a user
     * @param id User ID
     * @param redirectAttributes Redirect attributes
     * @param session HTTP session
     * @return Redirect to admin dashboard
     */
    @GetMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/dashboard";
    }
    
    /**
     * Display the admin settings page
     * @param model Model for view
     * @param session HTTP session
     * @return Admin settings view or redirect to login
     */
    @GetMapping("/settings")
    public String adminSettings(Model model, HttpSession session) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", session.getAttribute("user"));
        return "admin/settings";
    }
}
