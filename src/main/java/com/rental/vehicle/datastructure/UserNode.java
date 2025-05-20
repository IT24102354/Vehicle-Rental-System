package com.rental.vehicle.datastructure;

import com.rental.vehicle.model.User;

/**
 * Node class for the User linked list
 * Each node contains a User object and a reference to the next node
 */
public class UserNode {
    private User user;
    private UserNode next;
    
    public UserNode(User user) {
        this.user = user;
        this.next = null;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public UserNode getNext() {
        return next;
    }
    
    public void setNext(UserNode next) {
        this.next = next;
    }
}
