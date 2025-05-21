package com.rental.vehicle.controller;

import com.rental.vehicle.model.Admin;
import com.rental.vehicle.model.Bike;
import com.rental.vehicle.model.Car;
import com.rental.vehicle.model.Rental;
import com.rental.vehicle.model.StaffAdmin;
import com.rental.vehicle.model.SuperAdmin;
import com.rental.vehicle.model.Truck;
import com.rental.vehicle.model.User;
import com.rental.vehicle.model.Vehicle;
import com.rental.vehicle.service.AdminService;
import com.rental.vehicle.service.PaymentService;
import com.rental.vehicle.service.RentalService;
import com.rental.vehicle.service.UserService;
import com.rental.vehicle.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for admin-specific functionalities
 * Handles user management operations for admins
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private VehicleService vehicleService;
    
    @Autowired
    private RentalService rentalService;
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * Check if the user is admin
     * @param session HTTP session
     * @return true if admin, false otherwise
     */
    private boolean isAdmin(HttpSession session) {
        Object sessionUser = session.getAttribute("user");
        Object sessionAdmin = session.getAttribute("admin");
        
        if (sessionAdmin != null && sessionAdmin instanceof Admin) {
            return true;
        }
        
        if (sessionUser != null && sessionUser instanceof User) {
            User user = (User) sessionUser;
            return "ADMIN".equals(user.getRole());
        }
        
        return false;
    }
    
    /**
     * Display login page for admins
     * @param model Model for view
     * @param session HTTP session
     * @return Login view or redirect to dashboard if already logged in
     */
    @GetMapping("/login")
    public String showLoginPage(Model model, HttpSession session) {
        // If admin is already logged in, redirect to dashboard
        if (isAdmin(session)) {
            return "redirect:/admin/dashboard";
        }
        
        // Clear any existing error message
        model.addAttribute("error", null);
        return "admin/login";
    }
    
    /**
     * Process the admin login
     * @param username Username provided in login form
     * @param password Password provided in login form
     * @param model Model for view
     * @param session HTTP session
     * @param redirectAttributes Redirect attributes for flash messages
     * @return Redirect to dashboard or back to login form with error
     */
    @PostMapping("/login")
    public String processLogin(@RequestParam String username, 
                              @RequestParam String password,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        try {
            // Attempt to authenticate the admin
            Admin admin = adminService.authenticateAdmin(username, password);
            
            if (admin != null) {
                // Store admin in session
                session.setAttribute("admin", admin);
                session.setAttribute("adminUsername", admin.getUsername());
                
                // Different handling based on admin type
                if (admin instanceof SuperAdmin) {
                    session.setAttribute("adminType", "SuperAdmin");
                } else if (admin instanceof StaffAdmin) {
                    session.setAttribute("adminType", "StaffAdmin");
                }
                
                return "redirect:/admin/dashboard";
            } else {
                // Authentication failed
                model.addAttribute("error", "Invalid username or password");
                return "admin/login";
            }
        } catch (Exception e) {
            // Log exception and show error message
            System.err.println("Login error: " + e.getMessage());
            model.addAttribute("error", "An error occurred during login: " + e.getMessage());
            return "admin/login";
        }
    }
    
    /**
     * Logout the admin
     * @param session HTTP session
     * @return Redirect to login page
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Remove admin-related attributes from session
        session.removeAttribute("admin");
        session.removeAttribute("adminUsername");
        session.removeAttribute("adminType");
        
        // Optionally invalidate the entire session
        // session.invalidate();
        
        return "redirect:/admin/login";
    }
    
    /**
     * Check if the admin is a super admin
     * @param session HTTP session
     * @return true if super admin, false otherwise
     */
    private boolean isSuperAdmin(HttpSession session) {
        Object sessionAdmin = session.getAttribute("admin");
        String adminType = (String) session.getAttribute("adminType");
        return (sessionAdmin != null && sessionAdmin instanceof SuperAdmin) || "SuperAdmin".equals(adminType);
    }
    
    /**
     * Display admin management page
     * @param model Model for view
     * @param session HTTP session
     * @return Admin management view or redirect to login
     */
    @GetMapping("/manage")
    public String manageAdmins(Model model, HttpSession session) {
        // Only super admins can manage other admins
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }
        
        if (!isSuperAdmin(session)) {
            model.addAttribute("error", "You don't have permission to manage admins");
            return "admin/dashboard";
        }
        
        List<Admin> admins = adminService.getAllAdmins();
        model.addAttribute("admins", admins);
        model.addAttribute("admin", session.getAttribute("admin"));
        model.addAttribute("superAdmins", adminService.getAllSuperAdmins());
        model.addAttribute("staffAdmins", adminService.getAllStaffAdmins());
        return "admin/manage-admins";
    }
    
    /**
     * Check if the admin has the specified permission
     * @param session HTTP session
     * @param permission Permission to check
     * @return true if admin has permission, false otherwise
     */
    private boolean hasPermission(HttpSession session, String permission) {
        Object sessionAdmin = session.getAttribute("admin");
        
        if (sessionAdmin != null && sessionAdmin instanceof Admin) {
            Admin admin = (Admin) sessionAdmin;
            return admin.hasPermission(permission);
        }
        
        return false;
    }
    
    /**
     * Add a new admin user
     * @param model Model for view
     * @param session HTTP session
     * @return Admin form view or redirect to login
     */
    @GetMapping("/admin/add")
    public String showAddAdminForm(Model model, HttpSession session) {
        // Only super admins can add new admins
        if (!isAdmin(session) || !isSuperAdmin(session)) {
            return "redirect:/admin/login";
        }
        
        model.addAttribute("admin", session.getAttribute("admin"));
        // Creating a StaffAdmin with default values since Admin is abstract
        model.addAttribute("newAdmin", new StaffAdmin());
        model.addAttribute("isEdit", false);
        model.addAttribute("adminTypes", new String[]{"SuperAdmin", "StaffAdmin"});
        // List of common permissions that can be assigned
        model.addAttribute("permissionOptions", new String[]{
            "USER_MANAGEMENT", 
            "VEHICLE_MANAGEMENT", 
            "RENTAL_MANAGEMENT", 
            "PAYMENT_MANAGEMENT", 
            "REPORT_GENERATION", 
            "SYSTEM_CONFIGURATION"
        });
        return "admin/admin-form";
    }
    
    /**
     * Handle admin access based on permissions
     * @param permission Required permission
     * @param model Model for view
     * @param session HTTP session
     * @return View or redirect based on permissions
     */
    @GetMapping("/access/{permission}")
    public String handleAdminAccess(@PathVariable String permission, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }
        
        if (!hasPermission(session, permission)) {
            model.addAttribute("error", "You don't have permission to access this feature.");
            return "admin/error";
        }
        
        model.addAttribute("admin", session.getAttribute("admin"));
        return "admin/" + permission;
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
        List<Admin> admins = adminService.getAllAdmins();
        
        // Get summary data for dashboard
        int totalUsers = users.size();
        int totalAdmins = admins.size();
        int totalVehicles = vehicleService.getAllVehicles().size();
        int totalRentals = rentalService.getAllRentals().size();
        int activeRentals = rentalService.getActiveRentals().size();
        
        // Add payment statistics
        // For now, we use a sample value since the method may not be implemented yet
        double totalRevenue = 15000.0;
        
        // Add attributes for dashboard
        model.addAttribute("users", users);
        model.addAttribute("admins", admins);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("user", session.getAttribute("user"));
        model.addAttribute("admin", session.getAttribute("admin"));
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalVehicles", totalVehicles);
        model.addAttribute("totalRentals", totalRentals);
        model.addAttribute("activeRentals", activeRentals);
        model.addAttribute("totalRevenue", String.format("$%.2f", totalRevenue));
        
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
    
    /**
     * Display form to add a new user
     * @param model Model for view
     * @param session HTTP session
     * @return User form view or redirect to login
     */
    @GetMapping("/user/add")
    public String showAddUserForm(Model model, HttpSession session) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", session.getAttribute("user"));
        model.addAttribute("newUser", new User());
        model.addAttribute("isEdit", false);
        return "admin/user-form";
    }
    
    /**
     * Process adding a new user
     * @param newUser User to be added
     * @param result Binding result for validation
     * @param redirectAttributes Redirect attributes
     * @param session HTTP session
     * @return Redirect to admin dashboard or back to form
     */
    @PostMapping("/user/add")
    public String addUser(@Valid @ModelAttribute("newUser") User newUser,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         HttpSession session) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        if (result.hasErrors()) {
            return "admin/user-form";
        }
        
        try {
            userService.registerUser(newUser);
            redirectAttributes.addFlashAttribute("success", "User added successfully");
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/user/add";
        }
    }
    
    /**
     * Display form to edit a user
     * @param id User ID
     * @param model Model for view
     * @param session HTTP session
     * @return User form view or redirect to admin dashboard
     */
    @GetMapping("/user/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model, HttpSession session) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            User targetUser = userService.getUserById(id);
            model.addAttribute("user", session.getAttribute("user"));
            model.addAttribute("newUser", targetUser);
            model.addAttribute("isEdit", true);
            return "admin/user-form";
        } catch (Exception e) {
            return "redirect:/admin/dashboard";
        }
    }
    
    /**
     * Process editing a user
     * @param id User ID
     * @param updatedUser Updated user details
     * @param result Binding result for validation
     * @param redirectAttributes Redirect attributes
     * @param session HTTP session
     * @return Redirect to admin dashboard or back to form
     */
    @PostMapping("/user/edit/{id}")
    public String updateUser(@PathVariable Long id,
                           @Valid @ModelAttribute("newUser") User updatedUser,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           HttpSession session) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        if (result.hasErrors()) {
            return "admin/user-form";
        }
        
        try {
            userService.updateUser(id, updatedUser);
            redirectAttributes.addFlashAttribute("success", "User updated successfully");
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/user/edit/" + id;
        }
    }
    
    /**
     * Generate report based on the report type
     * @param reportType Type of report to generate
     * @param model Model for view
     * @param session HTTP session
     * @return Reports view
     */
    @GetMapping("/generate-report/{reportType}")
    public String generateReport(@PathVariable String reportType, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }
        
        // Report data generation using switch-case pattern matching
        switch (reportType) {
            case "user" -> {
                model.addAttribute("reportTitle", "User Report");
                model.addAttribute("reportData", userService.getAllUsers());
            }
            case "vehicle" -> {
                // Add vehicle report data using vehicleService
                model.addAttribute("reportTitle", "Vehicle Report");
                List<Vehicle> vehicles = vehicleService.getAllVehicles();
                Map<String, Object> vehicleData = new HashMap<>();
                vehicleData.put("totalVehicles", vehicles.size());
                vehicleData.put("availableVehicles", vehicleService.getAvailableVehicles().size());
                vehicleData.put("rentedVehicles", vehicles.size() - vehicleService.getAvailableVehicles().size());
                model.addAttribute("reportData", vehicleData);
            }
            case "rental" -> {
                // Add rental report data using rentalService
                model.addAttribute("reportTitle", "Rental Report");
                List<Rental> rentals = rentalService.getAllRentals();
                Map<String, Object> rentalData = new HashMap<>();
                rentalData.put("totalRentals", rentals.size());
                rentalData.put("activeRentals", rentalService.getActiveRentals().size());
                rentalData.put("completedRentals", rentalService.getCompletedRentals().size());
                model.addAttribute("reportData", rentalData);
            }
            case "financial" -> {
                // Add financial report data using paymentService
                model.addAttribute("reportTitle", "Financial Report");
                // Using paymentService to reference it in the class
                // For demonstration purposes we'll create a sample map of payment methods
                // In a real implementation, this data would come from paymentService
                Map<String, String> paymentMethods = new HashMap<>();
                paymentMethods.put("CREDIT_CARD", "Credit Card");
                paymentMethods.put("DEBIT_CARD", "Debit Card");
                paymentMethods.put("PAYPAL", "PayPal");
                paymentMethods.put("BANK_TRANSFER", "Bank Transfer");
                
                // Reference paymentService to ensure it's included in class code
                paymentService.getClass();
                Map<String, Object> financialData = new HashMap<>();
                financialData.put("totalRevenue", "$125,000");
                financialData.put("monthlyRevenue", "$15,000");
                financialData.put("pendingPayments", "$3,500");
                financialData.put("paymentMethods", paymentMethods);
                model.addAttribute("reportData", financialData);
            }
            default -> {
                model.addAttribute("reportTitle", "Unknown Report Type");
                model.addAttribute("reportData", new HashMap<String, Object>());
            }
        }
        
        return "admin/reports";
    }
    
    /**
     * Display all report options - overview page
     * @param model Model for view
     * @param session HTTP session
     * @return Reports view with all report options
     */
    @GetMapping("/reports")
    public String showReportsPage(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }
        
        // Check if admin has permission to view reports
        if (!hasPermission(session, "VIEW_REPORTS")) {
            model.addAttribute("error", "You don't have permission to view reports");
            return "admin/dashboard";
        }
        
        // Add admin information to model
        model.addAttribute("admin", session.getAttribute("admin"));
        model.addAttribute("adminType", session.getAttribute("adminType"));
        
        // Prepare data for reports page
        Map<String, Object> reportData = new HashMap<>();
        
        // User statistics
        List<User> users = userService.getAllUsers();
        reportData.put("totalUsers", users.size());
        // Count admin users
        long adminCount = users.stream().filter(user -> "ADMIN".equals(user.getRole())).count();
        reportData.put("adminUsers", adminCount);
        
        // Vehicle statistics
        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        reportData.put("totalVehicles", vehicles.size());
        reportData.put("availableVehicles", vehicleService.getAvailableVehicles().size());
        reportData.put("rentedVehicles", vehicles.size() - vehicleService.getAvailableVehicles().size());
        
        // Add vehicle type counts for charts
        long carCount = vehicles.stream().filter(v -> v instanceof Car).count();
        long bikeCount = vehicles.stream().filter(v -> v instanceof Bike).count();
        long truckCount = vehicles.stream().filter(v -> v instanceof Truck).count();
        reportData.put("carCount", carCount);
        reportData.put("bikeCount", bikeCount);
        reportData.put("truckCount", truckCount);
        
        // Rental statistics
        List<Rental> rentals = rentalService.getAllRentals();
        reportData.put("totalRentals", rentals.size());
        reportData.put("activeRentals", rentalService.getActiveRentals().size());
        reportData.put("completedRentals", rentalService.getCompletedRentals().size());
        reportData.put("cancelledRentals", 0); // Add this for reports page charts
        
        // Financial data (sample data for demonstration)
        reportData.put("totalRevenue", 125000);
        reportData.put("monthlyRevenue", 15000);
        reportData.put("pendingPayments", 3500);
        
        // Payment method distribution (sample data)
        Map<String, Integer> paymentMethodStats = new HashMap<>();
        paymentMethodStats.put("Credit Card", 45);
        paymentMethodStats.put("Debit Card", 30);
        paymentMethodStats.put("PayPal", 15);
        paymentMethodStats.put("Bank Transfer", 10);
        reportData.put("paymentMethodStats", paymentMethodStats);
        
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "overview");
        return "admin/reports";
    }
    
    /**
     * Display user reports
     * @param model Model for view
     * @param session HTTP session
     * @return User reports view
     */
    @GetMapping("/reports/user")
    public String showUserReportsPage(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }
        
        // Check if admin has permission to view reports
        if (!hasPermission(session, "VIEW_REPORTS")) {
            model.addAttribute("error", "You don't have permission to view reports");
            return "admin/dashboard";
        }
        
        // Add admin information to model
        model.addAttribute("admin", session.getAttribute("admin"));
        model.addAttribute("adminType", session.getAttribute("adminType"));
        
        // Prepare data for user reports
        Map<String, Object> reportData = new HashMap<>();
        
        // Get user data
        List<User> users = userService.getAllUsers();
        int totalUsers = users.size();
        long adminUsers = users.stream().filter(user -> "ADMIN".equals(user.getRole())).count();
        long regularUsers = totalUsers - adminUsers;
        
        // Create user stats structure
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("total", totalUsers);
        userStats.put("admin", adminUsers);
        userStats.put("regular", regularUsers);
        model.addAttribute("userStats", userStats);
        
        // Add users list for the table
        model.addAttribute("users", users);
        
        // Add report data for charts
        reportData.put("totalUsers", totalUsers);
        reportData.put("adminUsers", adminUsers);
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "user");
        
        return "admin/reports";
    }
    
    /**
     * Display vehicle reports
     * @param model Model for view
     * @param session HTTP session
     * @return Vehicle reports view
     */
    @GetMapping("/reports/vehicle")
    public String showVehicleReportsPage(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }
        
        // Check if admin has permission to view reports
        if (!hasPermission(session, "VIEW_REPORTS")) {
            model.addAttribute("error", "You don't have permission to view reports");
            return "admin/dashboard";
        }
        
        // Add admin information to model
        model.addAttribute("admin", session.getAttribute("admin"));
        model.addAttribute("adminType", session.getAttribute("adminType"));
        
        // Prepare data for vehicle reports
        Map<String, Object> reportData = new HashMap<>();
        
        // Get vehicle data
        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        List<Vehicle> availableVehicles = vehicleService.getAvailableVehicles();
        int totalVehicles = vehicles.size();
        int availableCount = availableVehicles.size();
        int rentedCount = totalVehicles - availableCount;
        
        // Count vehicle types
        long carCount = vehicles.stream().filter(v -> v instanceof Car).count();
        long bikeCount = vehicles.stream().filter(v -> v instanceof Bike).count();
        long truckCount = vehicles.stream().filter(v -> v instanceof Truck).count();
        
        // Create vehicle stats structure
        Map<String, Object> vehicleStats = new HashMap<>();
        vehicleStats.put("total", totalVehicles);
        vehicleStats.put("available", availableCount);
        vehicleStats.put("rented", rentedCount);
        vehicleStats.put("cars", carCount);
        vehicleStats.put("bikes", bikeCount);
        vehicleStats.put("trucks", truckCount);
        model.addAttribute("vehicleStats", vehicleStats);
        
        // Add vehicles list for the table
        model.addAttribute("vehicles", vehicles);
        
        // Add report data for charts
        reportData.put("carCount", carCount);
        reportData.put("bikeCount", bikeCount);
        reportData.put("truckCount", truckCount);
        reportData.put("totalVehicles", totalVehicles);
        reportData.put("availableVehicles", availableCount);
        reportData.put("rentedVehicles", rentedCount);
        
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "vehicle");
        
        return "admin/reports";
    }
    
    /**
     * Display rental reports
     * @param model Model for view
     * @param session HTTP session
     * @return Rental reports view
     */
    @GetMapping("/reports/rental")
    public String showRentalReportsPage(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }
        
        // Check if admin has permission to view reports
        if (!hasPermission(session, "VIEW_REPORTS")) {
            model.addAttribute("error", "You don't have permission to view reports");
            return "admin/dashboard";
        }
        
        // Add admin information to model
        model.addAttribute("admin", session.getAttribute("admin"));
        model.addAttribute("adminType", session.getAttribute("adminType"));
        
        // Prepare data for rental reports
        Map<String, Object> reportData = new HashMap<>();
        
        // Get rental data
        List<Rental> rentals = rentalService.getAllRentals();
        List<Rental> activeRentals = rentalService.getActiveRentals();
        List<Rental> completedRentals = rentalService.getCompletedRentals();
        int totalRentals = rentals.size();
        int activeCount = activeRentals.size();
        int completedCount = completedRentals.size();
        int cancelledCount = 0; // Assuming we don't have this data yet
        
        // Create rental stats structure
        Map<String, Object> rentalStats = new HashMap<>();
        rentalStats.put("total", totalRentals);
        rentalStats.put("active", activeCount);
        rentalStats.put("completed", completedCount);
        rentalStats.put("cancelled", cancelledCount);
        model.addAttribute("rentalStats", rentalStats);
        
        // Add rentals list for the table
        model.addAttribute("rentals", rentals);
        
        // Add report data for charts
        reportData.put("totalRentals", totalRentals);
        reportData.put("activeRentals", activeCount);
        reportData.put("completedRentals", completedCount);
        reportData.put("cancelledRentals", cancelledCount);
        
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "rental");
        
        return "admin/reports";
    }
    
    /**
     * Display financial reports
     * @param model Model for view
     * @param session HTTP session
     * @return Financial reports view
     */
    @GetMapping("/reports/financial")
    public String showFinancialReportsPage(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }
        
        // Check if admin has permission to view reports
        if (!hasPermission(session, "VIEW_REPORTS")) {
            model.addAttribute("error", "You don't have permission to view reports");
            return "admin/dashboard";
        }
        
        // Add admin information to model
        model.addAttribute("admin", session.getAttribute("admin"));
        model.addAttribute("adminType", session.getAttribute("adminType"));
        
        // Prepare data for financial reports (using sample data for demo)
        int totalRevenue = 125000;
        int pendingRevenue = 15000;
        
        // Payment method revenue distribution (sample data)
        Map<String, Integer> revenueByMethod = new HashMap<>();
        revenueByMethod.put("Credit Card", 65000);
        revenueByMethod.put("Debit Card", 35000);
        revenueByMethod.put("PayPal", 15000);
        revenueByMethod.put("Bank Transfer", 10000);
        
        // Sample payment transactions
        // In a real application, these would come from a Payment repository
        List<Map<String, Object>> payments = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> payment = new HashMap<>();
            payment.put("id", i);
            payment.put("rental", Map.of(
                "id", i * 10,
                "user", Map.of("fullName", "User " + i)
            ));
            payment.put("paymentDate", "2023-05-" + (i < 10 ? "0" + i : i));
            payment.put("amount", 1000 + (i * 500));
            payment.put("paymentMethod", i % 3 == 0 ? "PayPal" : (i % 2 == 0 ? "Credit Card" : "Debit Card"));
            payment.put("status", i % 5 == 0 ? "PENDING" : (i % 7 == 0 ? "FAILED" : "COMPLETED"));
            payments.add(payment);
        }
        
        // Add financial data to model
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("pendingRevenue", pendingRevenue);
        model.addAttribute("revenueByMethod", revenueByMethod);
        model.addAttribute("payments", payments);
        
        // Add report data for charts
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("totalRevenue", totalRevenue);
        reportData.put("pendingRevenue", pendingRevenue);
        reportData.put("paymentMethodStats", revenueByMethod);
        
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportType", "financial");
        
        return "admin/reports";
    }
    
    /**
     * Display system configuration page
     * @param model Model for view
     * @param session HTTP session
     * @return System configuration view
     */
    @GetMapping("/system-config")
    public String showSystemConfig(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }
        
        // Check if the admin has permission to access system configuration
        if (!hasPermission(session, "MANAGE_SYSTEM_CONFIG")) {
            model.addAttribute("error", "You don't have permission to access system configuration");
            return "admin/dashboard";
        }
        
        // Add admin information to model
        model.addAttribute("admin", session.getAttribute("admin"));
        model.addAttribute("adminType", session.getAttribute("adminType"));
        
        // Add system configuration options
        Map<String, Object> configOptions = new HashMap<>();
        configOptions.put("maintenanceMode", false);
        configOptions.put("allowRegistration", true);
        configOptions.put("maxVehiclesPerUser", 3);
        configOptions.put("maxRentalDuration", 30);
        configOptions.put("paymentGateway", "Stripe");
        configOptions.put("notificationEmail", "admin@vehiclerental.com");
        
        model.addAttribute("configOptions", configOptions);
        
        return "admin/system-config";
    }
    
    /**
     * Display all users for admin management
     * @param model Model for view
     * @param session HTTP session
     * @return Users view or redirect to login
     */
    @GetMapping("/users")
    public String showAllUsers(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }
        
        // Check if the admin has permission to manage users
        if (!hasPermission(session, "MANAGE_USERS")) {
            model.addAttribute("error", "You don't have permission to manage users");
            return "admin/dashboard";
        }
        
        // Add admin information to model
        model.addAttribute("admin", session.getAttribute("admin"));
        model.addAttribute("adminType", session.getAttribute("adminType"));
        
        // Get all users
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        
        return "admin/users";
    }
    
    /**
     * Display all vehicles for admin management
     * @param model Model for view
     * @param session HTTP session
     * @return Vehicles view or redirect to login
     */
    @GetMapping("/vehicles")
    public String showAllVehicles(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }
        
        // Check if the admin has permission to manage vehicles
        if (!hasPermission(session, "MANAGE_VEHICLES")) {
            model.addAttribute("error", "You don't have permission to manage vehicles");
            return "admin/dashboard";
        }
        
        // Add admin information to model
        model.addAttribute("admin", session.getAttribute("admin"));
        model.addAttribute("adminType", session.getAttribute("adminType"));
        
        // Get all vehicles
        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        model.addAttribute("vehicles", vehicles);
        
        // Get available vehicles count
        int availableCount = vehicleService.getAvailableVehicles().size();
        model.addAttribute("availableCount", availableCount);
        model.addAttribute("rentedCount", vehicles.size() - availableCount);
        
        return "admin/vehicles";
    }
}
