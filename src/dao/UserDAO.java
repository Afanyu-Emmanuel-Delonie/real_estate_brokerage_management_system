/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;


import java.util.List;
import model.User;
import org.hibernate.*;

/**
 *
 * @author afany
 */
public class UserDAO {
    
//    this section will cover the validation 
    
//    validation of the emails 
    private boolean isValidEmail(String email){
        if(email == null || email.trim().isEmpty()){
        return false;
        }
        
        String trimmedEmail = email.trim();
        
//        check the number of @ symbols for validation 

        int atCount = 0;
        for ( char c : trimmedEmail.toCharArray()){
            if(c == '@') atCount++;
        }
        if(atCount != 1){
            System.out.println("Email must contain exactly one @ symbol");
            return false;
        }
        
//        check for the number of dots  
          if (trimmedEmail.contains("..")) {
            System.out.println("Email cannot contain consecutive dots");
            return false;
        }
          
//          check if the email start and ends with a special character 
            if (trimmedEmail.startsWith(".") || trimmedEmail.startsWith("@") || 
            trimmedEmail.endsWith(".") || trimmedEmail.endsWith("@")) {
            System.out.println("Email cannot start or end with . or @");
            return false;
            }
            
            // Must end with @vzzbrokerage.com
        if (!trimmedEmail.toLowerCase().endsWith("@vzzbrokerage.com")) {
            System.out.println("Email must be from @vzzbrokerage.com domain");
            return false;
        }
        
        // Extract local part (before @)
        String localPart = trimmedEmail.substring(0, trimmedEmail.indexOf('@'));
        if (localPart.isEmpty()) {
            System.out.println("Email must have a username before @");
            return false;
        }
        
        // Validate local part - only alphanumeric, dots, underscores, hyphens
        String localPartRegex = "^[A-Za-z0-9._-]+$";
        if (!localPart.matches(localPartRegex)) {
            System.out.println("Email username can only contain letters, numbers, dots, underscores, and hyphens");
            return false;
        }
            
        return true;
    }
    
    // check if username already exist
    public boolean usernameExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT COUNT(*) FROM User WHERE username = :username");
            query.setParameter("username", username.trim());
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
    
    //check if email already exits
    private boolean emailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT COUNT(*) FROM User WHERE email = :email");
            query.setParameter("email", email.trim());
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
    
    
    // check if user exits
     private boolean userIdExists(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("SELECT COUNT(*) FROM User WHERE userId = :userId");
            query.setParameter("userId", userId);
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
     
     //Ensuring the user created is valid
      private boolean isValidUser(User user) {
        if (user == null) {
            System.out.println("User object is null");
            return false;
        }
        
        
        // Validate first name
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            System.out.println("First name cannot be empty");
            return false;
        }
        
        // Validate last name
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            System.out.println("Last name cannot be empty");
            return false;
        }
        
        // Validate username
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            System.out.println("Username cannot be empty");
            return false;
        }
        
        if (user.getUsername().length() < 3) {
            System.out.println("Username must be at least 3 characters");
            return false;
        }
        
        if (user.getUsername().length() > 50) {
            System.out.println("Username cannot exceed 50 characters");
            return false;
        }
        
        // Username should only contain alphanumeric and underscores
        if (!user.getUsername().matches("^[A-Za-z0-9_]+$")) {
            System.out.println("Username can only contain letters, numbers, and underscores");
            return false;
        }
        
        // Validate password
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            System.out.println("Password cannot be empty");
            return false;
        }
        
        if (user.getPassword().length() < 8) {
            System.out.println("Password must be at least 8 characters");
            return false;
        }
        
        // Advanced password validation
        if (!isValidPassword(user.getPassword())) {
            return false;
        }
        
        return true;
    }

    //Password validation
       private boolean isValidPassword(String password) {
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if ("!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(c) >= 0) hasSpecial = true;
        }
        
        if (!hasUpper) {
            System.out.println("Password must contain at least one uppercase letter");
            return false;
        }
        if (!hasLower) {
            System.out.println("Password must contain at least one lowercase letter");
            return false;
        }
        if (!hasDigit) {
            System.out.println("Password must contain at least one number");
            return false;
        }
        if (!hasSpecial) {
            System.out.println("Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)");
            return false;
        }
        
        return true;
    }
    
