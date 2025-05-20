package com.rental.vehicle.datastructure;

import com.rental.vehicle.model.User;
import org.springframework.stereotype.Component;

/**
 * Custom linked list implementation for User objects
 * Implements basic operations for the linked list
 */
@Component
public class UserLinkedList {
    private UserNode head;
    private int size;
    
    public UserLinkedList() {
        this.head = null;
        this.size = 0;
    }
    
    /**
     * Add a new user to the linked list
     * @param user User to be added
     */
    public void add(User user) {
        UserNode newNode = new UserNode(user);
        
        // If list is empty, make the new node the head
        if (head == null) {
            head = newNode;
        } else {
            // Traverse to the end of the list
            UserNode current = head;
            while (current.getNext() != null) {
                current = current.getNext();
            }
            // Add the new node at the end
            current.setNext(newNode);
        }
        size++;
    }
    
    /**
     * Get a user by its username
     * @param username Username to search for
     * @return User if found, null otherwise
     */
    public User findByUsername(String username) {
        UserNode current = head;
        while (current != null) {
            if (current.getUser().getUsername().equals(username)) {
                return current.getUser();
            }
            current = current.getNext();
        }
        return null; // User not found
    }
    
    /**
     * Get a user by its id
     * @param id User ID to search for
     * @return User if found, null otherwise
     */
    public User findById(Long id) {
        if (id == null) {
            return null;
        }
        
        UserNode current = head;
        while (current != null) {
            User currentUser = current.getUser();
            if (currentUser != null && currentUser.getId() != null && 
                currentUser.getId().equals(id)) {
                return currentUser;
            }
            current = current.getNext();
        }
        return null; // User not found
    }
    
    /**
     * Update an existing user
     * @param updatedUser User with updated information
     * @return true if update was successful, false otherwise
     */
    public boolean update(User updatedUser) {
        if (updatedUser == null || updatedUser.getId() == null) {
            return false;
        }
        
        UserNode current = head;
        while (current != null) {
            User currentUser = current.getUser();
            if (currentUser != null && currentUser.getId() != null && 
                currentUser.getId().equals(updatedUser.getId())) {
                current.setUser(updatedUser);
                return true;
            }
            current = current.getNext();
        }
        return false; // User not found
    }
    
    /**
     * Delete a user by its id
     * @param id ID of the user to be deleted
     * @return true if deletion was successful, false otherwise
     */
    public boolean delete(Long id) {
        // If id is null or list is empty, return false
        if (id == null || head == null) {
            return false;
        }
        
        // If the head node is the one to delete
        User headUser = head.getUser();
        if (headUser != null && headUser.getId() != null && headUser.getId().equals(id)) {
            head = head.getNext();
            size--;
            return true;
        }
        
        // Search for the node to delete
        UserNode current = head;
        UserNode previous = null;
        
        while (current != null) {
            User currentUser = current.getUser();
            if (currentUser != null && currentUser.getId() != null && currentUser.getId().equals(id)) {
                break;
            }
            previous = current;
            current = current.getNext();
        }
        
        // If user not found or previous is null
        if (current == null || previous == null) {
            return false;
        }
        
        // Remove the node
        previous.setNext(current.getNext());
        size--;
        return true;
    }
    
    /**
     * Get all users in the linked list
     * @return Array of all users
     */
    public User[] getAllUsers() {
        User[] users = new User[size];
        UserNode current = head;
        int index = 0;
        
        while (current != null) {
            users[index++] = current.getUser();
            current = current.getNext();
        }
        
        return users;
    }
    
    /**
     * Check if a username already exists
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    public boolean usernameExists(String username) {
        return findByUsername(username) != null;
    }
    
    /**
     * Check if an email already exists
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) {
        UserNode current = head;
        while (current != null) {
            if (current.getUser().getEmail().equals(email)) {
                return true;
            }
            current = current.getNext();
        }
        return false;
    }
    
    /**
     * Get the size of the linked list
     * @return Number of users in the list
     */
    public int size() {
        return size;
    }
    
    /**
     * Check if the linked list is empty
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }
}
