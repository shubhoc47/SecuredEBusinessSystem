package Authentication.Beans;

import java.io.Serializable;
//import jakarta.faces.bean.ManagedBean;
//import jakarta.faces.bean.SessionScoped;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Random;

import ebusiness.ejbs.CustomerEJB; 
import ebusiness.entities.Customer;  
import jakarta.ejb.EJB; 

import java.io.Serializable;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Random;

import ebusiness.ejbs.CustomerEJB;
import ebusiness.entities.Customer;
import jakarta.ejb.EJB;

@Named (value="authBean")
@SessionScoped
public class AutenticationBean implements Serializable {

    @PersistenceContext(unitName = "EBusinessPU") 
    private EntityManager em;
    @Resource
    private UserTransaction utx;

  
    @EJB
    private CustomerEJB customerEJB;

    //The session username and password
    private String username;
    private String password;
    //The verfication password
    private String passwordv;
    //The user first name
    private String fname;
    //The user last name
    private String lname;
    //The user email address
    private String email;
    //The verfication code entered by the user
    private String verificationcode;
    //The verficvation code that is sent to the user's emnail
    private String verificationcode1;
    //Login state flag
    private boolean Logged=false;
    private boolean recovery=false;
    /*The user entity
        1. consisting of first name, last name, username, password, email and since date
        2. providing JPQL queries based on the above attributes*/
    private Wuser ruser; // Used for recovery process

    private Wuser authenticatedWUser;

    public AutenticationBean() {
    }

