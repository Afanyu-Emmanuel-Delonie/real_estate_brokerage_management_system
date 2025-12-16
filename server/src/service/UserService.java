/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import model.User;

/**
 *
 * @author afany
 */
public interface UserService extends Remote {
     public User createUser(User theUser) throws RemoteException;
    public User getUserById(User theUser) throws RemoteException;
    public User getUserByUsername(User theUser) throws RemoteException;
    public List<User> getAllUsers() throws RemoteException;
    public User updateUser(User theUser) throws RemoteException;
    public User deleteUser(User theUser) throws RemoteException;
    public User login(User theUser) throws RemoteException;
    public User changePassword(User theUser) throws RemoteException;
    public Boolean usernameExists(User theUser) throws RemoteException;
}
