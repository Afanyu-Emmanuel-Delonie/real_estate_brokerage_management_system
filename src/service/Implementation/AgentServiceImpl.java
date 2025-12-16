package service.Implementation;
import dao.AgentDao;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import model.Agent;
import service.AgentService;

/**
 * @author afany
 */
public class AgentServiceImpl extends UnicastRemoteObject implements AgentService {
    
    private AgentDao dao = new AgentDao();
    
    public AgentServiceImpl() throws RemoteException {
    }
    
    @Override
    public Agent createAgent(Agent theAgent) throws RemoteException {
        Agent created = dao.createAgent(theAgent);
        if (created != null) {
            // Detach Hibernate collections before sending to client
            detachAgent(created);
            
            theAgent.setAgentId(created.getAgentId());
            theAgent.setAgentCode(created.getAgentCode());
            theAgent.setMessage("Agent created successfully");
            theAgent.setStatus("SUCCESS");
            return theAgent;
        } else {
            theAgent.setMessage("Failed to create agent");
            theAgent.setStatus("ERROR");
            return theAgent;
        }
    }
    
    @Override
    public Agent getAgentById(Agent theAgent) throws RemoteException {
        Agent found = dao.getAgentById(theAgent.getAgentId());
        if (found != null) {
            // Detach Hibernate collections before sending to client
            detachAgent(found);
            
            theAgent = found;
            theAgent.setMessage("Agent found");
            theAgent.setStatus("SUCCESS");
        } else {
            theAgent.setMessage("Agent not found");
            theAgent.setStatus("ERROR");
        }
        return theAgent;
    }
    
    @Override
    public Agent getAgentByCode(Agent theAgent) throws RemoteException {
        Agent found = dao.getAgentByCode(theAgent.getAgentCode());
        if (found != null) {
            // Detach Hibernate collections before sending to client
            detachAgent(found);
            
            theAgent = found;
            theAgent.setMessage("Agent found");
            theAgent.setStatus("SUCCESS");
        } else {
            theAgent.setMessage("Agent not found");
            theAgent.setStatus("ERROR");
        }
        return theAgent;
    }
    
    @Override
    public Agent getAgentByEmail(Agent theAgent) throws RemoteException {
        Agent found = dao.getAgentByEmail(theAgent.getEmail());
        if (found != null) {
            // Detach Hibernate collections before sending to client
            detachAgent(found);
            
            theAgent = found;
            theAgent.setMessage("Agent found");
            theAgent.setStatus("SUCCESS");
        } else {
            theAgent.setMessage("Agent not found");
            theAgent.setStatus("ERROR");
        }
        return theAgent;
    }

    @Override
    public List<Agent> getAllAgents() throws RemoteException {
        List<Agent> agents = dao.getAllAgents();
        if (agents != null) {
            // Detach Hibernate collections for all agents
            for (Agent agent : agents) {
                detachAgent(agent);
            }
        }
        return agents;
    }

    @Override
    public List<Agent> getAgentsByStatus(Agent theAgent) throws RemoteException {
        List<Agent> agents = dao.getAgentsByStatus(theAgent.getStatus());
        if (agents != null) {
            // Detach Hibernate collections for all agents
            for (Agent agent : agents) {
                detachAgent(agent);
            }
        }
        return agents;
    }

    @Override
    public Agent updateAgent(Agent theAgent) throws RemoteException {
        boolean updated = dao.updateAgent(theAgent);
        if (updated) {
            theAgent.setMessage("Agent updated successfully");
            theAgent.setStatus("SUCCESS");
        } else {
            theAgent.setMessage("Failed to update agent");
            theAgent.setStatus("ERROR");
        }
        return theAgent;
    }

    @Override
    public Agent deleteAgent(Agent theAgent) throws RemoteException {
        boolean deleted = dao.deleteAgent(theAgent.getAgentId());
        if (deleted) {
            theAgent.setMessage("Agent deleted successfully");
            theAgent.setStatus("SUCCESS");
        } else {
            theAgent.setMessage("Failed to delete agent");
            theAgent.setStatus("ERROR");
        }
        return theAgent;
    }

