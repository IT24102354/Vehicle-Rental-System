package com.rental.vehicle.config;

import com.rental.vehicle.model.User;
import com.rental.vehicle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Data initializer to create default admin user on application startup
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if admin already exists
        if (userRepository.findByUsername("admin") == null) {
            // Create admin user
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
            
            userRepository.save(adminUser);
            System.out.println("Default admin user created successfully");
        } else {
            System.out.println("Admin user already exists");
        }
    }
}
