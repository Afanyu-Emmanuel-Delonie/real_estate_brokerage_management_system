package model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;

/**
 * Agent Entity - Represents real estate agents
 * 
 * @author afany
 */
@Entity
@Table(name = "agents")
public class Agent implements Serializable {
    public static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long agentId;
    
    @Column(unique = true, nullable = false)
    private String agentCode;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String name;  // Agent's full name
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentStatus status = AgentStatus.ACTIVE;
    
    // Salary cap tracking fields
    @Column(nullable = false)
    private Double yearToDateCommission = 0.0;
    
    @Column(nullable = false)
    private Integer commissionYear;
    
    @Column(nullable = false)
    private Double salaryCap = 2000000.0;
    
    @OneToMany(mappedBy = "listedBy", cascade = CascadeType.ALL)
    private List<Property> properties;
    
    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL)
    private List<Transaction> transactions;
    
    @OneToMany(mappedBy = "listingAgent", cascade = CascadeType.ALL)
    private List<Transaction> listingTransactions;
    
    // Transient fields for service layer communication
    @Transient
    private String message;
    
    @Transient
    private String statusMessage;
    
    @Transient
    private String oldPassword;
    
    @Transient
    private double percentageThreshold;
    
    @Transient
    private int limit;

    // Default constructor
    public Agent() {
        this.commissionYear = java.time.Year.now().getValue();
        this.yearToDateCommission = 0.0;
        this.salaryCap = 2000000.0;
        this.status = AgentStatus.ACTIVE;
    }
    
    // Constructor with essential fields
    public Agent(String name, String email, String phone, User user) {
        this();
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.user = user;
    }
    
    // Full constructor
    public Agent(Long agentId, String agentCode, User user, String name, String email, 
                 String phone, AgentStatus status, Integer commissionYear, 
                 Double salaryCap, List<Property> properties, 
                 List<Transaction> transactions, List<Transaction> listingTransactions) {
        this.agentId = agentId;
        this.agentCode = agentCode;
        this.user = user;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.status = status != null ? status : AgentStatus.ACTIVE;
        this.commissionYear = commissionYear != null ? commissionYear : java.time.Year.now().getValue();
        this.salaryCap = salaryCap != null ? salaryCap : 2000000.0;
        this.yearToDateCommission = 0.0;
        this.properties = properties;
        this.transactions = transactions;
        this.listingTransactions = listingTransactions;
    }

    @PrePersist
    public void prePersist() {
        if (this.commissionYear == null) {
            this.commissionYear = java.time.Year.now().getValue();
        }
        if (this.yearToDateCommission == null) {
            this.yearToDateCommission = 0.0;
        }
        if (this.salaryCap == null) {
            this.salaryCap = 2000000.0;
        }
        if (this.status == null) {
            this.status = AgentStatus.ACTIVE;
        }
    }

    @Override
    public String toString() {
        return "Agent{" + 
               "agentId=" + agentId + 
               ", agentCode=" + agentCode + 
               ", name=" + name + 
               ", email=" + email + 
               ", phone=" + phone + 
               ", status=" + status + 
               ", yearToDateCommission=" + yearToDateCommission + 
               ", commissionYear=" + commissionYear + 
               ", salaryCap=" + salaryCap + 
               '}';
    }

    // Getters and Setters
    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public String getAgentCode() {
        return agentCode;
    }

    public void setAgentCode(String agentCode) {
        this.agentCode = agentCode;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public void setStatus(AgentStatus status) {
        this.status = status;
    }

    public Double getYearToDateCommission() {
        return yearToDateCommission;
    }

    public void setYearToDateCommission(Double yearToDateCommission) {
        this.yearToDateCommission = yearToDateCommission;
    }

    public Integer getCommissionYear() {
        return commissionYear;
    }

    public void setCommissionYear(Integer commissionYear) {
        this.commissionYear = commissionYear;
    }

    public Double getSalaryCap() {
        return salaryCap;
    }

    public void setSalaryCap(Double salaryCap) {
        this.salaryCap = salaryCap;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<Transaction> getListingTransactions() {
        return listingTransactions;
    }

    public void setListingTransactions(List<Transaction> listingTransactions) {
        this.listingTransactions = listingTransactions;
    }

    // Transient field getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    // Overloaded for backward compatibility with service layer
    public void setStatus(String status) {
        this.statusMessage = status;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public double getPercentageThreshold() {
        return percentageThreshold;
    }

    public void setPercentageThreshold(double percentageThreshold) {
        this.percentageThreshold = percentageThreshold;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    // Helper method to calculate commission percentage
    public double getCommissionPercentage() {
        if (salaryCap == null || salaryCap == 0) {
            return 0.0;
        }
        return (yearToDateCommission / salaryCap) * 100;
    }
    
    // Helper method to check if agent is at cap
    public boolean isAtCap() {
        return yearToDateCommission != null && salaryCap != null 
               && yearToDateCommission >= salaryCap;
    }
    
    // Helper method to get remaining commission space
    public Double getRemainingCommissionSpace() {
        if (salaryCap == null || yearToDateCommission == null) {
            return 0.0;
        }
        return Math.max(0, salaryCap - yearToDateCommission);
    }
}