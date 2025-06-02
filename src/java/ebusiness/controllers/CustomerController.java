/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ebusiness.controllers;

import Authentication.Beans.AutenticationBean; // For linking Wuser
import Authentication.Beans.Wuser;
import ebusiness.ejbs.CustomerEJB;
import ebusiness.entities.Customer;
import ebusiness.entities.POrder;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
//import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.view.ViewScoped; 
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
/**
 *
 * @author shubh
 */
@Named("customerController")
@ViewScoped 
public class CustomerController implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(CustomerController.class.getName());

    @EJB
    private CustomerEJB customerEJB;

    @Inject // To get the logged-in Wuser
    private AutenticationBean authBean;

    private Customer newCustomer = new Customer();
    private List<Customer> customerList;
    private Customer selectedCustomer; // For viewing details
    private List<POrder> selectedCustomerOrders;

    private String searchName;
    private List<Customer> searchResults;
    
    
    // In CustomerController.java
    private List<Long> foundCustomerIds;
    // Add getter and setter
    public List<Long> getFoundCustomerIds() {
        if (foundCustomerIds == null) {
            foundCustomerIds = new ArrayList<>();
        }
        return foundCustomerIds;
    }
    public void setFoundCustomerIds(List<Long> foundCustomerIds) {
        this.foundCustomerIds = foundCustomerIds;
    }
    
    
    // In CustomerController.java
private List<Customer> customersForMultiResultsDisplay;

public List<Customer> getCustomersForMultiResultsDisplay() {
    if (customersForMultiResultsDisplay == null) {
        customersForMultiResultsDisplay = new ArrayList<>();
    }
    return customersForMultiResultsDisplay;
}
// No setter needed usually, populated in init
@PostConstruct
public void init() {
    String currentViewId = "Unknown";
    FacesContext fc = FacesContext.getCurrentInstance();
    ExternalContext ec = null; // Declare here for broader scope if fc is not null

    if (fc != null && fc.getViewRoot() != null) {
        currentViewId = fc.getViewRoot().getViewId();
        ec = fc.getExternalContext(); // Initialize ec if fc is valid
    }
    LOGGER.info("CustomerController @PostConstruct init() called. Controller: " + this.toString() + ", ViewId: " + currentViewId);

    if (fc != null && !fc.isPostback() && ec != null) { // Ensure ec is not null
        if ("/customerDetails.xhtml".equals(currentViewId)) {
            LOGGER.info("Initializing for customerDetails.xhtml view.");
            Map<String, String> params = ec.getRequestParameterMap();
            String idParam = params.get("customerId");
            if (idParam != null && !idParam.trim().isEmpty()) {
                try {
                    Long custId = Long.parseLong(idParam.trim());
                    LOGGER.info("customerId parameter found for customerDetails: " + custId);
                    
                    loadSelectedCustomer(custId);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Invalid customerId parameter for customerDetails: " + idParam, e);
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Invalid customer ID format."));
                    this.selectedCustomer = null; // Explicitly nullify
                    this.selectedCustomerOrders = new ArrayList<>();
                }
            } else {
                LOGGER.warning("customerDetails.xhtml loaded without customerId parameter. selectedCustomer will be null.");
                this.selectedCustomer = null; // Explicitly nullify
                this.selectedCustomerOrders = new ArrayList<>();
            }
        } else if ("/customerMultiResults.xhtml".equals(currentViewId)) {
            LOGGER.info("Initializing for customerMultiResults.xhtml from Flash scope.");
            Flash flash = ec.getFlash();
            // Retrieve searchName first as it's simpler and less likely to be null
            String flashedSearchName = (String) flash.get("searchNameForDisplay");
            this.searchName = (flashedSearchName != null) ? flashedSearchName : "";
            LOGGER.info("Retrieved searchName from Flash: " + this.searchName);

            // Retrieve customerIdsForDisplay
            List<Long> idsFromFlash = (List<Long>) flash.get("customerIdsForDisplay");
            this.customersForMultiResultsDisplay = new ArrayList<>(); // Initialize fresh
            if (idsFromFlash != null && !idsFromFlash.isEmpty()) {
                LOGGER.info("Retrieved " + idsFromFlash.size() + " customer IDs from Flash. Fetching full customer objects.");
                for (Long customerId : idsFromFlash) {
                    Customer customer = customerEJB.findCustomerByIdWithOrders(customerId);
                    if (customer != null) {
                        this.customersForMultiResultsDisplay.add(customer);
                    }
                }
                LOGGER.info("Populated customersForMultiResultsDisplay with " + this.customersForMultiResultsDisplay.size() + " customers.");
            } else {
                LOGGER.info("customerIdsForDisplay from Flash was null or empty.");
            }

            FacesMessage flashedMessage = (FacesMessage) flash.get("multiSearchMessage");
            if (flashedMessage != null) {
                fc.addMessage(null, flashedMessage);
            } else if (this.customersForMultiResultsDisplay.isEmpty() && (this.searchName != null && !this.searchName.isEmpty())) {
                 fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "No Results", "No customers found matching name: '" + this.searchName + "'"));
            }
        }
    }

    // Ensure lists are initialized if still null (defensive, for direct access or other scenarios)
    if (this.searchResults == null) { this.searchResults = new ArrayList<>(); }
    if (this.selectedCustomerOrders == null) { this.selectedCustomerOrders = new ArrayList<>(); }
    if (this.customerList == null) { /* Lazy loaded by getter */ }
    if (this.customersForMultiResultsDisplay == null) { this.customersForMultiResultsDisplay = new ArrayList<>();} // For safety
}

