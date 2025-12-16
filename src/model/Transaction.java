/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.*;

/**
 *
 * @author afany
 */
@Entity
@Table(name = "transaction")
public class Transaction implements Serializable {

    public static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @Column(unique = true, nullable = false)
    private String transactionCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Double totalCommission;

    private Double agentCommission;
    private Double companyCommission;

    // For dual-agent sales transactions (different listing and selling agents)
    @ManyToOne
    @JoinColumn(name = "listing_agent_id")
    private Agent listingAgent;

    private Double listingAgentCommission;
    private Double sellingAgentCommission;

    // Track if agents were at cap when transaction was created
    @Column(nullable = false)
    private Boolean sellingAgentAtCap = false;

    private Boolean listingAgentAtCap = false;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;


    public Transaction() {}

    // logic for the auto generated code
    @PrePersist
    public void prePersist(){
        if (this.date == null) {
            this.date = LocalDate.now();
        }
        if (this.transactionCode == null) {
            generateTransactionCode();
        }
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getTotalCommission() {
        return totalCommission;
    }

    public void setTotalCommission(Double totalCommission) {
        this.totalCommission = totalCommission;
    }

    public Double getAgentCommission() {
        return agentCommission;
    }

    public void setAgentCommission(Double agentCommission) {
        this.agentCommission = agentCommission;
    }

    public Double getCompanyCommission() {
        return companyCommission;
    }

    public void setCompanyCommission(Double companyCommission) {
        this.companyCommission = companyCommission;
    }

    public Agent getListingAgent() {
        return listingAgent;
    }

    public void setListingAgent(Agent listingAgent) {
        this.listingAgent = listingAgent;
    }

    public Double getListingAgentCommission() {
        return listingAgentCommission;
    }

    public void setListingAgentCommission(Double listingAgentCommission) {
        this.listingAgentCommission = listingAgentCommission;
    }

    public Double getSellingAgentCommission() {
        return sellingAgentCommission;
    }

    public void setSellingAgentCommission(Double sellingAgentCommission) {
        this.sellingAgentCommission = sellingAgentCommission;
    }

    public Boolean getSellingAgentAtCap() {
        return sellingAgentAtCap;
    }

    public void setSellingAgentAtCap(Boolean sellingAgentAtCap) {
        this.sellingAgentAtCap = sellingAgentAtCap;
    }

    public Boolean getListingAgentAtCap() {
        return listingAgentAtCap;
    }

    public void setListingAgentAtCap(Boolean listingAgentAtCap) {
        this.listingAgentAtCap = listingAgentAtCap;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    @Override
    public String toString() {
        return "Transaction{" + "transactionId=" + transactionId + ", transactionCode=" + transactionCode + ", type=" + type + ", amount=" + amount + ", totalCommission=" + totalCommission + ", agentCommission=" + agentCommission + ", companyCommission=" + companyCommission + ", listingAgent=" + listingAgent + ", listingAgentCommission=" + listingAgentCommission + ", sellingAgentCommission=" + sellingAgentCommission + ", sellingAgentAtCap=" + sellingAgentAtCap + ", listingAgentAtCap=" + listingAgentAtCap + ", date=" + date + ", agent=" + agent + ", client=" + client + ", property=" + property + '}';
    }

    public Transaction(Long transactionId, String transactionCode, TransactionType type, Double amount, Double totalCommission, Double agentCommission, Double companyCommission, Agent listingAgent, Double listingAgentCommission, Double sellingAgentCommission, LocalDate date, Agent agent, Client client, Property property) {
        this.transactionId = transactionId;
        this.transactionCode = transactionCode;
        this.type = type;
        this.amount = amount;
        this.totalCommission = totalCommission;
        this.agentCommission = agentCommission;
        this.companyCommission = companyCommission;
        this.listingAgent = listingAgent;
        this.listingAgentCommission = listingAgentCommission;
        this.sellingAgentCommission = sellingAgentCommission;
        this.date = date;
        this.agent = agent;
        this.client = client;
        this.property = property;
    }

    private void generateTransactionCode() {
        if (this.transactionId != null) {
            this.transactionCode = "ZT" + String.format("%03d", this.transactionId);
        }
    }
}