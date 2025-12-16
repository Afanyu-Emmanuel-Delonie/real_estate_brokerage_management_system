/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.Implementation;

import dao.TransactionDAO;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import model.Transaction;
import service.TransactionService;



import dao.TransactionDAO;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import model.Transaction;
import service.TransactionService;

/**
 *
 * @author afany
 */
public class TransactionServiceImpl extends UnicastRemoteObject implements TransactionService {

    private TransactionDAO dao = new TransactionDAO();

    public TransactionServiceImpl() throws RemoteException {
    }

    @Override
    public Transaction createTransaction(Transaction theTransaction) throws RemoteException {
        Transaction created = dao.createTransaction(theTransaction);
        if (created != null) {
            return created;
        } else {
            return null;
        }
    }

    @Override
    public Transaction getTransactionByCode(Transaction theTransaction) throws RemoteException {
        Transaction found = dao.getTransactionByCode(theTransaction.getTransactionCode());
        return found;
    }

    @Override
    public List<Transaction> getAllTransactions() throws RemoteException {
        return dao.getAllTransactions();
    }

    @Override
    public List<Transaction> getTransactionsByAgent(Transaction theTransaction) throws RemoteException {
        return dao.getTransactionsByAgent(theTransaction.getAgent().getAgentId());
    }

    @Override
    public List<Transaction> getTransactionsByClient(Transaction theTransaction) throws RemoteException {
        return dao.getTransactionsByClient(theTransaction.getClient().getClientId());
    }

    @Override
    public List<Transaction> getTransactionsByProperty(Transaction theTransaction) throws RemoteException {
        return dao.getTransactionsByProperty(theTransaction.getProperty().getPropertyId());
    }

    @Override
    public List<Transaction> getTransactionsByType(Transaction theTransaction) throws RemoteException {
        return dao.getTransactionsByType(theTransaction.getType());
    }

    @Override
    public Transaction updateTransaction(Transaction theTransaction) throws RemoteException {
        Transaction updated = dao.updateTransaction(theTransaction);
        if (updated != null) {
            return updated;
        } else {
            return null;
        }
    }

    @Override
    public Transaction deleteTransaction(Transaction theTransaction) throws RemoteException {
        boolean deleted = dao.deleteTransaction(theTransaction.getTransactionId());
        if (deleted) {
            return theTransaction;
        } else {
            return null;
        }
    }

    @Override
    public Double getTotalAmountByAgent(Transaction theTransaction) throws RemoteException {
        return dao.getTotalAmountByAgent(theTransaction.getAgent().getAgentId());
    }

    @Override
    public Double getTotalCommissionByAgent(Transaction theTransaction) throws RemoteException {
        return dao.getTotalCommissionByAgent(theTransaction.getAgent().getAgentId());
    }

    @Override
    public Double getTotalCompanyCommission() throws RemoteException {
        return dao.getTotalCompanyCommission();
    }

    @Override
    public Double getTotalSalesVolume() throws RemoteException {
        return dao.getTotalSalesVolume();
    }

    @Override
    public Double getTotalRentalRevenue() throws RemoteException {
        return dao.getTotalRentalRevenue();
    }
}