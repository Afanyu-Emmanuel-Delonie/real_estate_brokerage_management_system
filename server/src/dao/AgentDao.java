package dao;

import java.time.Year;
import java.util.List;
import model.Agent;
import model.AgentStatus;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Enhanced Data Access Object for Agent entity
 * Includes commission tracking, year management, and authentication
 * 
 * @author afany
 */
public class AgentDao {
    
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String trimmedEmail = email.trim();
        
        // Check for multiple @ symbols
        int atCount = 0;
        for (char c : trimmedEmail.toCharArray()) {
            if (c == '@') atCount++;
        }
        if (atCount != 1) {
            System.out.println("Email must contain exactly one @ symbol");
            return false;
        }
        
        // Check for consecutive dots
        if (trimmedEmail.contains("..")) {
            System.out.println("Email cannot contain consecutive dots");
            return false;
        }
        
        // Check if starts or ends with special characters
        if (trimmedEmail.startsWith(".") || trimmedEmail.startsWith("@") || 
            trimmedEmail.endsWith(".") || trimmedEmail.endsWith("@")) {
            System.out.println("Email cannot start or end with . or @");
            return false;
        }
        
        // Must end with @vzzbrokerage.com
        if (!trimmedEmail.toLowerCase().endsWith("@vzzbrokerage.com")) {
            System.out.println("Email must be from @vzzbrokerage.com domain");
            return false;
        }
        
        // Extract local part (before @)
        String localPart = trimmedEmail.substring(0, trimmedEmail.indexOf('@'));
        if (localPart.isEmpty()) {
            System.out.println("Email must have a username before @");
            return false;
        }
        
        // Validate local part
        String localPartRegex = "^[A-Za-z0-9._-]+$";
        if (!localPart.matches(localPartRegex)) {
            System.out.println("Email username can only contain letters, numbers, dots, underscores, and hyphens");
            return false;
        }
        
