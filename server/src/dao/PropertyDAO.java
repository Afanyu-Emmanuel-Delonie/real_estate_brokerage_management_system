package dao;

import java.util.List;
import model.Property;
import model.PropertyStatus;
import model.Agent;
import org.hibernate.*;

/**
 * Enhanced Data Access Object for Property entity
 * Includes rental and listing agent tracking
 * 
 * @author afany
 */
public class PropertyDAO {
    
    private boolean isValidProperty(Property property) {
        if (property == null) {
            System.out.println("Property object is null");
            return false;
        }
        
        // Validate name
        if (property.getName() == null || property.getName().trim().isEmpty()) {
            System.out.println("Property name cannot be empty");
            return false;
        }
        
        if (property.getName().length() > 200) {
            System.out.println("Property name cannot exceed 200 characters");
            return false;
        }
        
        // Validate location
        if (property.getLocation() == null || property.getLocation().trim().isEmpty()) {
            System.out.println("Property location cannot be empty");
            return false;
        }
        
        // Validate price
        if (property.getPrice() == null || property.getPrice() <= 0) {
            System.out.println("Property price must be greater than 0");
            return false;
        }
        
        // Validate agent
        if (property.getListedBy() == null) {
            System.out.println("Property must be listed by an agent");
            return false;
        }
        
        // Validate rental fields if available for rent
        if (property.getAvailableForRent() != null && property.getAvailableForRent()) {
            if (property.getMonthlyRentPrice() == null || property.getMonthlyRentPrice() <= 0) {
                System.out.println("Monthly rent price must be greater than 0 for rental properties");
                return false;
            }
        }
        
        return true;
    }
    
