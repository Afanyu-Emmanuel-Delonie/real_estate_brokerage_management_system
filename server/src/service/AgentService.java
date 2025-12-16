/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import model.Agent;

/**
 *
 * @author afany
 */
public interface AgentService extends Remote{
    public Agent createAgent(Agent theAgent) throws RemoteException;
    public Agent getAgentById(Agent theAgent) throws RemoteException;
    public Agent getAgentByCode(Agent theAgent) throws RemoteException;
    public Agent getAgentByEmail(Agent theAgent) throws RemoteException;
    public List<Agent> getAllAgents() throws RemoteException;
    public List<Agent> getAgentsByStatus(Agent theAgent) throws RemoteException;
    public Agent updateAgent(Agent theAgent) throws RemoteException;
    public Agent deleteAgent(Agent theAgent) throws RemoteException;
    public Agent changeAgentStatus(Agent theAgent) throws RemoteException;
    public Agent authenticateAgent(Agent theAgent) throws RemoteException;
    public Agent authenticateAgentByEmail(Agent theAgent) throws RemoteException;
    public Agent authenticateAgentByCode(Agent theAgent) throws RemoteException;
    public Agent changePassword(Agent theAgent) throws RemoteException;
    public List<Agent> getAgentsAtCap() throws RemoteException;
    public List<Agent> getAgentsNearCap(Agent theAgent) throws RemoteException;
    public List<Agent> getTopEarningAgents(Agent theAgent) throws RemoteException;
    public Boolean emailExists(Agent theAgent) throws RemoteException;
}