    @Override
    public Agent changeAgentStatus(Agent theAgent) throws RemoteException {
        boolean changed = dao.changeAgentStatus(theAgent.getAgentId(), theAgent.getStatus());
        if (changed) {
            theAgent.setMessage("Agent status changed successfully");
            theAgent.setStatus("SUCCESS");
        } else {
            theAgent.setMessage("Failed to change agent status");
            theAgent.setStatus("ERROR");
        }
        return theAgent;
    }

    @Override
    public Agent authenticateAgent(Agent theAgent) throws RemoteException {
        // FIXED: Use username instead of name (which doesn't exist in User)
        Agent authenticated = dao.authenticateAgent(
            theAgent.getUser().getUsername(), 
            theAgent.getUser().getPassword()
        );
        if (authenticated != null) {
            // Detach Hibernate collections before sending to client
            detachAgent(authenticated);
            
            theAgent = authenticated;
            theAgent.setMessage("Authentication successful");
            theAgent.setStatus("SUCCESS");
        } else {
            theAgent.setMessage("Authentication failed");
            theAgent.setStatus("ERROR");
        }
        return theAgent;
    }

    @Override
    public Agent authenticateAgentByEmail(Agent theAgent) throws RemoteException {
        Agent authenticated = dao.authenticateAgentByEmail(
            theAgent.getEmail(), 
            theAgent.getUser().getPassword()
        );
        if (authenticated != null) {
            // Detach Hibernate collections before sending to client
            detachAgent(authenticated);
            
            theAgent = authenticated;
            theAgent.setMessage("Authentication successful");
            theAgent.setStatus("SUCCESS");
        } else {
            theAgent.setMessage("Authentication failed");
            theAgent.setStatus("ERROR");
        }
        return theAgent;
    }

    @Override
    public Agent authenticateAgentByCode(Agent theAgent) throws RemoteException {
        Agent authenticated = dao.authenticateAgentByCode(
            theAgent.getAgentCode(), 
            theAgent.getUser().getPassword()
        );
        if (authenticated != null) {
            // Detach Hibernate collections before sending to client
            detachAgent(authenticated);
            
            theAgent = authenticated;
            theAgent.setMessage("Authentication successful");
            theAgent.setStatus("SUCCESS");
        } else {
            theAgent.setMessage("Authentication failed");
            theAgent.setStatus("ERROR");
        }
        return theAgent;
    }

    @Override
    public Agent changePassword(Agent theAgent) throws RemoteException {
        boolean changed = dao.changePassword(
            theAgent.getAgentId(), 
            theAgent.getOldPassword(), 
            theAgent.getUser().getPassword()
        );
        if (changed) {
            theAgent.setMessage("Password changed successfully");
            theAgent.setStatus("SUCCESS");
        } else {
            theAgent.setMessage("Failed to change password");
            theAgent.setStatus("ERROR");
        }
        return theAgent;
    }

    @Override
    public List<Agent> getAgentsAtCap() throws RemoteException {
        List<Agent> agents = dao.getAgentsAtCap();
        if (agents != null) {
            // Detach Hibernate collections for all agents
            for (Agent agent : agents) {
                detachAgent(agent);
            }
        }
        return agents;
    }

    @Override
    public List<Agent> getAgentsNearCap(Agent theAgent) throws RemoteException {
        List<Agent> agents = dao.getAgentsNearCap(theAgent.getPercentageThreshold());
        if (agents != null) {
            // Detach Hibernate collections for all agents
            for (Agent agent : agents) {
                detachAgent(agent);
            }
        }
        return agents;
    }

    @Override
    public List<Agent> getTopEarningAgents(Agent theAgent) throws RemoteException {
        List<Agent> agents = dao.getTopEarningAgents(theAgent.getLimit());
        if (agents != null) {
            // Detach Hibernate collections for all agents
            for (Agent agent : agents) {
                detachAgent(agent);
            }
        }
        return agents;
    }

    @Override
    public Boolean emailExists(Agent theAgent) throws RemoteException {
        return dao.emailExists(theAgent.getEmail());
    }

    /**
     * CRITICAL METHOD: Detach Hibernate lazy-loaded collections
     * This prevents the "PersistentBag" serialization error in RMI
     * Must be called on every Agent object before returning to client
     */
    private void detachAgent(Agent agent) {
        if (agent != null) {
            // Set all lazy-loaded collections to null
            // This removes Hibernate's PersistentBag wrappers
            agent.setProperties(null);
            agent.setTransactions(null);
            agent.setListingTransactions(null);
        }
    }
}