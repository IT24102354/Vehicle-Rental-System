package com.rental.vehicle.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * User entity class for storing user information
 * Implements CRUD functionalities for User Management component
 * Includes role-based authentication and Sri Lankan specific fields
 */
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @NotBlank(message = "Phone number is required")
    private String phone;
    
    // Role for role-based authentication
    private String role = "USER"; // Default role is USER
    
    // Sri Lankan specific fields
    @NotBlank(message = "NIC number is required")
    @Pattern(regexp = "^[0-9]{9}[vVxX]$|^[0-9]{12}$", message = "Invalid NIC format. Should be 9 digits with V/X or 12 digits")
    private String nicNumber;
    
    @NotBlank(message = "Driving license number is required")
    private String drivingLicenseNumber;
    
    private String drivingLicenseExpiry;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    private String emergencyContactName;
    
    private String emergencyContactPhone;
    
    // Default constructor
    public User() {
    }
    
    // Constructor with parameters
    public User(String username, String password, String email, String fullName, String phone, String role, 
                String nicNumber, String drivingLicenseNumber, String drivingLicenseExpiry, String address, 
                String emergencyContactName, String emergencyContactPhone) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
        this.nicNumber = nicNumber;
        this.drivingLicenseNumber = drivingLicenseNumber;
        this.drivingLicenseExpiry = drivingLicenseExpiry;
        this.address = address;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactPhone = emergencyContactPhone;
    }
    
    // Basic constructor with minimal parameters
    public User(String username, String password, String email, String fullName, String phone) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getNicNumber() {
        return nicNumber;
    }
    
    public void setNicNumber(String nicNumber) {
        this.nicNumber = nicNumber;
    }
    
    public String getDrivingLicenseNumber() {
        return drivingLicenseNumber;
    }
    
    public void setDrivingLicenseNumber(String drivingLicenseNumber) {
        this.drivingLicenseNumber = drivingLicenseNumber;
    }
    
    public String getDrivingLicenseExpiry() {
        return drivingLicenseExpiry;
    }
    
    public void setDrivingLicenseExpiry(String drivingLicenseExpiry) {
        this.drivingLicenseExpiry = drivingLicenseExpiry;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getEmergencyContactName() {
        return emergencyContactName;
    }
    
    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }
    
    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }
    
    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }
    
    /**
     * Check if the user is an admin
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", role='" + role + '\'' +
                ", nicNumber='" + nicNumber + '\'' +
                '}';
    }
}
