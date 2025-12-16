/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import model.Client;

/**
 *
 * @author afany
 */
public interface ClientService extends Remote{
public Client createClient(Client theClient) throws RemoteException;
    public Client getClientByCode(Client theClient) throws RemoteException;
    public List<Client> getAllClients() throws RemoteException;
    public List<Client> searchClientsByName(Client theClient) throws RemoteException;
    public Client updateClient(Client theClient) throws RemoteException;
    public Client deleteClient(Client theClient) throws RemoteException;
    public Long countClients() throws RemoteException;
    public Boolean emailExists(Client theClient) throws RemoteException;
    public Boolean phoneExists(Client theClient) throws RemoteException;
}
