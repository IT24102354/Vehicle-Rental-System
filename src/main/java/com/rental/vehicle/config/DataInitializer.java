package com.rental.vehicle.config;

import com.rental.vehicle.model.Admin;
import com.rental.vehicle.model.Bike;
import com.rental.vehicle.model.Car;
import com.rental.vehicle.model.StaffAdmin;
import com.rental.vehicle.model.SuperAdmin;
import com.rental.vehicle.model.Truck;
import com.rental.vehicle.model.User;
import com.rental.vehicle.model.Vehicle;
import com.rental.vehicle.repository.AdminRepository;
import com.rental.vehicle.repository.BikeRepository;
import com.rental.vehicle.repository.CarRepository;
import com.rental.vehicle.repository.StaffAdminRepository;
import com.rental.vehicle.repository.SuperAdminRepository;
import com.rental.vehicle.repository.TruckRepository;
import com.rental.vehicle.repository.UserRepository;
import com.rental.vehicle.repository.VehicleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Data initializer to create default users and sample vehicles on application startup
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private CarRepository carRepository;
    
    @Autowired
    private BikeRepository bikeRepository;
    
    @Autowired
    private TruckRepository truckRepository;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private SuperAdminRepository superAdminRepository;
    
    @Autowired
    private StaffAdminRepository staffAdminRepository;

    @Override
    public void run(String... args) throws Exception {
        createUsers();
        createAdmins();
        createSampleVehicles();
    }
    
    private void createUsers() {
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
        
        // Create a regular user if it doesn't exist
        if (userRepository.findByUsername("user") == null) {
            User regularUser = new User(
                "user",
                "user123",
                "user@example.com",
                "Regular User",
                "+94 71 987 6543"
            );
            regularUser.setRole("USER");
            regularUser.setNicNumber("987654321V");
            regularUser.setDrivingLicenseNumber("DL98765432");
            regularUser.setAddress("456 User Street, Colombo, Sri Lanka");
            
            userRepository.save(regularUser);
            System.out.println("Default regular user created successfully");
        }
    }
    
    /**
     * Create default admin users (SuperAdmin and StaffAdmin)
     */
    private void createAdmins() {
        // Check if super admin already exists
        Optional<Admin> existingSuperAdmin = adminRepository.findByUsername("superadmin");
        if (existingSuperAdmin.isEmpty()) {
            // Create super admin
            SuperAdmin superAdmin = new SuperAdmin();
            superAdmin.setName("Super Administrator");
            superAdmin.setEmail("superadmin@rentalservice.lk");
            superAdmin.setUsername("superadmin");
            superAdmin.setPassword("superadmin123"); // In production, this should be encrypted
            superAdmin.setRole("SUPER_ADMIN");
            superAdmin.setActive(true);
            superAdmin.setLastLoginDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            superAdmin.setCanManageAdmins(true);
            superAdmin.setCanViewReports(true);
            superAdmin.setCanConfigureSystem(true);
            
            // Add permissions
            Set<String> superAdminPermissions = new HashSet<>();
            superAdminPermissions.add("MANAGE_ADMINS");
            superAdminPermissions.add("VIEW_REPORTS");
            superAdminPermissions.add("MANAGE_SYSTEM_CONFIG");
            superAdminPermissions.add("MANAGE_USERS");
            superAdminPermissions.add("MANAGE_VEHICLES");
            superAdminPermissions.add("MANAGE_RENTALS");
            superAdmin.setPermissions(superAdminPermissions);
            
            superAdminRepository.save(superAdmin);
            System.out.println("Default super admin created successfully");
        } else {
            System.out.println("Super admin already exists");
        }
        
        // Check if staff admin already exists
        Optional<Admin> existingStaffAdmin = adminRepository.findByUsername("staffadmin");
        if (existingStaffAdmin.isEmpty()) {
            // Create staff admin
            StaffAdmin staffAdmin = new StaffAdmin();
            staffAdmin.setName("Staff Administrator");
            staffAdmin.setEmail("staffadmin@rentalservice.lk");
            staffAdmin.setUsername("staffadmin");
            staffAdmin.setPassword("staffadmin123"); // In production, this should be encrypted
            staffAdmin.setRole("STAFF_ADMIN");
            staffAdmin.setActive(true);
            staffAdmin.setLastLoginDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            staffAdmin.setDepartment("Customer Service");
            
            // Set permissions for staff admin
            staffAdmin.setCanManageUsers(true);
            staffAdmin.setCanManageVehicles(true);
            staffAdmin.setCanManageRentals(true);
            staffAdmin.setCanViewReports(true);
            
            // Add specific permissions
            Set<String> staffAdminPermissions = new HashSet<>();
            staffAdminPermissions.add("VIEW_REPORTS");
            staffAdminPermissions.add("MANAGE_USERS");
            staffAdminPermissions.add("MANAGE_VEHICLES");
            staffAdminPermissions.add("MANAGE_RENTALS");
            staffAdmin.setPermissions(staffAdminPermissions);
            
            staffAdminRepository.save(staffAdmin);
            System.out.println("Default staff admin created successfully");
        } else {
            System.out.println("Staff admin already exists");
        }
    }
    
    private void createSampleVehicles() {
        // Check if we already have vehicles
        List<Vehicle> existingVehicles = vehicleRepository.findAll();
        if (!existingVehicles.isEmpty()) {
            System.out.println("Vehicles already exist. Skipping sample vehicle creation.");
            return;
        }
        
        // Create sample cars
        Car car1 = new Car(
            "CAR-001",
            "Toyota",
            "Corolla",
            2022,
            "White",
            new BigDecimal("50.00"),
            4,  // numberOfDoors
            5   // numberOfSeats
        );
        car1.setAvailable(true);
        car1.setImageUrl("https://images.unsplash.com/photo-1590362891991-f776e747a588");
        car1.setDescription("Comfortable and fuel-efficient sedan, perfect for city driving.");
        car1.setLastMaintenanceDate(LocalDate.now().minusMonths(2));
        car1.setMileage(5000);
        car1.setFuelType("Petrol");
        car1.setTransmissionType("Automatic");
        car1.setCarType("Sedan");
        car1.setHasAirConditioning(true);
        car1.setHasNavigation(true);
        car1.setHasAutomaticTransmission(true);
        carRepository.save(car1);
        
        Car car2 = new Car(
            "CAR-002",
            "Honda",
            "CR-V",
            2021,
            "Silver",
            new BigDecimal("65.00"),
            5,  // numberOfDoors
            5   // numberOfSeats
        );
        car2.setAvailable(true);
        car2.setImageUrl("https://images.unsplash.com/photo-1533473359331-0135ef1b58bf");
        car2.setDescription("Spacious SUV with excellent safety features and ample cargo space.");
        car2.setLastMaintenanceDate(LocalDate.now().minusMonths(1));
        car2.setMileage(8000);
        car2.setFuelType("Petrol");
        car2.setTransmissionType("Automatic");
        car2.setCarType("SUV");
        car2.setHasAirConditioning(true);
        car2.setHasNavigation(true);
        car2.setHasAutomaticTransmission(true);
        carRepository.save(car2);
        
        // Create sample bikes
        Bike bike1 = new Bike();
        bike1.setRegistrationNumber("BIKE-001");
        bike1.setMake("Honda");
        bike1.setModel("CBR 250R");
        bike1.setYear(2023);
        bike1.setColor("Red");
        bike1.setDailyRate(new BigDecimal("35.00"));
        bike1.setAvailable(true);
        bike1.setImageUrl("https://images.unsplash.com/photo-1568772585407-9361f9bf3a87");
        bike1.setDescription("Sporty motorcycle with excellent handling and performance.");
        bike1.setLastMaintenanceDate(LocalDate.now().minusMonths(3));
        bike1.setMileage(2000);
        bike1.setFuelType("Petrol");
        bike1.setTransmissionType("Manual");
        bike1.setEngineCapacity(250);
        bike1.setBikeType("Sport");
        bike1.setHasABS(true);
        bike1.setHasBluetooth(false);
        bike1.setLicenseRequirement("A1");
        bikeRepository.save(bike1);
        
        Bike bike2 = new Bike();
        bike2.setRegistrationNumber("BIKE-002");
        bike2.setMake("Yamaha");
        bike2.setModel("MT-07");
        bike2.setYear(2022);
        bike2.setColor("Black");
        bike2.setDailyRate(new BigDecimal("40.00"));
        bike2.setAvailable(true);
        bike2.setImageUrl("https://images.unsplash.com/photo-1558981806-ec527fa84c39");
        bike2.setDescription("Powerful naked bike with aggressive styling and torquey engine.");
        bike2.setLastMaintenanceDate(LocalDate.now().minusMonths(2));
        bike2.setMileage(3000);
        bike2.setFuelType("Petrol");
        bike2.setTransmissionType("Manual");
        bike2.setEngineCapacity(700);
        bike2.setBikeType("Naked");
        bike2.setHasABS(true);
        bike2.setHasBluetooth(true);
        bike2.setLicenseRequirement("A");
        bikeRepository.save(bike2);
        
        // Create sample trucks
        Truck truck1 = new Truck();
        truck1.setRegistrationNumber("TRUCK-001");
        truck1.setMake("Ford");
        truck1.setModel("F-150");
        truck1.setYear(2021);
        truck1.setColor("Blue");
        truck1.setDailyRate(new BigDecimal("80.00"));
        truck1.setAvailable(true);
        truck1.setImageUrl("https://images.unsplash.com/photo-1542319637-49f9f2ef53fb");
        truck1.setDescription("Versatile pickup truck with excellent towing capacity.");
        truck1.setLastMaintenanceDate(LocalDate.now().minusMonths(4));
        truck1.setMileage(15000);
        truck1.setFuelType("Diesel");
        truck1.setTransmissionType("Automatic");
        truck1.setPayloadCapacity(1.5);
        truck1.setVolumeCapacity(2.0);
        truck1.setNumberOfAxles(2);
        truck1.setTruckType("Pickup");
        truck1.setHasTailLift(false);
        truck1.setHasRefrigeration(false);
        truck1.setLicenseRequirement("B");
        truckRepository.save(truck1);
        
        Truck truck2 = new Truck();
        truck2.setRegistrationNumber("TRUCK-002");
        truck2.setMake("Mercedes-Benz");
        truck2.setModel("Actros");
        truck2.setYear(2020);
        truck2.setColor("White");
        truck2.setDailyRate(new BigDecimal("120.00"));
        truck2.setAvailable(true);
        truck2.setImageUrl("https://images.unsplash.com/photo-1601584115197-04ecc0da31d7");
        truck2.setDescription("Heavy-duty truck for commercial transportation with spacious cabin.");
        truck2.setLastMaintenanceDate(LocalDate.now().minusMonths(6));
        truck2.setMileage(25000);
        truck2.setFuelType("Diesel");
        truck2.setTransmissionType("Automatic");
        truck2.setPayloadCapacity(20.0);
        truck2.setVolumeCapacity(40.0);
        truck2.setNumberOfAxles(3);
        truck2.setTruckType("Heavy Duty");
        truck2.setHasTailLift(true);
        truck2.setHasRefrigeration(true);
        truck2.setLicenseRequirement("C");
        truckRepository.save(truck2);
        
        System.out.println("Sample vehicles created successfully");
    }
}
