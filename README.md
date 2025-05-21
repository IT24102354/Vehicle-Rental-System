# Vehicle Rental System

A comprehensive online platform that enables users to rent vehicles from anywhere in Sri Lanka.

## Project Overview

The Vehicle Rental System is a web-based application built using Spring Boot that simplifies the vehicle rental process. The system is designed to connect vehicle owners with potential renters, providing a secure, efficient, and user-friendly platform for vehicle rentals.

## Features

### Implemented Features

- **User Management**
  - User registration and authentication
  - Role-based access control (Admin and User roles)
  - User profile management
  - Sri Lankan NIC and driving license validation
  - Secure password handling

### Planned Features

- **Vehicle Management**
  - Vehicle listing and categorization
  - Vehicle availability tracking
  - Vehicle details and specifications
  - Vehicle search and filtering

- **Booking System**
  - Booking creation and management
  - Date-based availability checking
  - Booking confirmation workflow
  - Booking history

- **Payment Integration**
  - Multiple payment methods
  - Secure payment processing
  - Receipt generation

- **Reviews and Ratings**
  - User feedback system
  - Vehicle and service ratings

## Technical Details

- **Backend**: Spring Boot, Spring MVC, Spring Data JPA
- **Frontend**: Thymeleaf, HTML, CSS, JavaScript
- **Database**: MySQL/PostgreSQL
- **Security**: Session-based authentication
- **Data Structures**: Custom implementations for optimized data operations

## Getting Started

### Prerequisites

- Java 17 or later
- Maven
- MySQL or PostgreSQL

### Running the Application

1. Clone the repository
2. Configure the database connection in `application.properties`
3. Run `mvn spring-boot:run`
4. Access the application at `http://localhost:8080`

## Project Status

This project is currently in active development. The User Management component has been completed, and work is ongoing for the remaining features.
