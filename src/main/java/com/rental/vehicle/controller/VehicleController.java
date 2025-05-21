package com.rental.vehicle.controller;

import com.rental.vehicle.model.Bike;
import com.rental.vehicle.model.Car;
import com.rental.vehicle.model.Truck;
import com.rental.vehicle.model.User;
import com.rental.vehicle.model.Vehicle;
import com.rental.vehicle.service.VehicleService;
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
 * Controller class for Vehicle Management
 * Handles HTTP requests related to vehicle operations
 */
@Controller
@RequestMapping("/vehicles")
public class VehicleController {
    
    /**
     * Check if the user is admin
     * @param session HTTP session
     * @return true if admin, false otherwise
     */
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN".equals(user.getRole());
    }
    
    @Autowired
    private VehicleService vehicleService;
    
    /**
     * Display all vehicles
     * @param model Model for view
     * @param keyword Search keyword
     * @param type Vehicle type filter
     * @return Vehicle listing page view
     */
    @GetMapping
    public String listVehicles(Model model, 
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String type) {
        List<Vehicle> vehicles;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            vehicles = vehicleService.searchVehicles(keyword);
            model.addAttribute("keyword", keyword);
        } else if (type != null && !type.trim().isEmpty()) {
            switch (type.toUpperCase()) {
                case "CAR":
                    vehicles = vehicleService.getAllCars();
                    break;
                case "BIKE":
                    vehicles = vehicleService.getAllBikes();
                    break;
                case "TRUCK":
                    vehicles = vehicleService.getAllTrucks();
                    break;
                default:
                    vehicles = vehicleService.getAllVehicles();
            }
            model.addAttribute("type", type);
        } else {
            vehicles = vehicleService.getAllVehicles();
        }
        
        model.addAttribute("vehicles", vehicles);
        return "vehicles/list";
    }
    
    /**
     * Display vehicle details
     * @param id Vehicle ID
     * @param model Model for view
     * @return Vehicle details page view or redirect to vehicles list
     */
    @GetMapping("/{id}")
    public String viewVehicle(@PathVariable Long id, Model model) {
        Optional<Vehicle> vehicleOpt = vehicleService.getVehicleById(id);
        
        if (vehicleOpt.isEmpty()) {
            return "redirect:/vehicles";
        }
        
        Vehicle vehicle = vehicleOpt.get();
        model.addAttribute("vehicle", vehicle);
        
        // Add vehicle type specific attributes
        if (vehicle instanceof Car) {
            model.addAttribute("vehicleType", "car");
            model.addAttribute("car", (Car) vehicle);
        } else if (vehicle instanceof Bike) {
            model.addAttribute("vehicleType", "bike");
            model.addAttribute("bike", (Bike) vehicle);
        } else if (vehicle instanceof Truck) {
            model.addAttribute("vehicleType", "truck");
            model.addAttribute("truck", (Truck) vehicle);
        }
        
        return "vehicles/view";
    }
    
    /**
     * Display form to add a new car
     * @param model Model for view
     * @return Car form view
     */
    @GetMapping("/add/car")
    public String showAddCarForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Only administrators can add vehicles");
            return "redirect:/vehicles";
        }
        model.addAttribute("car", new Car());
        model.addAttribute("formAction", "add");
        return "vehicles/car-form";
    }
    
    /**
     * Process car addition
     * @param car Car to add
     * @param result Binding result for validation
     * @param redirectAttributes Redirect attributes
     * @return Redirect to vehicles list or back to form
     */
    @PostMapping("/add/car")
    public String addCar(@Valid @ModelAttribute("car") Car car,
                        BindingResult result,
                        RedirectAttributes redirectAttributes,
                        HttpSession session) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Only administrators can add vehicles");
            return "redirect:/vehicles";
        }
        if (result.hasErrors()) {
            return "vehicles/car-form";
        }
        
        try {
            vehicleService.saveVehicle(car);
            redirectAttributes.addFlashAttribute("success", "Car added successfully");
            return "redirect:/vehicles";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vehicles/add/car";
        }
    }
    
    /**
     * Display form to add a new bike
     * @param model Model for view
     * @return Bike form view
     */
    @GetMapping("/add/bike")
    public String showAddBikeForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Only administrators can add vehicles");
            return "redirect:/vehicles";
        }
        model.addAttribute("bike", new Bike());
        model.addAttribute("formAction", "add");
        return "vehicles/bike-form";
    }
    
    /**
     * Process bike addition
     * @param bike Bike to add
     * @param result Binding result for validation
     * @param redirectAttributes Redirect attributes
     * @return Redirect to vehicles list or back to form
     */
    @PostMapping("/add/bike")
    public String addBike(@Valid @ModelAttribute("bike") Bike bike,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         HttpSession session) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Only administrators can add vehicles");
            return "redirect:/vehicles";
        }
        if (result.hasErrors()) {
            return "vehicles/bike-form";
        }
        
        try {
            vehicleService.saveVehicle(bike);
            redirectAttributes.addFlashAttribute("success", "Bike added successfully");
            return "redirect:/vehicles";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vehicles/add/bike";
        }
    }
    
    /**
     * Display form to add a new truck
     * @param model Model for view
     * @return Truck form view
     */
    @GetMapping("/add/truck")
    public String showAddTruckForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Only administrators can add vehicles");
            return "redirect:/vehicles";
        }
        model.addAttribute("truck", new Truck());
        model.addAttribute("formAction", "add");
        return "vehicles/truck-form";
    }
    
    /**
     * Process truck addition
     * @param truck Truck to add
     * @param result Binding result for validation
     * @param redirectAttributes Redirect attributes
     * @return Redirect to vehicles list or back to form
     */
    @PostMapping("/add/truck")
    public String addTruck(@Valid @ModelAttribute("truck") Truck truck,
                          BindingResult result,
                          RedirectAttributes redirectAttributes,
                          HttpSession session) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Only administrators can add vehicles");
            return "redirect:/vehicles";
        }
        if (result.hasErrors()) {
            return "vehicles/truck-form";
        }
        
        try {
            vehicleService.saveVehicle(truck);
            redirectAttributes.addFlashAttribute("success", "Truck added successfully");
            return "redirect:/vehicles";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vehicles/add/truck";
        }
    }
    
    /**
     * Display form to edit a car
     * @param id Car ID
     * @param model Model for view
     * @return Car form view or redirect to vehicles list
     */
    @GetMapping("/edit/car/{id}")
    public String showEditCarForm(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Only administrators can edit vehicles");
            return "redirect:/vehicles";
        }
        Optional<Vehicle> vehicleOpt = vehicleService.getVehicleById(id);
        
        if (vehicleOpt.isEmpty() || !(vehicleOpt.get() instanceof Car)) {
            return "redirect:/vehicles";
        }
        
        model.addAttribute("car", vehicleOpt.get());
        model.addAttribute("formAction", "edit");
        return "vehicles/car-form";
    }
    
    /**
     * Process car update
     * @param id Car ID
     * @param car Updated car details
     * @param result Binding result for validation
     * @param redirectAttributes Redirect attributes
     * @return Redirect to vehicles list or back to form
     */
    @PostMapping("/edit/car/{id}")
    public String updateCar(@PathVariable Long id,
                           @Valid @ModelAttribute("car") Car car,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           HttpSession session) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Only administrators can edit vehicles");
            return "redirect:/vehicles";
        }
        if (result.hasErrors()) {
            return "vehicles/car-form";
        }
        
        try {
            vehicleService.updateVehicle(id, car);
            redirectAttributes.addFlashAttribute("success", "Car updated successfully");
            return "redirect:/vehicles";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vehicles/edit/car/" + id;
        }
    }
    
    /**
     * Display form to edit a bike
     * @param id Bike ID
     * @param model Model for view
     * @return Bike form view or redirect to vehicles list
     */
    @GetMapping("/edit/bike/{id}")
    public String showEditBikeForm(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Only administrators can edit vehicles");
            return "redirect:/vehicles";
        }
        Optional<Vehicle> vehicleOpt = vehicleService.getVehicleById(id);
        
        if (vehicleOpt.isEmpty() || !(vehicleOpt.get() instanceof Bike)) {
            return "redirect:/vehicles";
        }
        
        model.addAttribute("bike", vehicleOpt.get());
        model.addAttribute("formAction", "edit");
        return "vehicles/bike-form";
    }
    
    /**
     * Process bike update
     * @param id Bike ID
     * @param bike Updated bike details
     * @param result Binding result for validation
     * @param redirectAttributes Redirect attributes
     * @return Redirect to vehicles list or back to form
     */
    @PostMapping("/edit/bike/{id}")
    public String updateBike(@PathVariable Long id,
                            @Valid @ModelAttribute("bike") Bike bike,
                            BindingResult result,
                            RedirectAttributes redirectAttributes,
                            HttpSession session) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Only administrators can edit vehicles");
            return "redirect:/vehicles";
        }
        if (result.hasErrors()) {
            return "vehicles/bike-form";
        }
        
        try {
            vehicleService.updateVehicle(id, bike);
            redirectAttributes.addFlashAttribute("success", "Bike updated successfully");
            return "redirect:/vehicles";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vehicles/edit/bike/" + id;
        }
    }
    
    /**
     * Display form to edit a truck
     * @param id Truck ID
     * @param model Model for view
     * @return Truck form view or redirect to vehicles list
     */
    @GetMapping("/edit/truck/{id}")
    public String showEditTruckForm(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Only administrators can edit vehicles");
            return "redirect:/vehicles";
        }
        Optional<Vehicle> vehicleOpt = vehicleService.getVehicleById(id);
        
        if (vehicleOpt.isEmpty() || !(vehicleOpt.get() instanceof Truck)) {
            return "redirect:/vehicles";
        }
        
        model.addAttribute("truck", vehicleOpt.get());
        model.addAttribute("formAction", "edit");
        return "vehicles/truck-form";
    }
    
    /**
     * Process truck update
     * @param id Truck ID
     * @param truck Updated truck details
     * @param result Binding result for validation
     * @param redirectAttributes Redirect attributes
     * @return Redirect to vehicles list or back to form
     */
    @PostMapping("/edit/truck/{id}")
    public String updateTruck(@PathVariable Long id,
                             @Valid @ModelAttribute("truck") Truck truck,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Only administrators can edit vehicles");
            return "redirect:/vehicles";
        }
        if (result.hasErrors()) {
            return "vehicles/truck-form";
        }
        
        try {
            vehicleService.updateVehicle(id, truck);
            redirectAttributes.addFlashAttribute("success", "Truck updated successfully");
            return "redirect:/vehicles";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vehicles/edit/truck/" + id;
        }
    }
    
    /**
     * Delete a vehicle
     * @param id Vehicle ID
     * @param redirectAttributes Redirect attributes
     * @return Redirect to vehicles list
     */
    @GetMapping("/delete/{id}")
    public String deleteVehicle(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        // Check if user is logged in and is an admin
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Only administrators can delete vehicles");
            return "redirect:/vehicles";
        }
        try {
            vehicleService.deleteVehicle(id);
            redirectAttributes.addFlashAttribute("success", "Vehicle deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/vehicles";
    }
    
    /**
     * Display search and filter form
     * @param model Model for view
     * @return Search form view
     */
    @GetMapping("/search")
    public String showSearchForm(Model model) {
        model.addAttribute("types", new String[]{"All", "Car", "Bike", "Truck"});
        return "vehicles/search";
    }
}