    // --- Standard Getters and Setters ---
    public boolean isLogged() {
        return Logged;
    }
    public void setLogged(boolean Logged) {
        this.Logged = Logged;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getPasswordv() {
        return passwordv;
    }
    public void setPasswordv(String passwordv) {
        this.passwordv = passwordv;
    }
    public String getFname() {
        return fname;
    }
    public void setFname(String fname) {
        this.fname = fname;
    }
    public String getLname() {
        return lname;
    }
    public void setLname(String lname) {
        this.lname = lname;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getVerificationcode() {
        return verificationcode;
    }
    public void setVerificationcode(String verificationcode) {
        this.verificationcode = verificationcode;
    }
    public String getVerificationcode1() {
        return verificationcode1;
    }
    public void setVerificationcode1(String verificationcode1) {
        this.verificationcode1 = verificationcode1;
    }
    public boolean isRecovery() {
        return recovery;
    }
    public void setRecovery(boolean recovery) {
        this.recovery = recovery;
    }
    public Wuser getRuser() {
        return ruser;
    }
    public void setRuser(Wuser ruser) {
        this.ruser = ruser;
    }

 
    public Wuser getAuthenticatedWUser() {
        return authenticatedWUser;
    }
   
    public void setAuthenticatedWUser(Wuser authenticatedWUser) {
        this.authenticatedWUser = authenticatedWUser;
    }
    
    public boolean isAdmin() {
        if (authenticatedWUser != null) {
            
            return "admin".equalsIgnoreCase(authenticatedWUser.getUsername());
        }
        return false;
    }


    // --- Core Logic Methods ---

    //Generate the hash code of a password
    public String HashConvert(String oripassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(oripassword.getBytes());
            byte byteData[] = md.digest();
            //convert the byte to hex format
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
                throw new UnsupportedOperationException(e);
        }
    }

    /*Validate an existing user by checking
        1. if the user is a registered user
        2. if the username and password are correct*/
    public String validateUser() {
        FacesContext context = FacesContext.getCurrentInstance();
        Wuser user = getUser(); // This uses the 'username' field of this bean
        if (user != null) {
            String p1=user.getPassword();
            String p2=HashConvert(password); // This uses the 'password' field of this bean
            if (!p1.equals(p2)) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                            "Login Failed!", "The password specified is not correct.");
                context.addMessage(null, message);
                return null;
            }
            Logged=true;
            this.authenticatedWUser = user; // Store the full Wuser object

            return "default.xhtml?faces-redirect=true";
        } else {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Login Failed!", "Username '"+ username+"' does not exist.");
            context.addMessage(null, message);
            return null;
        }
    }

    /*Recover a user by email address*/
    public void recoverUser() {
        FacesContext context = FacesContext.getCurrentInstance();
        Wuser user = getUserbyEmail(); // This uses the 'email' field of this bean
        if (user != null) {
            this.ruser=user; // Store user details for the recovery form
        } else {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
               "Invalid Email!", "Email '"+ email+"' does not exist.");
            context.addMessage(null, message);
        }
    }

    //Retrieve a user by username
    private Wuser getUser() {
        try {
            Wuser user = (Wuser)
            em.createNamedQuery("Wuser.findByUsername").
                    setParameter("username", username).getSingleResult(); // uses this.username
            return user;
        } catch (NoResultException nre) {
            return null;
        }
    }

    //Retrieve a user by email address
    private Wuser getUserbyEmail() {
        try {
            Wuser user = (Wuser)
            em.createNamedQuery("Wuser.findByEmail").
                    setParameter("email", email).getSingleResult(); // uses this.email
            return user;
        } catch (NoResultException nre) {
            return null;
        }
    }

    //Generate a recovery string of 20 character length
    public String createRecoveryCode(){
        if (email == null || email.trim().isEmpty()) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email Required", "Please enter your email address."));
            return null;
        }
        recoverUser(); // Check if email exists and populate ruser
        if (ruser == null) { // recoverUser() would have added a message if email doesn't exist
            return null; // Stay on the same page
        }
        recovery=true;
        String urpage=createRandomCode();
        return urpage; // This will be "userRecovery.xhtml" if successful
    }

   
    public String createVerificationCode(){
        if (email == null || email.trim().isEmpty()) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email Required", "Please enter your email address."));
            return null;
        }
        recovery=false;
        Wuser usr=this.getUserbyEmail(); // checks if email is already registered
        if (usr==null) { // Email is not yet registered, proceed
            String urpage=createRandomCode();
            return urpage; // This will be "register.xhtml" if successful
        } else {
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "Email '" + email + "' already registered!  Please choose a different email.",
                                        "Please choose a different email.");
            context.addMessage(null, message);
            
            return null; // Stay on emailVerification.xhtml
        }
    }

    
    public String createRandomCode() {
        //The sending of email uses a fake SMTP server
        Properties props = new Properties();
        props.put("mail.smtp.host", "localhost");
        props.put("mail.smtp.port", 2525); // Ensure CentreMail is listening here
        Session mailSession = Session.getDefaultInstance(props); // Renamed from 'session' to avoid conflict
        MimeMessage message = new MimeMessage(mailSession);
        try {
            message.setFrom(new InternetAddress("CENTRE@glassfish.com")); 
            InternetAddress[] address = {new InternetAddress(email)};
            message.setRecipients(Message.RecipientType.TO, address);

            String codeSubject;
            String codeText;
            String generatedCode = genVerificationCode(); // Generates and stores in this.verificationcode1

            if (!recovery) { // It's for registration
                codeSubject = "The Verification Code";
                codeText = "The Verification Code: "+ generatedCode;
            } else { // It's for password recovery
                codeSubject = "The Recovery Code";
                codeText = "The Recovery Code: "+ generatedCode;
            }
            message.setSubject(codeSubject);
            message.setSentDate(new Date());
            message.setText(codeText);

            Transport.send(message);
            Logger.getLogger(AutenticationBean.class.getName()).log(Level.INFO, (recovery ? "Recovery" : "Verification") + " Code Sent to " + email + ": " + generatedCode);

        } catch (MessagingException ex) {
            Logger.getLogger(AutenticationBean.class.getName()).log(Level.SEVERE, "Error sending email", ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email Error", "Could not send the code. Please try again later or contact support."));
            return null; // Stay on the current page
        }

        if (recovery) {
            
            return "userRecovery.xhtml?faces-redirect=true";
        } else {
            return "register.xhtml?faces-redirect=true";
        }
    }

    //Generate a random string of length of 20 characters
    public String genVerificationCode() {
        //The character pool
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "1234567890"
                + "abcdefghijklmnopqrstuvwxyz"
                + "!@#$%&*-+=?";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 20) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        this.verificationcode1=saltStr; // Store the generated code
        return saltStr;
    }

    //Register a user into database
    public String createUser() {
        FacesContext context = FacesContext.getCurrentInstance();

        // Basic validations
        if (!password.equals(passwordv)) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password Mismatch", "The specified passwords do not match, please try again!");
            context.addMessage(null, message);
            return null;
        }
        if (this.verificationcode1 == null || !verificationcode.equals(verificationcode1)) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Verification Failed", "Wrong verification code, please try again!");
            context.addMessage(null, message);
            return null;
        }
        // Check if username or email already exists (again, just in case, though createVerificationCode checks email)
        Wuser existingUserByUsername = getUser(); // Checks this.username
        if (existingUserByUsername != null) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Username Taken", "Username '" + username + "' already registered! Please choose a different username.");
            context.addMessage(null, message);
            return null;
        }


        Wuser newWuser = new Wuser();
        newWuser.setFirstname(fname);
        newWuser.setLastname(lname);
        newWuser.setPassword(HashConvert(password));
        newWuser.setUsername(username);
        newWuser.setSince(new Date());
        newWuser.setEmail(email); // email is already a field in this bean

        Customer newCustomer = new Customer();
        newCustomer.setName(fname + " " + lname); // Combine first and last for customer name
        newCustomer.setEmail(email); // Use the same email for the customer profile
        newCustomer.setWuser(newWuser); // Link the Wuser to the Customer

        try {
            utx.begin();
            em.persist(newWuser);
            // After newWuser is persisted and has an ID, newCustomer can be persisted.
            // The WUSER_ID foreign key in CUSTOMERS table will reference newWuser's ID.
            em.persist(newCustomer);
            utx.commit();

            //Reset the user input fields
            clearRegistrationFields();
            Logged = false; // Ensure not logged in after registration, force login
            authenticatedWUser = null;

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Registration Successful", "Your account has been created. Please login."));
            return "login.xhtml?faces-redirect=true"; // Redirect to login after successful registration

        } catch (Exception e) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Error creating user!",
                    "Unexpected error when creating your account. Please contact the system Administrator.");
            context.addMessage(null, message);
            Logger.getLogger(AutenticationBean.class.getName()).log(Level.SEVERE, "Unable to create new user",e);
            try { if(utx != null && utx.getStatus() == jakarta.transaction.Status.STATUS_ACTIVE) utx.rollback(); } catch (Exception ex) { Logger.getLogger(AutenticationBean.class.getName()).log(Level.SEVERE, "Rollback failed",ex);}
            return null;
        }
    }

    //Reset a user from the recovery procedure
    public String resetUser() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (ruser == null) { // ruser should be populated from createRecoveryCode -> recoverUser
             FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Recovery Error", "User details not found for recovery. Please start over.");
            context.addMessage(null, message);
            return "emailRecovery.xhtml?faces-redirect=true";
        }

        if (!password.equals(passwordv)) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password Mismatch", "The specified new passwords do not match, please try again!");
            context.addMessage(null, message);
            return null;
        }
        if (this.verificationcode1 == null || !verificationcode.equals(verificationcode1)) {
            // verificationcode is user input, verificationcode1 is the one sent
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Recovery Failed", "Wrong recovery code, please try again!");
            context.addMessage(null, message);
            return null;
        }

        ruser.setPassword(HashConvert(password)); // Update password on the fetched ruser object
        try {
            utx.begin();
            em.merge(ruser); // Merge changes to the database
            utx.commit();

            //Reset the user input fields
            clearRecoveryFields();
            Logged = false; // Ensure not logged in
            authenticatedWUser = null;
            recovery = false; // Reset recovery flag

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Password Reset", "Your password has been reset. Please login with your new password."));
            return "login.xhtml?faces-redirect=true";

        } catch (Exception e) {
          FacesMessage message = new FacesMessage(
                  FacesMessage.SEVERITY_ERROR, "Error recovering user!",
                  "Unexpected error when recovering your account. Please contact the system Administrator.");
          context.addMessage(null, message);
          Logger.getLogger(AutenticationBean.class.getName()).log(Level.SEVERE, "Unable to recover user", e);
          try { if(utx != null && utx.getStatus() == jakarta.transaction.Status.STATUS_ACTIVE) utx.rollback(); } catch (Exception ex) { Logger.getLogger(AutenticationBean.class.getName()).log(Level.SEVERE, "Rollback failed",ex);}
          return null;
        }
    }

    // Method to be called by LoginFilter or a logout action
    public void logout() {
        this.Logged = false;
        this.authenticatedWUser = null;
        this.username = null;
        this.password = null;
        // Clear other sensitive session data if necessary

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.getExternalContext() != null) {
            facesContext.getExternalContext().invalidateSession();
        }
    }

    private void clearRegistrationFields() {
        this.username=null;
        this.password=null;
        this.passwordv=null;
        this.fname=null;
        this.lname=null;
        this.verificationcode=null;
        this.verificationcode1=null;
        this.email=null;
    }
     private void clearRecoveryFields() {
        this.password=null;
        this.passwordv=null;
        this.verificationcode=null;
        this.verificationcode1=null;
        this.ruser = null;
    }
}