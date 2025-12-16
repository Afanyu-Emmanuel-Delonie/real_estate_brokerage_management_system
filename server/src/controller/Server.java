package controller;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import service.Implementation.AgentServiceImpl;
import service.Implementation.ClientServiceImpl;
import service.Implementation.PropertyServiceImpl;
import service.Implementation.TransactionServiceImpl;
import service.Implementation.UserServiceImpl;

public class Server {
    private static final int SERVER_PORT = 1101; 
    
    public static void main(String[] args) {
        try{
            // set properties
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
            
            // create registry
            Registry registry = LocateRegistry.createRegistry(SERVER_PORT);
            
            // Register services
            registry.rebind("UserService", new UserServiceImpl());
            registry.rebind("AgentService", new AgentServiceImpl());
            registry.rebind("ClientService", new ClientServiceImpl());
            registry.rebind("PropertyService", new PropertyServiceImpl());
            registry.rebind("TransactionService", new TransactionServiceImpl());
            
            System.out.println("✓ Server is running on port " + SERVER_PORT);
            System.out.println("✓ All services registered successfully");
            System.out.println("✓ Waiting for client connections...");
            
        } catch(Exception ex) {
            System.err.println("✗ Server failed to start: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}