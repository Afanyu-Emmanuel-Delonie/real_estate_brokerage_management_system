/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import dao.AgentDao;
import java.time.Year;
import model.Agent;
import model.Transaction;
import model.TransactionType;

/**
 *
 * @author afany
 */
public class CommissionService {
     private static final Double SALES_COMMISSION_RATE = 0.06; 
    private static final Double RENTAL_MANAGEMENT_FEE = 0.20; 
    
    // Split ratios before cap
    private static final Double SALES_AGENT_SPLIT = 0.80; 
    private static final Double SALES_COMPANY_SPLIT = 0.20; 
    private static final Double RENTAL_AGENT_SPLIT = 0.90; 
    private static final Double RENTAL_COMPANY_SPLIT = 0.10; 
    
    // Split ratios after cap
    private static final Double AFTER_CAP_AGENT_SPLIT = 0.95; 
    private static final Double AFTER_CAP_COMPANY_SPLIT = 0.05; 
    
    // Dual-agent split (of the 80% agent portion)
    private static final Double SELLING_AGENT_RATIO = 0.60; 
    private static final Double LISTING_AGENT_RATIO = 0.40; 
    
    private AgentDao agentDao;
    
    public CommissionService() {
        this.agentDao = new AgentDao();
    }
    
    /**
     * Calculate and set all commission fields for a transaction
     * This is the main entry point for commission calculation
     */
    public void calculateCommissions(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        
        // Check and reset agent's year if needed
        checkAndResetAgentYear(transaction.getAgent());
        
        if (transaction.getType() == TransactionType.SALE) {
            if (transaction.getListingAgent() != null && 
                !transaction.getListingAgent().getAgentId().equals(transaction.getAgent().getAgentId())) {
                // Dual-agent sale
                checkAndResetAgentYear(transaction.getListingAgent());
                calculateDualAgentSaleCommission(transaction);
            } else {
                // Single-agent sale
                calculateSingleAgentSaleCommission(transaction);
            }
        } else if (transaction.getType() == TransactionType.RENT) {
            calculateRentalCommission(transaction);
        } else {
            throw new IllegalArgumentException("Invalid transaction type");
        }
    }
    
    /**
     * Calculate commission for single-agent sale
     */
    private void calculateSingleAgentSaleCommission(Transaction transaction) {
        Agent agent = transaction.getAgent();
        Double saleAmount = transaction.getAmount();
        
        // Calculate total commission (6% of sale amount)
        Double totalCommission = saleAmount * SALES_COMMISSION_RATE;
        transaction.setTotalCommission(totalCommission);
        
        // Check if agent has reached cap
        boolean agentAtCap = hasReachedCap(agent);
        transaction.setSellingAgentAtCap(agentAtCap);
        
        Double agentCommission;
        Double companyCommission;
        
        if (agentAtCap) {
            // After cap: 95/5 split
            agentCommission = totalCommission * AFTER_CAP_AGENT_SPLIT;
            companyCommission = totalCommission * AFTER_CAP_COMPANY_SPLIT;
        } else {
            // Before cap: 80/20 split
            agentCommission = totalCommission * SALES_AGENT_SPLIT;
            companyCommission = totalCommission * SALES_COMPANY_SPLIT;
        }
        
        transaction.setAgentCommission(agentCommission);
        transaction.setCompanyCommission(companyCommission);
        transaction.setSellingAgentCommission(agentCommission);
        transaction.setListingAgentCommission(0.0);
        
        // Update agent's year-to-date commission
        updateAgentCommission(agent, agentCommission);
    }
    
    /**
     * Calculate commission for dual-agent sale (different listing and selling agents)
     */
    private void calculateDualAgentSaleCommission(Transaction transaction) {
        Agent sellingAgent = transaction.getAgent();
        Agent listingAgent = transaction.getListingAgent();
        Double saleAmount = transaction.getAmount();
        
        // Calculate total commission (6% of sale amount)
        Double totalCommission = saleAmount * SALES_COMMISSION_RATE;
        transaction.setTotalCommission(totalCommission);
        
        // Check cap status for both agents
        boolean sellingAgentAtCap = hasReachedCap(sellingAgent);
        boolean listingAgentAtCap = hasReachedCap(listingAgent);
        transaction.setSellingAgentAtCap(sellingAgentAtCap);
        transaction.setListingAgentAtCap(listingAgentAtCap);
        
        // Calculate agent portion (80% of total) and company portion (20%)
        Double agentPortion = totalCommission * SALES_AGENT_SPLIT;
        Double companyCommission = totalCommission * SALES_COMPANY_SPLIT;
        
        // Split agent portion 60/40 between selling and listing agents
        Double sellingAgentBase = agentPortion * SELLING_AGENT_RATIO; // 48% of total
        Double listingAgentBase = agentPortion * LISTING_AGENT_RATIO; // 32% of total
        
        // Adjust for cap status if needed
        Double sellingAgentCommission = sellingAgentBase;
        Double listingAgentCommission = listingAgentBase;
        
        // If selling agent at cap, they get more from company portion
        if (sellingAgentAtCap) {
            Double sellingAgentBonus = companyCommission * 0.50; // Take 50% from company
            sellingAgentCommission += sellingAgentBonus;
            companyCommission -= sellingAgentBonus;
        }
        
        // If listing agent at cap, they get more from company portion
        if (listingAgentAtCap) {
            Double listingAgentBonus = companyCommission * 0.50; // Take 50% from company
            listingAgentCommission += listingAgentBonus;
            companyCommission -= listingAgentBonus;
        }
        
        transaction.setSellingAgentCommission(sellingAgentCommission);
        transaction.setListingAgentCommission(listingAgentCommission);
        transaction.setAgentCommission(sellingAgentCommission); 
        transaction.setCompanyCommission(companyCommission);
        
        updateAgentCommission(sellingAgent, sellingAgentCommission);
        updateAgentCommission(listingAgent, listingAgentCommission);
    }
    