//    Create the user 
    public User createUser(User userObj) {
        // Validation
        if (!isValidUser(userObj)) {
            System.out.println("Validation failed: Invalid user data");
            return null;
        }
        
        // Check for duplicate username
        if (usernameExists(userObj.getUsername())) {
            System.out.println("Error: Username already exists");
            return null;
        }
        
        Session session = null;
        Transaction dbTransaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            dbTransaction = session.beginTransaction();
            session.save(userObj);
            dbTransaction.commit();
            System.out.println("User created successfully with ID: " + userObj.getUserId());
            return userObj;
        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            System.out.println("Error creating user: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
    // generation of user id
    private String generateUserId(String role) {
        if (role == null || role.trim().isEmpty()) {
            System.out.println("Role cannot be empty");
            return null;
        }
        
        String normalizedRole = role.trim().toUpperCase();
        
        if (normalizedRole.equals("ADMIN")) {
            // Check if admin already exists
            if (userIdExists("ZAdmin")) {
                System.out.println("Admin user already exists");
                return null;
            }
            return "ZAdmin";
        } else if (normalizedRole.equals("AGENT")) {
            // Find the next available agent number
            return generateNextAgentId();
        } else {
            System.out.println("Invalid role. Must be ADMIN or AGENT");
            return null;
        }
    }
    
    
//    generate the next user id 
    private String generateNextAgentId() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            
            Query query = session.createQuery(
                "SELECT u.userId FROM User u WHERE u.userId LIKE 'ZA%' ORDER BY u.userId DESC"
            );
            query.setMaxResults(1);
            
            List<String> results = query.list();
            
            if (results.isEmpty()) {
                // First agent
                return "ZA001";
            } else {
                String lastAgentId = results.get(0);
                // Extract number from ZA001, ZA002, etc.
                try {
                    int lastNumber = Integer.parseInt(lastAgentId.substring(2));
                    int nextNumber = lastNumber + 1;
                    // Format with leading zeros (ZA001, ZA002... ZA999)
                    return String.format("ZA%03d", nextNumber);
                } catch (Exception e) {
                    System.out.println("Error parsing agent ID, starting from ZA001");
                    return "ZA001";
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "ZA001"; // Default to first agent on error
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
 // user login logic
     public User login(String username, String password) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Username cannot be empty");
            return null;
        }
        
        if (password == null || password.trim().isEmpty()) {
            System.out.println("Password cannot be empty");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM User WHERE username = :username AND password = :password");
            query.setParameter("username", username.trim());
            query.setParameter("password", password);
            User user = (User) query.uniqueResult();
            
            if (user != null) {
                System.out.println("Login successful for user: " + username);
                return user;
            } else {
                System.out.println("Login failed: Invalid username or password");
                return null;
            }
        } catch (Exception ex) {
            System.out.println("Login error: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
     
    public User getUserById(long userId) {
        if (userId <= 0) {
            System.out.println("Invalid user ID");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            User user = (User) session.get(User.class, userId);
            if (user == null) {
                System.out.println("User not found with ID: " + userId);
            }
            return user;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
     public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Username cannot be empty");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM User WHERE username = :username");
            query.setParameter("username", username.trim());
            User user = (User) query.uniqueResult();
            return user;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    
     public List<User> getAllUsers() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM User");
            List<User> users = query.list();
            return users;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
     
     public List<User> getUsersByRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            System.out.println("Role cannot be empty");
            return null;
        }
        
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery("FROM User WHERE role = :role");
            query.setParameter("role", role.trim().toUpperCase());
            List<User> users = query.list();
            return users;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
     
     public boolean updateUser(User userObj) {
        // Validation
        if (!isValidUser(userObj)) {
            System.out.println("Validation failed: Invalid user data");
            return false;
        }
        
        // Check if user exists
        if (userObj.getUserId() == null || userObj.getUserId() <= 0) {
            System.out.println("Invalid user ID for update");
            return false;
        }
        
        Session session = null;
        Transaction dbTransaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            
            // Check if user exists
            User existingUser = (User) session.get(User.class, userObj.getUserId());
            if (existingUser == null) {
                System.out.println("User not found for update");
                return false;
            }
            
            // If username is being changed, check for duplicates
            if (!existingUser.getUsername().equals(userObj.getUsername())) {
                if (usernameExists(userObj.getUsername())) {
                    System.out.println("Username already exists");
                    return false;
                }
            }
            
            dbTransaction = session.beginTransaction();
            session.update(userObj);
            dbTransaction.commit();
            System.out.println("User updated successfully");
            return true;
        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            System.out.println("Error updating user: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    } 
      
     public boolean deleteUser(long userId) {
        if (userId <= 0) {
            System.out.println("Invalid user ID");
            return false;
        }
        
        Session session = null;
        Transaction dbTransaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            dbTransaction = session.beginTransaction();
            User user = (User) session.get(User.class, userId);
            if (user != null) {
                session.delete(user);
                dbTransaction.commit();
                System.out.println("User deleted successfully");
                return true;
            } else {
                System.out.println("User not found");
                return false;
            }
        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            System.out.println("Error deleting user: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

     public boolean changePassword(int userId, String oldPassword, String newPassword) {
        // Validate inputs
        if (userId <= 0) {
            System.out.println("Invalid user ID");
            return false;
        }
        
        if (newPassword == null || newPassword.length() < 6) {
            System.out.println("New password must be at least 6 characters");
            return false;
        }
        
        Session session = null;
        Transaction dbTransaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            User user = (User) session.get(User.class, userId);
            
            if (user == null) {
                System.out.println("User not found");
                return false;
            }
            
            // Verify old password
            if (!user.getPassword().equals(oldPassword)) {
                System.out.println("Incorrect old password");
                return false;
            }
            
            // Update password
            dbTransaction = session.beginTransaction();
            user.setPassword(newPassword);
            session.update(user);
            dbTransaction.commit();
            System.out.println("Password changed successfully");
            return true;
        } catch (Exception ex) {
            if (dbTransaction != null) {
                dbTransaction.rollback();
            }
            System.out.println("Error changing password: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public boolean changePassword(Long userId, String password) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