        return true;
    }

    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true;
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
    public boolean emailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT COUNT(*) FROM Agent WHERE email = :email");
            query.setParameter("email", email.trim());
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
      
    private boolean agentCodeExists(String agentCode) {
        if (agentCode == null || agentCode.trim().isEmpty()) {
            return false;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT COUNT(*) FROM Agent WHERE agentCode = :agentCode");
            query.setParameter("agentCode", agentCode);
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
       
    private String generateNextAgentCode() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            
            Query query = session.createQuery(
                "SELECT a.agentCode FROM Agent a WHERE a.agentCode LIKE 'ZA%' ORDER BY a.agentCode DESC"
            );
            query.setMaxResults(1);
            
            List<String> results = query.list();
            
            if (results.isEmpty()) {
                return "ZA001";
            } else {
                String lastAgentCode = results.get(0);
                try {
                    int lastNumber = Integer.parseInt(lastAgentCode.substring(2));
                    int nextNumber = lastNumber + 1;
                    return String.format("ZA%03d", nextNumber);
                } catch (Exception e) {
                    System.out.println("Error parsing agent code, starting from ZA001");
                    return "ZA001";
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "ZA001";
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    private boolean isValidAgent(Agent agent) {
        if (agent == null) {
            System.out.println("Agent object is null");
            return false;
        }
        
        // Validate user
        if (agent.getUser() == null) {
            System.out.println("Agent must be associated with a user");
            return false;
        }
        
        // Validate email
        if (!isValidEmail(agent.getEmail())) {
            return false;
        }
        
        // Validate phone
        if (!isValidPhone(agent.getPhone())) {
            return false;
        }
        
        // Validate salary cap
        if (agent.getSalaryCap() == null || agent.getSalaryCap() <= 0) {
            System.out.println("Salary cap must be greater than 0");
            return false;
        }
        
        return true;
    }
    
    // ========== Authentication Methods ==========
    
    /**
     * Authenticate agent by name and password
     * Returns the Agent object if credentials are valid, null otherwise
     */
    public Agent authenticateAgent(String name, String password) {
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Name cannot be empty");
            return null;
        }
        
        if (password == null || password.isEmpty()) {
            System.out.println("Password cannot be empty");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            
            // Query to find agent by name through the User relationship
            Query query = session.createQuery(
                "FROM Agent a WHERE a.user.name = :name AND a.user.password = :password"
            );
            query.setParameter("name", name.trim());
            query.setParameter("password", password);
            
            Agent agent = (Agent) query.uniqueResult();
            
            if (agent == null) {
                System.out.println("Invalid credentials");
                return null;
            }
            
            // Check if agent is active
            if (agent.getStatus() != AgentStatus.ACTIVE) {
                System.out.println("Agent account is not active");
                return null;
            }
            
            System.out.println("Agent authenticated successfully: " + agent.getAgentCode());
            return agent;
            
        } catch (Exception ex) {
            System.out.println("Error during authentication: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Authenticate agent by email and password
     */
    public Agent authenticateAgentByEmail(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            System.out.println("Email cannot be empty");
            return null;
        }
        
        if (password == null || password.isEmpty()) {
            System.out.println("Password cannot be empty");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            
            Query query = session.createQuery(
                "FROM Agent a WHERE a.email = :email AND a.user.password = :password"
            );
            query.setParameter("email", email.trim());
            query.setParameter("password", password);
            
            Agent agent = (Agent) query.uniqueResult();
            
            if (agent == null) {
                System.out.println("Invalid credentials");
                return null;
            }
            
            // Check if agent is active
            if (agent.getStatus() != AgentStatus.ACTIVE) {
                System.out.println("Agent account is not active");
                return null;
            }
            
            System.out.println("Agent authenticated successfully: " + agent.getAgentCode());
            return agent;
            
        } catch (Exception ex) {
            System.out.println("Error during authentication: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Authenticate agent by agent code and password
     */
    public Agent authenticateAgentByCode(String agentCode, String password) {
        if (agentCode == null || agentCode.trim().isEmpty()) {
            System.out.println("Agent code cannot be empty");
            return null;
        }
        
        if (password == null || password.isEmpty()) {
            System.out.println("Password cannot be empty");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            
            Query query = session.createQuery(
                "FROM Agent a WHERE a.agentCode = :agentCode AND a.user.password = :password"
            );
            query.setParameter("agentCode", agentCode.trim());
            query.setParameter("password", password);
            
            Agent agent = (Agent) query.uniqueResult();
            
            if (agent == null) {
                System.out.println("Invalid credentials");
                return null;
            }
            
            // Check if agent is active
            if (agent.getStatus() != AgentStatus.ACTIVE) {
                System.out.println("Agent account is not active");
                return null;
            }
            
            System.out.println("Agent authenticated successfully: " + agent.getAgentCode());
            return agent;
            
        } catch (Exception ex) {
            System.out.println("Error during authentication: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Change agent password
     */
    public boolean changePassword(Long agentId, String oldPassword, String newPassword) {
        if (agentId == null || agentId <= 0) {
            System.out.println("Invalid agent ID");
            return false;
        }
        
        if (oldPassword == null || oldPassword.isEmpty()) {
            System.out.println("Old password cannot be empty");
            return false;
        }
        
        if (newPassword == null || newPassword.isEmpty()) {
            System.out.println("New password cannot be empty");
            return false;
        }
        
        if (newPassword.length() < 6) {
            System.out.println("New password must be at least 6 characters");
            return false;
        }
        
        Session session = null;
        Transaction dbTransaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Agent agent = (Agent) session.get(Agent.class, agentId);
            
            if (agent == null) {
                System.out.println("Agent not found");
                return false;
            }
            
            // Verify old password
            if (!agent.getUser().getPassword().equals(oldPassword)) {
                System.out.println("Old password is incorrect");
                return false;
            }
            
            dbTransaction = session.beginTransaction();
            agent.getUser().setPassword(newPassword);
            session.update(agent);
            dbTransaction.commit();
            
            System.out.println("Password changed successfully");
            return true;
            
        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            System.out.println("Error changing password: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    // ========== CRUD Methods ==========

    public Agent createAgent(Agent agent) {
        System.out.println("DAO: Starting createAgent");
        System.out.println("DAO: Agent data - " + agent.getName() + ", " + agent.getEmail());
        System.out.println("DAO: User object - " + (agent.getUser() != null ? "EXISTS" : "NULL"));

        if (!isValidAgent(agent)) {
            System.out.println("DAO: Validation failed: Invalid agent data");
            return null;
        }

        System.out.println("DAO: Validation passed");

        if (emailExists(agent.getEmail())) {
            System.out.println("DAO: Error: Email already exists");
            return null;
        }

        System.out.println("DAO: Email check passed");

        String agentCode = generateNextAgentCode();
        if (agentCode == null) {
            System.out.println("DAO: Error: Could not generate agent code");
            return null;
        }
        agent.setAgentCode(agentCode);

        System.out.println("DAO: Agent code generated: " + agentCode);

        if (agent.getStatus() == null) {
            agent.setStatus(AgentStatus.ACTIVE);
        }

        if (agent.getCommissionYear() == null) {
            agent.setCommissionYear(Year.now().getValue());
        }
        if (agent.getYearToDateCommission() == null) {
            agent.setYearToDateCommission(0.0);
        }
        if (agent.getSalaryCap() == null) {
            agent.setSalaryCap(2000000.0);
        }

        System.out.println("DAO: About to save to database");

        Session session = null;
        Transaction dbTransaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            dbTransaction = session.beginTransaction();

            System.out.println("DAO: Session opened, transaction started");

            session.save(agent);

            System.out.println("DAO: Agent saved, committing transaction");

            dbTransaction.commit();

            System.out.println("DAO: Transaction committed successfully");
            System.out.println("DAO: Agent created successfully with code: " + agentCode);
            return agent;
        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
                System.out.println("DAO: Transaction rolled back");
            }
            System.out.println("DAO: Error creating agent: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
                System.out.println("DAO: Session closed");
            }
        }
    }

    public Agent getAgentById(Long agentId) {
        if (agentId == null || agentId <= 0) {
            System.out.println("Invalid agent ID");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Agent agent = (Agent) session.get(Agent.class, agentId);
            if (agent == null) {
                System.out.println("Agent not found with ID: " + agentId);
            }
            return agent;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
     
    public Agent getAgentByCode(String agentCode) {
        if (agentCode == null || agentCode.trim().isEmpty()) {
            System.out.println("Agent code cannot be empty");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Agent WHERE agentCode = :agentCode");
            query.setParameter("agentCode", agentCode.trim());
            Agent agent = (Agent) query.uniqueResult();
            return agent;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
       
    public Agent getAgentByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            System.out.println("Email cannot be empty");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Agent WHERE email = :email");
            query.setParameter("email", email.trim());
            Agent agent = (Agent) query.uniqueResult();
            return agent;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
     
    public List<Agent> getAllAgents() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Agent ORDER BY agentCode ASC");
            List<Agent> agents = query.list();
            return agents;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<Agent> getAgentsByStatus(AgentStatus status) {
        if (status == null) {
            System.out.println("Status cannot be null");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Agent WHERE status = :status ORDER BY agentCode ASC");
            query.setParameter("status", status);
            List<Agent> agents = query.list();
            return agents;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    
    public boolean updateAgent(Agent agent) {
    if (!isValidAgent(agent)) {
        System.out.println("Validation failed: Invalid agent data");
        return false;
    }
    
    if (agent.getAgentId() == null || agent.getAgentId() <= 0) {
        System.out.println("Invalid agent ID for update");
        return false;
    }
    
    Session session = null;
    Transaction dbTransaction = null;
    try {
        session = HibernateUtil.getSessionFactory().openSession();
        
        // Load the existing agent within this session
        Agent existingAgent = (Agent) session.get(Agent.class, agent.getAgentId());
        if (existingAgent == null) {
            System.out.println("Agent not found for update");
            return false;
        }
        
        // Check email uniqueness (only if email changed)
        if (!existingAgent.getEmail().equals(agent.getEmail())) {
            if (emailExists(agent.getEmail())) {
                System.out.println("Email already exists");
                return false;
            }
        }
        
        // Start transaction
        dbTransaction = session.beginTransaction();
        
        // Update Agent fields on the existing managed entity
        existingAgent.setName(agent.getName());
        existingAgent.setEmail(agent.getEmail());
        existingAgent.setPhone(agent.getPhone());
        existingAgent.setStatus(agent.getStatus());
        existingAgent.setSalaryCap(agent.getSalaryCap());
        
        // Update User fields - ONLY firstName and lastName exist!
        if (agent.getUser() != null && existingAgent.getUser() != null) {
            existingAgent.getUser().setFirstName(agent.getUser().getFirstName());
            existingAgent.getUser().setLastName(agent.getUser().getLastName());
            // REMOVED: existingAgent.getUser().setName() - this method doesn't exist!
        }
        
        // Hibernate will auto-detect changes to managed entities
        // No need to call update() or merge()
        
        dbTransaction.commit();
        
        System.out.println("Agent updated successfully");
        return true;
        
    } catch (Exception ex) {
        if (dbTransaction != null) {
            dbTransaction.rollback();
        }
        System.out.println("Error updating agent: " + ex.getMessage());
        ex.printStackTrace();
        return false;
    } finally {
        if (session != null) {
            session.close();
        }
    }
}

    public boolean deleteAgent(Long agentId) {
        if (agentId == null || agentId <= 0) {
            System.out.println("Invalid agent ID");
            return false;
        }
        
        Session session = null;
        Transaction dbTransaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            dbTransaction = session.beginTransaction();
            Agent agent = (Agent) session.get(Agent.class, agentId);
            if (agent != null) {
                session.delete(agent);
                dbTransaction.commit();
                System.out.println("Agent deleted successfully");
                return true;
            } else {
                System.out.println("Agent not found");
                return false;
            }
        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            System.out.println("Error deleting agent: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
        
    public boolean changeAgentStatus(Long agentId, AgentStatus newStatus) {
        if (agentId == null || agentId <= 0) {
            System.out.println("Invalid agent ID");
            return false;
        }
        
        if (newStatus == null) {
            System.out.println("Status cannot be null");
            return false;
        }
        
        Session session = null;
        Transaction dbTransaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Agent agent = (Agent) session.get(Agent.class, agentId);
            
            if (agent == null) {
                System.out.println("Agent not found");
                return false;
            }
            
            dbTransaction = session.beginTransaction();
            agent.setStatus(newStatus);
            session.update(agent);
            dbTransaction.commit();
            System.out.println("Agent status changed successfully to: " + newStatus);
            return true;
        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            System.out.println("Error changing agent status: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    // ========== Commission-Related Methods ==========
    
    /**
     * Get agents who have reached their salary cap for the current year
     */
    public List<Agent> getAgentsAtCap() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            int currentYear = Year.now().getValue();
            Query query = session.createQuery(
                "FROM Agent WHERE commissionYear = :year AND yearToDateCommission >= salaryCap ORDER BY yearToDateCommission DESC"
            );
            query.setParameter("year", currentYear);
            List<Agent> agents = query.list();
            return agents;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Get agents close to reaching their cap (within specified percentage)
     */
    public List<Agent> getAgentsNearCap(double percentageThreshold) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            int currentYear = Year.now().getValue();
            Query query = session.createQuery(
                "FROM Agent WHERE commissionYear = :year AND " +
                "yearToDateCommission >= (salaryCap * :threshold) AND " +
                "yearToDateCommission < salaryCap ORDER BY yearToDateCommission DESC"
            );
            query.setParameter("year", currentYear);
            query.setParameter("threshold", percentageThreshold);
            List<Agent> agents = query.list();
            return agents;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Get top earning agents for the current year
     */
    public List<Agent> getTopEarningAgents(int limit) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            int currentYear = Year.now().getValue();
            Query query = session.createQuery(
                "FROM Agent WHERE commissionYear = :year ORDER BY yearToDateCommission DESC"
            );
            query.setParameter("year", currentYear);
            query.setMaxResults(limit);
            List<Agent> agents = query.list();
            return agents;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Reset all agents' commission year if needed (typically run at year start)
     */
    public int resetCommissionYearForAllAgents() {
        Session session = null;
        Transaction dbTransaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            int currentYear = Year.now().getValue();
            
            dbTransaction = session.beginTransaction();
            Query query = session.createQuery(
                "UPDATE Agent SET commissionYear = :currentYear, yearToDateCommission = 0.0 " +
                "WHERE commissionYear < :currentYear"
            );
            query.setParameter("currentYear", currentYear);
            int updated = query.executeUpdate();
            dbTransaction.commit();
            
            System.out.println("Reset commission year for " + updated + " agents");
            return updated;
        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            System.out.println("Error resetting commission year: " + ex.getMessage());
            ex.printStackTrace();
            return 0;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Get total commission paid to all agents for current year
     */
    public Double getTotalCommissionsPaidThisYear() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            int currentYear = Year.now().getValue();
            Query query = session.createQuery(
                "SELECT SUM(yearToDateCommission) FROM Agent WHERE commissionYear = :year"
            );
            query.setParameter("year", currentYear);
            Double total = (Double) query.uniqueResult();
            return total != null ? total : 0.0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0.0;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Get count of agents at cap
     */
    public long countAgentsAtCap() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            int currentYear = Year.now().getValue();
            Query query = session.createQuery(
                "SELECT COUNT(*) FROM Agent WHERE commissionYear = :year AND yearToDateCommission >= salaryCap"
            );
            query.setParameter("year", currentYear);
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