// The loadSelectedCustomer method remains the same:
//private void loadSelectedCustomer(Long customerId) {
//    if (customerId == null) {
//        this.selectedCustomer = null;
//        this.selectedCustomerOrders = new ArrayList<>();
//        LOGGER.info("loadSelectedCustomer called with null customerId.");
//        return;
//    }
//    this.selectedCustomer = customerEJB.findCustomerByIdWithOrders(customerId); // Uses JOIN FETCH
//
//    if (this.selectedCustomer != null) {
//        if (this.selectedCustomer.getOrders() != null) {
//            this.selectedCustomerOrders = new ArrayList<>(this.selectedCustomer.getOrders());
//        } else {
//            this.selectedCustomerOrders = new ArrayList<>();
//        }
//        LOGGER.info("Loaded details for customer ID: " + customerId + ". Name: " + this.selectedCustomer.getName() + ". Orders in controller: " + this.selectedCustomerOrders.size());
//    } else {
//        this.selectedCustomerOrders = new ArrayList<>();
//        LOGGER.warning("Customer not found for ID: " + customerId + " during loadSelectedCustomer (using findByIdWithOrders).");
//    }
//}

// The viewCustomerDetails action method remains the same:
//public String viewCustomerDetails(Long customerId) {
//    LOGGER.info("viewCustomerDetails action called for customer ID: " + customerId);
//    if (customerId == null) {
//         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Invalid customer ID provided."));
//        return "listCustomers.xhtml?faces-redirect=true";
//    }
//    return "customerDetails.xhtml?customerId=" + customerId + "&faces-redirect=true";
//}



    // --- Getters and Setters ---
    public Customer getNewCustomer() { return newCustomer; }
    public void setNewCustomer(Customer newCustomer) { this.newCustomer = newCustomer; }

    public List<Customer> getCustomerList() {
        if (customerList == null) {
            customerList = customerEJB.findAllCustomers();
        }
        return customerList;
    }
    
    public Customer getSelectedCustomer() { return selectedCustomer; }
    public void setSelectedCustomer(Customer selectedCustomer) { this.selectedCustomer = selectedCustomer; }

    public List<POrder> getSelectedCustomerOrders() { return selectedCustomerOrders; }
    public void setSelectedCustomerOrders(List<POrder> selectedCustomerOrders) { this.selectedCustomerOrders = selectedCustomerOrders; }
    
    public String getSearchName() { return searchName; }
    public void setSearchName(String searchName) { this.searchName = searchName; }

    public List<Customer> getSearchResults() {
        if (searchResults == null) {
            searchResults = new ArrayList<>();
        }
        return searchResults;
    }
    public void setSearchResults(List<Customer> searchResults) { this.searchResults = searchResults; }

    // --- Action Methods ---
    public String doCreateCustomer() {
        // This method is for admin creating customers.
        // If registration creates customers, that logic is in AutenticationBean.
        try {
            // For manual creation, wuser might be null or set differently
            customerEJB.createCustomer(newCustomer);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Customer '" + newCustomer.getName() + "' created."));
            newCustomer = new Customer(); // Reset form
            customerList = null; // Refresh list
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not create customer: " + e.getMessage()));
        }
        return "listCustomers.xhtml?faces-redirect=true";
    }
    

    
    private void loadSelectedCustomer(Long customerId) {
        if (customerId == null) {
            this.selectedCustomer = null;
            this.selectedCustomerOrders = new ArrayList<>();
            LOGGER.info("loadSelectedCustomer called with null customerId.");
            return;
        }
        // CALL THE EJB METHOD THAT USES JOIN FETCH
        this.selectedCustomer = customerEJB.findCustomerByIdWithOrders(customerId);

        if (this.selectedCustomer != null) {
            // The orders collection should already be initialized by the JOIN FETCH
            if (this.selectedCustomer.getOrders() != null) {
                this.selectedCustomerOrders = new ArrayList<>(this.selectedCustomer.getOrders());
            } else {
                // This case should be rare if JOIN FETCH worked and customer has orders.
                // Could happen if customer has 0 orders, then getOrders() might be an empty list.
                this.selectedCustomerOrders = new ArrayList<>();
            }
            LOGGER.info("Loaded details for customer ID: " + customerId + ". Name: " + this.selectedCustomer.getName() + ". Orders in controller: " + this.selectedCustomerOrders.size());
        } else {
            this.selectedCustomerOrders = new ArrayList<>();
            LOGGER.warning("Customer not found for ID: " + customerId + " during loadSelectedCustomer (using findByIdWithOrders).");
            // The FacesMessage for "Customer not found" will be added by the viewCustomerDetails action method
            // if selectedCustomer remains null after this.
        }
    }

    public String viewCustomerDetails(Long customerId) {
        LOGGER.info("viewCustomerDetails action called for customer ID: " + customerId);
        // The action method itself no longer needs to load the customer if the
        // details page will load it based on a parameter.
        // It just needs to ensure the ID is valid before redirecting.
        if (customerId == null) {
             FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Invalid customer ID provided."));
            return "listCustomers.xhtml?faces-redirect=true";
        }


        return "customerDetails.xhtml?customerId=" + customerId + "&faces-redirect=true";
    }

    
    // Inside CustomerController.java

