/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.Implementation;

import dao.PropertyDAO;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import model.Property;
import service.PropertyService;

/**
 *
 * @author afany
 */
public class PropertyServiceImpl extends UnicastRemoteObject implements PropertyService {

    private PropertyDAO dao = new PropertyDAO();

    public PropertyServiceImpl() throws RemoteException {
    }

    @Override
    public Property createProperty(Property theProperty) throws RemoteException {
        Property created = dao.createProperty(theProperty);
        if (created != null) {
            theProperty.setPropertyId(created.getPropertyId());
            theProperty.setPropertyCode(created.getPropertyCode());
            theProperty.setMessage("Property created successfully");
            theProperty.setResponseStatus("SUCCESS");
            return theProperty;
        } else {
            theProperty.setMessage("Failed to create property");
            theProperty.setResponseStatus("ERROR");
            return theProperty;
        }
    }

    @Override
    public Property getPropertyByCode(Property theProperty) throws RemoteException {
        Property found = dao.getPropertyByCode(theProperty.getPropertyCode());
        if (found != null) {
            theProperty = found;
            theProperty.setMessage("Property found");
            theProperty.setResponseStatus("SUCCESS");
        } else {
            theProperty.setMessage("Property not found");
            theProperty.setResponseStatus("ERROR");
        }
        return theProperty;
    }

    @Override
    public Property getPropertyByCode(String propertyCode) throws RemoteException {
        Property theProperty = new Property();
        theProperty.setPropertyCode(propertyCode);
        return getPropertyByCode(theProperty);
    }

    @Override
    public List<Property> getAllProperties() throws RemoteException {
        return dao.getAllProperties();
    }

    @Override
    public List<Property> getPropertiesByStatus(Property theProperty) throws RemoteException {
        return dao.getPropertiesByStatus(theProperty.getStatus());
    }

    @Override
    public List<Property> getPropertiesByAgent(Property theProperty) throws RemoteException {
        return dao.getPropertiesByAgent(theProperty.getListedBy().getAgentId());
    }

    @Override
    public List<Property> searchPropertiesByName(Property theProperty) throws RemoteException {
        return dao.searchPropertiesByName(theProperty.getName());
    }

    @Override
    public List<Property> searchPropertiesByLocation(Property theProperty) throws RemoteException {
        return dao.searchPropertiesByLocation(theProperty.getLocation());
    }

    @Override
    public Property updateProperty(Property theProperty) throws RemoteException {
        boolean updated = dao.updateProperty(theProperty);
        if (updated) {
            theProperty.setMessage("Property updated successfully");
            theProperty.setResponseStatus("SUCCESS");
        } else {
            theProperty.setMessage("Failed to update property");
            theProperty.setResponseStatus("ERROR");
        }
        return theProperty;
    }

    @Override
    public Property changePropertyStatus(Property theProperty) throws RemoteException {
        boolean changed = dao.changePropertyStatus(theProperty.getPropertyId(),
                theProperty.getStatus());
        if (changed) {
            theProperty.setMessage("Property status changed successfully");
            theProperty.setResponseStatus("SUCCESS");
        } else {
            theProperty.setMessage("Failed to change property status");
            theProperty.setResponseStatus("ERROR");
        }
        return theProperty;
    }

    @Override
    public Property deleteProperty(Property theProperty) throws RemoteException {
        boolean deleted = dao.deleteProperty(theProperty.getPropertyId());
        if (deleted) {
            theProperty.setMessage("Property deleted successfully");
            theProperty.setResponseStatus("SUCCESS");
        } else {
            theProperty.setMessage("Failed to delete property");
            theProperty.setResponseStatus("ERROR");
        }
        return theProperty;
    }

    @Override
    public List<Property> getAvailableRentalProperties() throws RemoteException {
        return dao.getAvailableRentalProperties();
    }

    @Override
    public Property toggleRentalAvailability(Property theProperty) throws RemoteException {
        boolean toggled = dao.toggleRentalAvailability(theProperty.getPropertyId(),
                theProperty.getAvailableForRent(),
                theProperty.getMonthlyRentPrice());
        if (toggled) {
            theProperty.setMessage("Rental availability updated successfully");
            theProperty.setResponseStatus("SUCCESS");
        } else {
            theProperty.setMessage("Failed to update rental availability");
            theProperty.setResponseStatus("ERROR");
        }
        return theProperty;
    }

    @Override
    public Long countAvailableRentalProperties() throws RemoteException {
        return dao.countAvailableRentalProperties();
    }

    @Override
    public Double getAverageRentalPrice() throws RemoteException {
        return dao.getAverageRentalPrice();
    }

    @Override
    public Double getAverageSalePrice() throws RemoteException {
        return dao.getAverageSalePrice();
    }
}