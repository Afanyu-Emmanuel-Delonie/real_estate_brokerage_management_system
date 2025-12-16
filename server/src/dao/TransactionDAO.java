package dao;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import model.Transaction;
import model.TransactionType;
import model.Agent;
import model.Client;
import model.Property;
import org.hibernate.*;
import service.CommissionService;

/**
 * Enhanced Data Access Object for Transaction entity
 * Integrated with CommissionService for automatic commission calculation
 * 
 * @author afany
 */
public class TransactionDAO {
    
    private CommissionService commissionService;
    
    public TransactionDAO() {
        this.commissionService = new CommissionService();
    }
    
    private boolean isValidTransaction(Transaction transaction) {
        if (transaction == null) {
            System.out.println("Transaction object is null");
            return false;
        }
        
        if (transaction.getType() == null) {
            System.out.println("Transaction type cannot be null");
            return false;
        }
        
        if (transaction.getAmount() == null || transaction.getAmount() <= 0) {
            System.out.println("Transaction amount must be greater than 0");
            return false;
        }
        
        if (transaction.getAgent() == null) {
            System.out.println("Transaction must be associated with an agent");
            return false;
        }
        
        if (transaction.getClient() == null) {
            System.out.println("Transaction must be associated with a client");
            return false;
        }
        
        if (transaction.getProperty() == null) {
            System.out.println("Transaction must be associated with a property");
            return false;
        }
        
        return true;
    }
    