// Inside CustomerController.java (@ViewScoped)

public String doSearchCustomers() {
    LOGGER.info("CustomerController: doSearchCustomers action called with searchName input: '" + this.searchName + "'");
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext ec = context.getExternalContext();
    Flash flash = ec.getFlash();

    // Clear previous results from flash or bean state for this action
    flash.remove("customerIdsForDisplay");
    flash.remove("searchNameForDisplay");
    flash.remove("multiSearchMessage");
    this.foundCustomerIds = new ArrayList<>(); // Initialize for this search

    if (this.searchName != null && !this.searchName.trim().isEmpty()) {
        String trimmedSearchName = this.searchName.trim();
        LOGGER.info("Calling customerEJB.findCustomersByName with: '" + trimmedSearchName + "'");
        List<Customer> resultsFromEJB = customerEJB.findCustomersByName(trimmedSearchName);

        if (resultsFromEJB != null && !resultsFromEJB.isEmpty()) {
            LOGGER.info("EJB returned " + resultsFromEJB.size() + " customers for name: '" + trimmedSearchName + "'. Extracting IDs.");
            for (Customer customer : resultsFromEJB) {
                this.foundCustomerIds.add(customer.getId());
            }
            // Put the list of IDs and the search term into Flash scope
            flash.put("customerIdsForDisplay", this.foundCustomerIds);
            flash.put("searchNameForDisplay", this.searchName);
            LOGGER.info("Stored " + this.foundCustomerIds.size() + " customer IDs in Flash.");

        } else { // EJB returned no results
            LOGGER.info("EJB returned no results for name: '" + trimmedSearchName + "'.");
            flash.put("multiSearchMessage", new FacesMessage(FacesMessage.SEVERITY_INFO, "No Results", "No customers found matching name: '" + trimmedSearchName + "'"));
        }
    } else { // searchName was empty
        LOGGER.info("searchName is empty or null. Adding 'Input Required' message to flash.");
        flash.put("multiSearchMessage", new FacesMessage(FacesMessage.SEVERITY_WARN, "Input Required", "Please enter a customer name to search."));
    }

    return "customerMultiResults.xhtml?faces-redirect=true"; // Navigate to the new results page with redirect
}
}