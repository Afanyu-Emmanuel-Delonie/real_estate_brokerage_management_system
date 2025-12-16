/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.Implementation;

import dao.UserDAO;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import model.User;
import service.UserService;

/**
 *
 * @author afany
 */
public class UserServiceImpl extends UnicastRemoteObject implements UserService{
    
       private UserDAO dao = new UserDAO();
    
    public UserServiceImpl() throws RemoteException {
    }
    
    @Override
    public User createUser(User theUser) throws RemoteException {
        User created = dao.createUser(theUser);
        if (created != null) {
            theUser.setUserId(created.getUserId());
            theUser.setMessage("User created successfully");
            theUser.setStatus("SUCCESS");
            return theUser;
        } else {
            theUser.setMessage("Failed to create user");
            theUser.setStatus("ERROR");
            return theUser;
        }
    }
    
    @Override
    public User getUserById(User theUser) throws RemoteException {
        User found = dao.getUserById(theUser.getUserId());
        if (found != null) {
            theUser = found;
            theUser.setMessage("User found");
            theUser.setStatus("SUCCESS");
        } else {
            theUser.setMessage("User not found");
            theUser.setStatus("ERROR");
        }
        return theUser;
    }
    
    @Override
    public User getUserByUsername(User theUser) throws RemoteException {
        User found = dao.getUserByUsername(theUser.getUsername());
        if (found != null) {
            theUser = found;
            theUser.setMessage("User found");
            theUser.setStatus("SUCCESS");
        } else {
            theUser.setMessage("User not found");
            theUser.setStatus("ERROR");
        }
        return theUser;
    }
    
    @Override
    public List<User> getAllUsers() throws RemoteException {
        return dao.getAllUsers();
    }
    
    @Override
    public User updateUser(User theUser) throws RemoteException {
        boolean updated = dao.updateUser(theUser);
        if (updated) {
            theUser.setMessage("User updated successfully");
            theUser.setStatus("SUCCESS");
        } else {
            theUser.setMessage("Failed to update user");
            theUser.setStatus("ERROR");
        }
        return theUser;
    }
    
    @Override
    public User deleteUser(User theUser) throws RemoteException {
        boolean deleted = dao.deleteUser(theUser.getUserId());
        if (deleted) {
            theUser.setMessage("User deleted successfully");
            theUser.setStatus("SUCCESS");
        } else {
            theUser.setMessage("Failed to delete user");
            theUser.setStatus("ERROR");
        }
        return theUser;
    }
    
    @Override
    public User login(User theUser) throws RemoteException {
        User authenticated = dao.login(theUser.getUsername(), theUser.getPassword());
        if (authenticated != null) {
            theUser = authenticated;
            theUser.setMessage("Login successful");
            theUser.setStatus("SUCCESS");
        } else {
            theUser.setMessage("Invalid credentials");
            theUser.setStatus("ERROR");
        }
        return theUser;
    }
    
    @Override
    public User changePassword(User theUser) throws RemoteException {
        // Assuming theUser has oldPassword and newPassword fields
        boolean changed = dao.changePassword(theUser.getUserId(), 
                                           theUser.getPassword());
        if (changed) {
            theUser.setMessage("Password changed successfully");
            theUser.setStatus("SUCCESS");
        } else {
            theUser.setMessage("Failed to change password");
            theUser.setStatus("ERROR");
        }
        return theUser;
    }
    
    @Override
    public Boolean usernameExists(User theUser) throws RemoteException {
        return dao.usernameExists(theUser.getUsername());
    }
}
