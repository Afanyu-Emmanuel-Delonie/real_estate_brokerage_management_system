package dao;

import java.util.List;
import model.Client;
import org.hibernate.*;

public class ClientDAO {

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String trimmedEmail = email.trim();

        int atCount = 0;
        for (char c : trimmedEmail.toCharArray()) {
            if (c == '@') atCount++;
        }
        if (atCount != 1) {
            System.out.println("Email must contain exactly one @ symbol");
            return false;
        }

        if (trimmedEmail.contains("..")) {
            System.out.println("Email cannot contain consecutive dots");
            return false;
        }

        if (trimmedEmail.startsWith(".") || trimmedEmail.startsWith("@") ||
                trimmedEmail.endsWith(".") || trimmedEmail.endsWith("@")) {
            System.out.println("Email cannot start or end with . or @");
            return false;
        }

        String localPart = trimmedEmail.substring(0, trimmedEmail.indexOf('@'));
        if (localPart.isEmpty()) {
            System.out.println("Email must have a username before @");
            return false;
        }

        String localPartRegex = "^[A-Za-z0-9._-]+$";
        if (!localPart.matches(localPartRegex)) {
            System.out.println("Email username can only contain letters, numbers, dots, underscores, and hyphens");
            return false;
        }

        return true;
    }

    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            System.out.println("Phone number cannot be empty");
            return false;
        }

        String cleanPhone = phone.replaceAll("[\\s()-]", "");

        if (cleanPhone.length() < 10) {
            System.out.println("Phone number must be at least 10 digits");
            return false;
        }

        if (!cleanPhone.matches("^\\+?[0-9]+$")) {
            System.out.println("Phone number can only contain digits and optional + prefix");
            return false;
        }

        return true;
    }

    private boolean isValidClient(Client client) {
        if (client == null) {
            System.out.println("Client object is null");
            return false;
        }

        if (client.getName() == null || client.getName().trim().isEmpty()) {
            System.out.println("Client name cannot be empty");
            return false;
        }

        if (client.getName().length() < 2) {
            System.out.println("Client name must be at least 2 characters");
            return false;
        }

        if (!isValidEmail(client.getEmail())) {
            return false;
        }

        if (!isValidPhone(client.getPhone())) {
            return false;
        }

        return true;
    }

    private boolean emailExists(String email, Long excludeClientId) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query;
            if (excludeClientId != null) {
                query = session.createQuery(
                    "SELECT COUNT(*) FROM Client WHERE LOWER(email) = LOWER(:email) AND clientId != :id");
                query.setParameter("email", email.trim());
                query.setParameter("id", excludeClientId);
            } else {
                query = session.createQuery("SELECT COUNT(*) FROM Client WHERE LOWER(email) = LOWER(:email)");
                query.setParameter("email", email.trim());
            }
            Long count = (Long) query.uniqueResult();
            return count > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private boolean phoneExists(String phone, Long excludeClientId) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query;
            if (excludeClientId != null) {
                query = session.createQuery(
                    "SELECT COUNT(*) FROM Client WHERE phone = :phone AND clientId != :id");
                query.setParameter("phone", phone.trim());
                query.setParameter("id", excludeClientId);
            } else {
                query = session.createQuery("SELECT COUNT(*) FROM Client WHERE phone = :phone");
                query.setParameter("phone", phone.trim());
            }
            Long count = (Long) query.uniqueResult();
            return count > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private String generateNextClientCode() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();

            Query query = session.createQuery(
                    "SELECT c.clientCode FROM Client c WHERE c.clientCode LIKE 'CL%' ORDER BY c.clientCode DESC"
            );
            query.setMaxResults(1);

            List<String> results = query.list();

            if (results.isEmpty()) {
                return "CL001";
            } else {
                String lastClientCode = results.get(0);
                try {
                    int lastNumber = Integer.parseInt(lastClientCode.substring(2));
                    int nextNumber = lastNumber + 1;
                    return String.format("CL%03d", nextNumber);
                } catch (Exception e) {
                    System.out.println("Error parsing client code, starting from CL001");
                    return "CL001";
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "CL001";
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public Client createClient(Client client) {
        if (!isValidClient(client)) {
            System.out.println("Validation failed: Invalid client data");
            return null;
        }

        if (emailExists(client.getEmail(), null)) {
            System.out.println("Error: Email already exists");
            return null;
        }

        if (phoneExists(client.getPhone(), null)) {
            System.out.println("Error: Phone number already exists");
            return null;
        }

        String clientCode = generateNextClientCode();
        if (clientCode == null) {
            System.out.println("Error: Could not generate client code");
            return null;
        }
        client.setClientCode(clientCode);

        Session session = null;
        Transaction dbTransaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            dbTransaction = session.beginTransaction();
            session.save(client);
            dbTransaction.commit();
            System.out.println("Client created successfully with code: " + clientCode);
            return client;
        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            System.out.println("Error creating client: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public Client getClientByCode(String clientCode) {
        if (clientCode == null || clientCode.trim().isEmpty()) {
            System.out.println("Client code cannot be empty");
            return null;
        }

        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Client WHERE clientCode = :clientCode");
            query.setParameter("clientCode", clientCode.trim());
            Client client = (Client) query.uniqueResult();
            return client;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<Client> getAllClients() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Client ORDER BY name ASC");
            List<Client> clients = query.list();
            return clients;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<Client> searchClientsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Name cannot be empty");
            return null;
        }

        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Client WHERE LOWER(name) LIKE :name ORDER BY name ASC");
            query.setParameter("name", "%" + name.trim().toLowerCase() + "%");
            List<Client> clients = query.list();
            return clients;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public boolean updateClient(Client client) {
        if (!isValidClient(client)) {
            System.out.println("Validation failed: Invalid client data");
            return false;
        }

        if (client.getClientId() == null || client.getClientId() <= 0) {
            System.out.println("Invalid client ID for update");
            return false;
        }

        Session session = null;
        Transaction dbTransaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            dbTransaction = session.beginTransaction();

            Client existingClient = (Client) session.get(Client.class, client.getClientId());
            if (existingClient == null) {
                System.out.println("Client not found for update - ID: " + client.getClientId());
                if (dbTransaction != null) {
                    dbTransaction.rollback();
                }
                return false;
            }

            // Check if email is being changed and if new email already exists
            if (!existingClient.getEmail().equalsIgnoreCase(client.getEmail().trim())) {
                if (emailExists(client.getEmail(), client.getClientId())) {
                    System.out.println("Email already exists: " + client.getEmail());
                    if (dbTransaction != null) {
                        dbTransaction.rollback();
                    }
                    return false;
                }
            }

            // Check if phone is being changed and if new phone already exists
            if (!existingClient.getPhone().equals(client.getPhone().trim())) {
                if (phoneExists(client.getPhone(), client.getClientId())) {
                    System.out.println("Phone number already exists: " + client.getPhone());
                    if (dbTransaction != null) {
                        dbTransaction.rollback();
                    }
                    return false;
                }
            }

            // Update the managed entity's fields
            existingClient.setName(client.getName().trim());
            existingClient.setEmail(client.getEmail().trim());
            existingClient.setPhone(client.getPhone().trim());

            session.update(existingClient);
            dbTransaction.commit();
            System.out.println("Client updated successfully: " + existingClient.getClientCode());
            return true;

        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            System.out.println("Error updating client: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public boolean deleteClient(Long clientId) {
        if (clientId == null || clientId <= 0) {
            System.out.println("Invalid client ID");
            return false;
        }

        Session session = null;
        Transaction dbTransaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            dbTransaction = session.beginTransaction();
            Client client = (Client) session.get(Client.class, clientId);
            if (client != null) {
                session.delete(client);
                dbTransaction.commit();
                System.out.println("Client deleted successfully");
                return true;
            } else {
                System.out.println("Client not found");
                return false;
            }
        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            System.out.println("Error deleting client: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public long countClients() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT COUNT(*) FROM Client");
            Long count = (Long) query.uniqueResult();
            return count != null ? count : 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}