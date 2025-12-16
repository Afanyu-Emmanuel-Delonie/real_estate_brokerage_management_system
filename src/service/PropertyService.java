package service;

import model.Property;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * RMI Service Interface for Property Management
 * @author afany
 */
public interface PropertyService extends Remote {

    Property createProperty(Property theProperty) throws RemoteException;
    Property getPropertyByCode(Property theProperty) throws RemoteException;
    Property getPropertyByCode(String propertyCode) throws RemoteException;  // ← ADD THIS
    List<Property> getAllProperties() throws RemoteException;

    List<Property> getPropertiesByStatus(Property theProperty) throws RemoteException;

    List<Property> getPropertiesByAgent(Property theProperty) throws RemoteException;

    List<Property> searchPropertiesByName(Property theProperty) throws RemoteException;

    List<Property> searchPropertiesByLocation(Property theProperty) throws RemoteException;

    Property updateProperty(Property theProperty) throws RemoteException;

    Property changePropertyStatus(Property theProperty) throws RemoteException;

    Property deleteProperty(Property theProperty) throws RemoteException;

    List<Property> getAvailableRentalProperties() throws RemoteException;

    Property toggleRentalAvailability(Property theProperty) throws RemoteException;

    Long countAvailableRentalProperties() throws RemoteException;

    Double getAverageRentalPrice() throws RemoteException;

    Double getAverageSalePrice() throws RemoteException;
}