    private boolean transactionCodeExists(String transactionCode) {
        if (transactionCode == null || transactionCode.trim().isEmpty()) {
            return false;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT COUNT(*) FROM Transaction WHERE transactionCode = :transactionCode");
            query.setParameter("transactionCode", transactionCode);
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
    
    private String generateNextTransactionCode() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            
            Query query = session.createQuery(
                "SELECT t.transactionCode FROM Transaction t WHERE t.transactionCode LIKE 'ZT%' ORDER BY t.transactionCode DESC"
            );
            query.setMaxResults(1);
            
            List<String> results = query.list();
            
            if (results.isEmpty()) {
                return "ZT001";
            } else {
                String lastTransactionCode = results.get(0);
                try {
                    int lastNumber = Integer.parseInt(lastTransactionCode.substring(2));
                    int nextNumber = lastNumber + 1;
                    return String.format("ZT%03d", nextNumber);
                } catch (Exception e) {
                    System.out.println("Error parsing transaction code, starting from ZT001");
                    return "ZT001";
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "ZT001";
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Create a new transaction with automatic commission calculation
     * This method uses CommissionService to calculate all commission fields
     */
    public Transaction createTransaction(Transaction transaction) {
        if (!isValidTransaction(transaction)) {
            System.out.println("Validation failed: Invalid transaction data");
            return null;
        }
        
        String transactionCode = generateNextTransactionCode();
        if (transactionCode == null) {
            System.out.println("Error: Could not generate transaction code");
            return null;
        }
        transaction.setTransactionCode(transactionCode);
        
        if (transaction.getDate() == null) {
            transaction.setDate(LocalDate.now());
        }
        
        // Calculate commissions using CommissionService
        try {
            commissionService.calculateCommissions(transaction);
        } catch (Exception ex) {
            System.out.println("Error calculating commissions: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
        
        Session session = null;
        org.hibernate.Transaction dbTransaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            dbTransaction = session.beginTransaction();
            session.save(transaction);
            dbTransaction.commit();
            System.out.println("Transaction created successfully with code: " + transactionCode);
            return transaction;
        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            System.out.println("Error creating transaction: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public Transaction getTransactionById(Long transactionId) {
        if (transactionId == null || transactionId <= 0) {
            System.out.println("Invalid transaction ID");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = (Transaction) session.get(Transaction.class, transactionId);
            if (transaction == null) {
                System.out.println("Transaction not found with ID: " + transactionId);
            }
            return transaction;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public Transaction getTransactionByCode(String transactionCode) {
        if (transactionCode == null || transactionCode.trim().isEmpty()) {
            System.out.println("Transaction code cannot be empty");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Transaction WHERE transactionCode = :transactionCode");
            query.setParameter("transactionCode", transactionCode.trim());
            Transaction transaction = (Transaction) query.uniqueResult();
            return transaction;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public List<Transaction> getAllTransactions() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Transaction ORDER BY date DESC");
            List<Transaction> transactions = query.list();
            return transactions;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public List<Transaction> getTransactionsByAgent(Long agentId) {
        if (agentId == null || agentId <= 0) {
            System.out.println("Invalid agent ID");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Transaction WHERE agent.agentId = :agentId ORDER BY date DESC");
            query.setParameter("agentId", agentId);
            List<Transaction> transactions = query.list();
            return transactions;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public List<Transaction> getTransactionsByClient(Long clientId) {
        if (clientId == null || clientId <= 0) {
            System.out.println("Invalid client ID");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Transaction WHERE client.clientId = :clientId ORDER BY date DESC");
            query.setParameter("clientId", clientId);
            List<Transaction> transactions = query.list();
            return transactions;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public List<Transaction> getTransactionsByProperty(Long propertyId) {
        if (propertyId == null || propertyId <= 0) {
            System.out.println("Invalid property ID");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Transaction WHERE property.propertyId = :propertyId ORDER BY date DESC");
            query.setParameter("propertyId", propertyId);
            List<Transaction> transactions = query.list();
            return transactions;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public List<Transaction> getTransactionsByType(TransactionType type) {
        if (type == null) {
            System.out.println("Transaction type cannot be null");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Transaction WHERE type = :type ORDER BY date DESC");
            query.setParameter("type", type);
            List<Transaction> transactions = query.list();
            return transactions;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public List<Transaction> getTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            System.out.println("Start date and end date cannot be null");
            return null;
        }
        
        if (startDate.isAfter(endDate)) {
            System.out.println("Start date cannot be after end date");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Transaction WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC");
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            List<Transaction> transactions = query.list();
            return transactions;
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
     * Update transaction - recalculates commissions if transaction amount or type changed
     */
    public Transaction updateTransaction(Transaction transaction) {
        if (!isValidTransaction(transaction)) {
            System.out.println("Validation failed: Invalid transaction data");
            return null;
        }

        if (transaction.getTransactionId() == null || transaction.getTransactionId() <= 0) {
            System.out.println("Invalid transaction ID for update");
            return null;
        }

        Session session = null;
        org.hibernate.Transaction dbTransaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            dbTransaction = session.beginTransaction();

            // Get the existing transaction from database
            Transaction existingTransaction = (Transaction) session.get(Transaction.class, transaction.getTransactionId());
            if (existingTransaction == null) {
                System.out.println("Transaction not found for update");
                return null;
            }

            // Check if amount or type changed - if so, recalculate commissions
            boolean shouldRecalculate = !existingTransaction.getAmount().equals(transaction.getAmount()) ||
                    !existingTransaction.getType().equals(transaction.getType());

            // Update the existing transaction's fields
            existingTransaction.setProperty(transaction.getProperty());
            existingTransaction.setAgent(transaction.getAgent());
            existingTransaction.setClient(transaction.getClient());
            existingTransaction.setType(transaction.getType());
            existingTransaction.setAmount(transaction.getAmount());

            // Recalculate commissions if needed
            if (shouldRecalculate) {
                commissionService.calculateCommissions(existingTransaction);
            }

            // No need to call session.update() - Hibernate will automatically detect changes
            // on the managed entity (existingTransaction)

            dbTransaction.commit();
            System.out.println("Transaction updated successfully: " + existingTransaction.getTransactionCode());
            return existingTransaction;
        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            System.out.println("Error updating transaction: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    // Alternative version using merge() - also works well
    public Transaction updateTransactionWithMerge(Transaction transaction) {
        if (!isValidTransaction(transaction)) {
            System.out.println("Validation failed: Invalid transaction data");
            return null;
        }

        if (transaction.getTransactionId() == null || transaction.getTransactionId() <= 0) {
            System.out.println("Invalid transaction ID for update");
            return null;
        }

        Session session = null;
        org.hibernate.Transaction dbTransaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            dbTransaction = session.beginTransaction();

            // Get the existing transaction to check if we need to recalculate
            Transaction existingTransaction = (Transaction) session.get(Transaction.class, transaction.getTransactionId());
            if (existingTransaction == null) {
                System.out.println("Transaction not found for update");
                return null;
            }

            // Check if amount or type changed - if so, recalculate commissions
            boolean shouldRecalculate = !existingTransaction.getAmount().equals(transaction.getAmount()) ||
                    !existingTransaction.getType().equals(transaction.getType());

            if (shouldRecalculate) {
                commissionService.calculateCommissions(transaction);
            }

            // Use merge to handle detached entity
            Transaction mergedTransaction = (Transaction) session.merge(transaction);

            dbTransaction.commit();
            System.out.println("Transaction updated successfully: " + mergedTransaction.getTransactionCode());
            return mergedTransaction;
        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            System.out.println("Error updating transaction: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public boolean deleteTransaction(Long transactionId) {
        if (transactionId == null || transactionId <= 0) {
            System.out.println("Invalid transaction ID");
            return false;
        }
        
        Session session = null;
        org.hibernate.Transaction dbTransaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            dbTransaction = session.beginTransaction();
            Transaction transaction = (Transaction) session.get(Transaction.class, transactionId);
            if (transaction != null) {
                session.delete(transaction);
                dbTransaction.commit();
                System.out.println("Transaction deleted successfully");
                return true;
            } else {
                System.out.println("Transaction not found");
                return false;
            }
        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            System.out.println("Error deleting transaction: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    // ========== Financial Summary Methods ==========
    
    public Double getTotalAmountByAgent(Long agentId) {
        if (agentId == null || agentId <= 0) {
            return 0.0;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT SUM(amount) FROM Transaction WHERE agent.agentId = :agentId");
            query.setParameter("agentId", agentId);
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
    
    public Double getTotalCommissionByAgent(Long agentId) {
        if (agentId == null || agentId <= 0) {
            return 0.0;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT SUM(agentCommission) FROM Transaction WHERE agent.agentId = :agentId");
            query.setParameter("agentId", agentId);
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
    
    public Double getTotalCompanyCommission() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT SUM(companyCommission) FROM Transaction");
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
    
    public long countTransactionsByAgent(Long agentId) {
        if (agentId == null || agentId <= 0) {
            return 0;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT COUNT(*) FROM Transaction WHERE agent.agentId = :agentId");
            query.setParameter("agentId", agentId);
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
    
    public long countTransactionsByType(TransactionType type) {
        if (type == null) {
            return 0;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT COUNT(*) FROM Transaction WHERE type = :type");
            query.setParameter("type", type);
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
    
    // ========== Commission-Specific Methods ==========
    
    /**
     * Get transactions where agents were at cap
     */
    public List<Transaction> getTransactionsWithAgentsAtCap() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Transaction WHERE sellingAgentAtCap = true OR listingAgentAtCap = true ORDER BY date DESC"
            );
            List<Transaction> transactions = query.list();
            return transactions;
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
     * Get dual-agent transactions (different listing and selling agents)
     */
    public List<Transaction> getDualAgentTransactions() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Transaction WHERE listingAgent IS NOT NULL AND listingAgent != agent ORDER BY date DESC"
            );
            List<Transaction> transactions = query.list();
            return transactions;
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
     * Get transactions by listing agent
     */
    public List<Transaction> getTransactionsByListingAgent(Long agentId) {
        if (agentId == null || agentId <= 0) {
            System.out.println("Invalid agent ID");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Transaction WHERE listingAgent.agentId = :agentId ORDER BY date DESC");
            query.setParameter("agentId", agentId);
            List<Transaction> transactions = query.list();
            return transactions;
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
     * Get total listing agent commission
     */
    public Double getTotalListingCommissionByAgent(Long agentId) {
        if (agentId == null || agentId <= 0) {
            return 0.0;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "SELECT SUM(listingAgentCommission) FROM Transaction WHERE listingAgent.agentId = :agentId"
            );
            query.setParameter("agentId", agentId);
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
     * Get transactions for current year by agent
     */
    public List<Transaction> getCurrentYearTransactionsByAgent(Long agentId) {
        if (agentId == null || agentId <= 0) {
            System.out.println("Invalid agent ID");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            int currentYear = Year.now().getValue();
            LocalDate startOfYear = LocalDate.of(currentYear, 1, 1);
            LocalDate endOfYear = LocalDate.of(currentYear, 12, 31);
            
            Query query = session.createQuery(
                "FROM Transaction WHERE agent.agentId = :agentId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC"
            );
            query.setParameter("agentId", agentId);
            query.setParameter("startDate", startOfYear);
            query.setParameter("endDate", endOfYear);
            List<Transaction> transactions = query.list();
            return transactions;
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
     * Get average transaction amount by type
     */
    public Double getAverageTransactionAmountByType(TransactionType type) {
        if (type == null) {
            return 0.0;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT AVG(amount) FROM Transaction WHERE type = :type");
            query.setParameter("type", type);
            Double avg = (Double) query.uniqueResult();
            return avg != null ? avg : 0.0;
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
     * Get total sales volume (sum of all SALE transactions)
     */
    public Double getTotalSalesVolume() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT SUM(amount) FROM Transaction WHERE type = :type");
            query.setParameter("type", TransactionType.SALE);
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
     * Get total rental revenue (sum of all RENT transactions)
     */
    public Double getTotalRentalRevenue() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT SUM(amount) FROM Transaction WHERE type = :type");
            query.setParameter("type", TransactionType.RENT);
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
     * Get monthly transaction report for a specific month
     */
    public List<Transaction> getMonthlyTransactions(int year, int month) {
        if (month < 1 || month > 12) {
            System.out.println("Invalid month");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1);
            
            Query query = session.createQuery(
                "FROM Transaction WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC"
            );
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            List<Transaction> transactions = query.list();
            return transactions;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}