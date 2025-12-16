package service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import model.Transaction;

public interface TransactionService extends Remote {
    public Transaction createTransaction(Transaction theTransaction) throws RemoteException;
    public Transaction getTransactionByCode(Transaction theTransaction) throws RemoteException;
    public List<Transaction> getAllTransactions() throws RemoteException;
    public List<Transaction> getTransactionsByAgent(Transaction theTransaction) throws RemoteException;
    public List<Transaction> getTransactionsByClient(Transaction theTransaction) throws RemoteException;
    public List<Transaction> getTransactionsByProperty(Transaction theTransaction) throws RemoteException;
    public List<Transaction> getTransactionsByType(Transaction theTransaction) throws RemoteException;
    public Transaction updateTransaction(Transaction theTransaction) throws RemoteException;
    public Transaction deleteTransaction(Transaction theTransaction) throws RemoteException;
    public Double getTotalAmountByAgent(Transaction theTransaction) throws RemoteException;
    public Double getTotalCommissionByAgent(Transaction theTransaction) throws RemoteException;
    public Double getTotalCompanyCommission() throws RemoteException;
    public Double getTotalSalesVolume() throws RemoteException;
    public Double getTotalRentalRevenue() throws RemoteException;
}