    /**
     * Calculate commission for rental transaction
     */
    private void calculateRentalCommission(Transaction transaction) {
        Agent agent = transaction.getAgent();
        Double monthlyRent = transaction.getAmount();
        
        // Calculate management fee (20% of monthly rent)
        Double totalCommission = monthlyRent * RENTAL_MANAGEMENT_FEE;
        transaction.setTotalCommission(totalCommission);
        
        // Check if agent has reached cap
        boolean agentAtCap = hasReachedCap(agent);
        transaction.setSellingAgentAtCap(agentAtCap);
        
        Double agentCommission;
        Double companyCommission;
        
        if (agentAtCap) {
            
            agentCommission = totalCommission * AFTER_CAP_AGENT_SPLIT;
            companyCommission = totalCommission * AFTER_CAP_COMPANY_SPLIT;
        } else {
          
            agentCommission = totalCommission * RENTAL_AGENT_SPLIT;
            companyCommission = totalCommission * RENTAL_COMPANY_SPLIT;
        }
        
        transaction.setAgentCommission(agentCommission);
        transaction.setCompanyCommission(companyCommission);
        transaction.setSellingAgentCommission(agentCommission);
        transaction.setListingAgentCommission(0.0);
        updateAgentCommission(agent, agentCommission);
    }
    
    /**
     * Check if agent has reached their salary cap
     */
    private boolean hasReachedCap(Agent agent) {
        if (agent == null) {
            return false;
        }
        
        return agent.getYearToDateCommission() >= agent.getSalaryCap();
    }
    
    /**
     * Update agent's year-to-date commission
     */
    private void updateAgentCommission(Agent agent, Double commissionAmount) {
        if (agent == null || commissionAmount == null) {
            return;
        }
        
        Double currentYTD = agent.getYearToDateCommission();
        agent.setYearToDateCommission(currentYTD + commissionAmount);
        
        // Save updated agent
        agentDao.updateAgent(agent);
    }
    
    /**
     * Check if agent's commission year needs to be reset
     * If it's a new year, reset YTD commission to 0
     */
    private void checkAndResetAgentYear(Agent agent) {
        if (agent == null) {
            return;
        }
        
        int currentYear = Year.now().getValue();
        
        if (agent.getCommissionYear() == null || agent.getCommissionYear() < currentYear) {
            agent.setCommissionYear(currentYear);
            agent.setYearToDateCommission(0.0);
            agentDao.updateAgent(agent);
        }
    }
    
    /**
     * Get agent's remaining commission before hitting cap
     */
    public Double getRemainingBeforeCap(Agent agent) {
        if (agent == null) {
            return 0.0;
        }
        
        Double remaining = agent.getSalaryCap() - agent.getYearToDateCommission();
        return remaining > 0 ? remaining : 0.0;
    }
    
    /**
     * Check if agent will reach cap with this transaction
     */
    public boolean willReachCapWithTransaction(Agent agent, Double transactionAmount, TransactionType type) {
        if (agent == null || transactionAmount == null || type == null) {
            return false;
        }
        
        Double totalCommission;
        Double agentCommission;
        
        if (type == TransactionType.SALE) {
            totalCommission = transactionAmount * SALES_COMMISSION_RATE;
            agentCommission = totalCommission * SALES_AGENT_SPLIT;
        } else {
            totalCommission = transactionAmount * RENTAL_MANAGEMENT_FEE;
            agentCommission = totalCommission * RENTAL_AGENT_SPLIT;
        }
        
        Double projectedYTD = agent.getYearToDateCommission() + agentCommission;
        return projectedYTD >= agent.getSalaryCap();
    }
}
