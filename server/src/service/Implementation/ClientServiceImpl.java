package service.Implementation;

import dao.ClientDAO;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import model.Client;
import service.ClientService;

/**
 * Client Service Implementation
 * @author afany
 */
public class ClientServiceImpl extends UnicastRemoteObject implements ClientService {
    
    private ClientDAO dao = new ClientDAO();
    
    public ClientServiceImpl() throws RemoteException {
    }
    
    @Override
    public Client createClient(Client theClient) throws RemoteException {
        Client created = dao.createClient(theClient);
        if (created != null) {
            theClient.setClientId(created.getClientId());
            theClient.setClientCode(created.getClientCode());
            theClient.setMessage("Client created successfully");
            theClient.setStatus("SUCCESS");
            return theClient;
        } else {
            theClient.setMessage("Failed to create client");
            theClient.setStatus("ERROR");
            return theClient;
        }
    }
    
    @Override
    public Client getClientByCode(Client theClient) throws RemoteException {
        Client found = dao.getClientByCode(theClient.getClientCode());
        if (found != null) {
            theClient = found;
            theClient.setMessage("Client found");
            theClient.setStatus("SUCCESS");
        } else {
            theClient.setMessage("Client not found");
            theClient.setStatus("ERROR");
        }
        return theClient;
    }
    
    @Override
    public List<Client> getAllClients() throws RemoteException {
        return dao.getAllClients();
    }
    
    @Override
    public List<Client> searchClientsByName(Client theClient) throws RemoteException {
        return dao.searchClientsByName(theClient.getName());
    }
    
    @Override
    public Client updateClient(Client theClient) throws RemoteException {
        boolean updated = dao.updateClient(theClient);
        if (updated) {
            theClient.setMessage("Client updated successfully");
            theClient.setStatus("SUCCESS");
        } else {
            theClient.setMessage("Failed to update client");
            theClient.setStatus("ERROR");
        }
        return theClient;
    }
    
    @Override
    public Client deleteClient(Client theClient) throws RemoteException {
        boolean deleted = dao.deleteClient(theClient.getClientId());
        if (deleted) {
            theClient.setMessage("Client deleted successfully");
            theClient.setStatus("SUCCESS");
        } else {
            theClient.setMessage("Failed to delete client");
            theClient.setStatus("ERROR");
        }
        return theClient;
    }
    
    @Override
    public Long countClients() throws RemoteException {
        return dao.countClients();
    }

    @Override
    public Boolean emailExists(Client theClient) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Boolean phoneExists(Client theClient) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}