    private boolean propertyCodeExists(String propertyCode) {
        if (propertyCode == null || propertyCode.trim().isEmpty()) {
            return false;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT COUNT(*) FROM Property WHERE propertyCode = :propertyCode");
            query.setParameter("propertyCode", propertyCode);
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
    
    private String generateNextPropertyCode() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            
            Query query = session.createQuery(
                "SELECT p.propertyCode FROM Property p WHERE p.propertyCode LIKE 'ZP%' ORDER BY p.propertyCode DESC"
            );
            query.setMaxResults(1);
            
            List<String> results = query.list();
            
            if (results.isEmpty()) {
                return "ZP001";
            } else {
                String lastPropertyCode = results.get(0);
                try {
                    int lastNumber = Integer.parseInt(lastPropertyCode.substring(2));
                    int nextNumber = lastNumber + 1;
                    return String.format("ZP%03d", nextNumber);
                } catch (Exception e) {
                    System.out.println("Error parsing property code, starting from ZP001");
                    return "ZP001";
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "ZP001";
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    public Property createProperty(Property property) {
        // Validation
        if (!isValidProperty(property)) {
            System.out.println("Validation failed: Invalid property data");
            return null;
        }
        
        // Generate property code
        String propertyCode = generateNextPropertyCode();
        if (propertyCode == null) {
            System.out.println("Error: Could not generate property code");
            return null;
        }
        property.setPropertyCode(propertyCode);
        
        // Set default status if not set
        if (property.getStatus() == null) {
            property.setStatus(PropertyStatus.AVAILABLE);
        }
        
        // Set default rental availability if not set
        if (property.getAvailableForRent() == null) {
            property.setAvailableForRent(false);
        }
        
        Session session = null;
        Transaction dbTransaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            dbTransaction = session.beginTransaction();
            session.save(property);
            dbTransaction.commit();
            System.out.println("Property created successfully with code: " + propertyCode);
            return property;
        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            System.out.println("Error creating property: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public Property getPropertyById(Long propertyId) {
        if (propertyId == null || propertyId <= 0) {
            System.out.println("Invalid property ID");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Property property = (Property) session.get(Property.class, propertyId);
            if (property == null) {
                System.out.println("Property not found with ID: " + propertyId);
            }
            return property;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public Property getPropertyByCode(String propertyCode) {
        if (propertyCode == null || propertyCode.trim().isEmpty()) {
            System.out.println("Property code cannot be empty");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Property WHERE propertyCode = :propertyCode");
            query.setParameter("propertyCode", propertyCode.trim());
            Property property = (Property) query.uniqueResult();
            return property;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<Property> getAllProperties() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Property ORDER BY propertyCode ASC");
            List<Property> properties = query.list();
            return properties;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<Property> getPropertiesByStatus(PropertyStatus status) {
        if (status == null) {
            System.out.println("Status cannot be null");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Property WHERE status = :status ORDER BY propertyCode ASC");
            query.setParameter("status", status);
            List<Property> properties = query.list();
            return properties;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<Property> getPropertiesByAgent(Long agentId) {
        if (agentId == null || agentId <= 0) {
            System.out.println("Invalid agent ID");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Property WHERE listedBy.agentId = :agentId ORDER BY propertyCode ASC");
            query.setParameter("agentId", agentId);
            List<Property> properties = query.list();
            return properties;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<Property> searchPropertiesByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Name cannot be empty");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Property WHERE LOWER(name) LIKE :name ORDER BY name ASC");
            query.setParameter("name", "%" + name.trim().toLowerCase() + "%");
            List<Property> properties = query.list();
            return properties;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<Property> searchPropertiesByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            System.out.println("Location cannot be empty");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Property WHERE LOWER(location) LIKE :location ORDER BY location ASC");
            query.setParameter("location", "%" + location.trim().toLowerCase() + "%");
            List<Property> properties = query.list();
            return properties;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<Property> getPropertiesByPriceRange(Double minPrice, Double maxPrice) {
        if (minPrice == null || maxPrice == null || minPrice < 0 || maxPrice < minPrice) {
            System.out.println("Invalid price range");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM Property WHERE price BETWEEN :minPrice AND :maxPrice ORDER BY price ASC");
            query.setParameter("minPrice", minPrice);
            query.setParameter("maxPrice", maxPrice);
            List<Property> properties = query.list();
            return properties;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public boolean updateProperty(Property property) {
    if (!isValidProperty(property)) {
        System.out.println("Validation failed: Invalid property data");
        return false;
    }
    
    if (property.getPropertyId() == null || property.getPropertyId() <= 0) {
        System.out.println("Invalid property ID for update");
        return false;
    }
    
    Session session = null;
    Transaction dbTransaction = null;
    try {
        session = HibernateUtil.getSessionFactory().openSession();
        dbTransaction = session.beginTransaction();
        
        
        session.merge(property);
        
        dbTransaction.commit();
        System.out.println("Property updated successfully");
        return true;
    } catch (Exception ex) {
        if (dbTransaction != null) {
            dbTransaction.rollback();
        }
        System.out.println("Error updating property: " + ex.getMessage());
        ex.printStackTrace();
        return false;
    } finally {
        if (session != null) {
            session.close();
        }
    }
}

    public boolean changePropertyStatus(Long propertyId, PropertyStatus newStatus) {
        if (propertyId == null || propertyId <= 0) {
            System.out.println("Invalid property ID");
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
            Property property = (Property) session.get(Property.class, propertyId);
            
            if (property == null) {
                System.out.println("Property not found");
                return false;
            }
            
            dbTransaction = session.beginTransaction();
            property.setStatus(newStatus);
            session.update(property);
            dbTransaction.commit();
            System.out.println("Property status changed successfully to: " + newStatus);
            return true;
        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            System.out.println("Error changing property status: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public boolean deleteProperty(Long propertyId) {
        if (propertyId == null || propertyId <= 0) {
            System.out.println("Invalid property ID");
            return false;
        }
        
        Session session = null;
        Transaction dbTransaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            dbTransaction = session.beginTransaction();
            Property property = (Property) session.get(Property.class, propertyId);
            if (property != null) {
                session.delete(property);
                dbTransaction.commit();
                System.out.println("Property deleted successfully");
                return true;
            } else {
                System.out.println("Property not found");
                return false;
            }
        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            System.out.println("Error deleting property: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public long countPropertiesByStatus(PropertyStatus status) {
        if (status == null) {
            return 0;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT COUNT(*) FROM Property WHERE status = :status");
            query.setParameter("status", status);
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

    public long countPropertiesByAgent(Long agentId) {
        if (agentId == null || agentId <= 0) {
            return 0;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT COUNT(*) FROM Property WHERE listedBy.agentId = :agentId");
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
    
    // ========== Rental-Related Methods ==========
    
    /**
     * Get all properties available for rent
     */
    public List<Property> getAvailableRentalProperties() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Property WHERE availableForRent = true AND status = :status ORDER BY monthlyRentPrice ASC"
            );
            query.setParameter("status", PropertyStatus.AVAILABLE);
            List<Property> properties = query.list();
            return properties;
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
     * Get rental properties by price range
     */
    public List<Property> getRentalPropertiesByPriceRange(Double minRent, Double maxRent) {
        if (minRent == null || maxRent == null || minRent < 0 || maxRent < minRent) {
            System.out.println("Invalid rent range");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "FROM Property WHERE availableForRent = true AND monthlyRentPrice BETWEEN :minRent AND :maxRent ORDER BY monthlyRentPrice ASC"
            );
            query.setParameter("minRent", minRent);
            query.setParameter("maxRent", maxRent);
            List<Property> properties = query.list();
            return properties;
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
     * Toggle rental availability for a property
     */
    public boolean toggleRentalAvailability(Long propertyId, boolean available, Double monthlyRent) {
        if (propertyId == null || propertyId <= 0) {
            System.out.println("Invalid property ID");
            return false;
        }
        
        if (available && (monthlyRent == null || monthlyRent <= 0)) {
            System.out.println("Monthly rent must be greater than 0 for rental properties");
            return false;
        }
        
        Session session = null;
        Transaction dbTransaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Property property = (Property) session.get(Property.class, propertyId);
            
            if (property == null) {
                System.out.println("Property not found");
                return false;
            }
            
            dbTransaction = session.beginTransaction();
            property.setAvailableForRent(available);
            if (available) {
                property.setMonthlyRentPrice(monthlyRent);
            }
            session.update(property);
            dbTransaction.commit();
            System.out.println("Rental availability updated successfully");
            return true;
        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            System.out.println("Error updating rental availability: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    /**
     * Count available rental properties
     */
    public long countAvailableRentalProperties() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "SELECT COUNT(*) FROM Property WHERE availableForRent = true AND status = :status"
            );
            query.setParameter("status", PropertyStatus.AVAILABLE);
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
    
    /**
     * Get average rental price
     */
    public Double getAverageRentalPrice() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "SELECT AVG(monthlyRentPrice) FROM Property WHERE availableForRent = true"
            );
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
     * Get average sale price
     */
    public Double getAverageSalePrice() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT AVG(price) FROM Property");
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
}