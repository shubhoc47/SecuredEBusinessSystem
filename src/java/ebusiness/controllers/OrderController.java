/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ebusiness.controllers;

import Authentication.Beans.AutenticationBean;
import Authentication.Beans.Wuser;
import ebusiness.ejbs.CustomerEJB;
import ebusiness.ejbs.OrderEJB;
import ebusiness.ejbs.ProductEJB;
import ebusiness.entities.Customer;
import ebusiness.entities.POrder;
import ebusiness.entities.Product;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped; // Or ViewScoped if form is complex
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shubh
 */
@Named("orderController")
@RequestScoped
public class OrderController implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(OrderController.class.getName());

    @EJB
    private OrderEJB orderEJB;
    @EJB
    private ProductEJB productEJB;
    @EJB
    private CustomerEJB customerEJB;

    @Inject
    private AutenticationBean authBean; // To get current user's customer profile

    private POrder newOrder = new POrder();
    private Long selectedProductId;
    private Long selectedCustomerId; // For admins creating orders for any customer
    private Integer quantity = 1; // Default quantity

    private List<POrder> orderList;
    private List<SelectItem> availableProductItems;
    private List<SelectItem> availableCustomerItems; // For admin

    private Long searchOrderId;
    private POrder foundOrder;

    @PostConstruct
    public void init() {
        // Pre-load products for dropdown
        List<Product> products = productEJB.findAllProducts();
        availableProductItems = products.stream()
                .map(p -> new SelectItem(p.getId(), p.getBrand() + " " + p.getModel() + " - $" + p.getPrice() + " (Stock: " + p.getStockQuantity() + ")"))
                .collect(Collectors.toList());
        
        // For admin to select customer (optional, depends on UI design)
         List<Customer> customers = customerEJB.findAllCustomers();
        availableCustomerItems = customers.stream()
                .map(c -> new SelectItem(c.getId(), c.getName()))
                .collect(Collectors.toList());
    }

    // --- Getters and Setters ---
    public POrder getNewOrder() { return newOrder; }
    public void setNewOrder(POrder newOrder) { this.newOrder = newOrder; }

    public Long getSelectedProductId() { return selectedProductId; }
    public void setSelectedProductId(Long selectedProductId) { this.selectedProductId = selectedProductId; }
    
    public Long getSelectedCustomerId() { return selectedCustomerId; }
    public void setSelectedCustomerId(Long selectedCustomerId) { this.selectedCustomerId = selectedCustomerId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public List<POrder> getOrderList() {
    if (orderList == null) { // Lazy load
        Wuser currentUser = authBean.getAuthenticatedWUser();

        if (currentUser == null) {
            LOGGER.warning("getOrderList: No authenticated user. Should be caught by LoginFilter.");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Authentication Error", "User not logged in."));
            orderList = new ArrayList<>();
            return orderList;
        }

        // Use the isAdmin() method from AutenticationBean
        if (authBean.isAdmin()) {
            LOGGER.info("User '" + currentUser.getUsername() + "' is ADMIN. Fetching all orders.");
            try {
                orderList = orderEJB.findAllOrders(); // This should fetch all orders
                if (orderList == null) {
                    orderList = new ArrayList<>();
                }
                LOGGER.info("Admin fetched " + orderList.size() + " total orders.");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error fetching all orders for admin.", e);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not retrieve all orders."));
                orderList = new ArrayList<>();
            }
        } else {
            // Regular user: Fetch only their orders
            LOGGER.info("User '" + currentUser.getUsername() + "' is REGULAR. Fetching their orders.");
            Customer currentCustomer = customerEJB.findCustomerByWuser(currentUser);

            if (currentCustomer != null) {
                try {
                    orderList = orderEJB.findOrdersByCustomer(currentCustomer);
                    if (orderList == null) {
                        orderList = new ArrayList<>();
                    }
                    LOGGER.info("Found " + orderList.size() + " orders for customer ID: " + currentCustomer.getId() + " (User: " + currentUser.getUsername() + ")");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error fetching orders for customer ID: " + currentCustomer.getId(), e);
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not retrieve your orders."));
                    orderList = new ArrayList<>();
                }
            } else {
                LOGGER.warning("No customer profile found for logged-in user: " + currentUser.getUsername());
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Profile Issue", "Your customer profile is not set up. Cannot display orders."));
                orderList = new ArrayList<>();
            }
        }
    }
    return orderList;
}
    
    public List<SelectItem> getAvailableProductItems() { return availableProductItems; }
    public List<SelectItem> getAvailableCustomerItems() { return availableCustomerItems; }

    public Long getSearchOrderId() { return searchOrderId; }
    public void setSearchOrderId(Long searchOrderId) { this.searchOrderId = searchOrderId; }

    public POrder getFoundOrder() { return foundOrder; }
    public void setFoundOrder(POrder foundOrder) { this.foundOrder = foundOrder; }


    // --- Action Methods ---
    public String doCreateOrder() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            // 1. Validate Customer Selection
            if (selectedCustomerId == null) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Input Error", "Please select a customer."));
                return null; // Stay on the same page
            }
            Customer orderCustomer = customerEJB.findCustomerById(selectedCustomerId);
            if (orderCustomer == null) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Selected customer not found."));
                init(); // Refresh customer list
                return null;
            }

            // 2. Validate Product Selection
            if (selectedProductId == null) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Input Error", "Please select a product."));
                return null;
            }
            Product product = productEJB.findProductById(selectedProductId);
            if (product == null) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Selected product not found."));
                init(); // Refresh product list
                return null;
            }

            // 3. Validate Quantity
            if (quantity == null || quantity <= 0) {
                 context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Input Error", "Quantity must be a positive number."));
                return null;
            }
            if (quantity > product.getStockQuantity()) { // Check against actual product stock
                 context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Stock Error", "Not enough stock for " + product.getBrand() + " " + product.getModel() + ". Available: " + product.getStockQuantity()));
                return null;
            }

            // 4. User Authentication Check (still important)
            Wuser loggedInWuser = authBean.getAuthenticatedWUser();
            if (loggedInWuser == null) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Authentication Error", "You must be logged in to place an order."));
                return "login.xhtml?faces-redirect=true";
            }
         

            // 5. Create and Persist the Order
            POrder orderToCreate = new POrder();
            orderEJB.createOrder(orderToCreate, orderCustomer, product, quantity);

            // Display the customer's name in the success message, matching the screenshot
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                    "Successfully created the order for " + orderCustomer.getName()));


            // 6. Reset Form and Refresh Lists
            this.selectedProductId = null;
            this.selectedCustomerId = null;
            this.quantity = 1;

            init(); // Refreshes availableProductItems (updated stock) and availableCustomerItems
            orderList = null; // Force refresh of the order list

            return "listOrders.xhtml?faces-redirect=true";

        } catch (IllegalArgumentException e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Order Failed", e.getMessage()));
            init();
            return null;
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "System Error", "Could not create order. Please try again later."));
            Logger.getLogger(OrderController.class.getName()).log(Level.SEVERE, "Error creating order", e);
            init();
            return null;
        }
    }

    public String doDeleteOrder(Long orderId) {
        try {
            orderEJB.deleteOrder(orderId);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Order #" + orderId + " deleted."));
            orderList = null; // Refresh list
            // Refresh product list for updated stock in dropdown
            init(); 
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not delete order: " + e.getMessage()));
        }
        return "listOrders.xhtml?faces-redirect=true";
    }
    
    public String doSearchOrder() {
        if (searchOrderId != null) {
            foundOrder = orderEJB.findOrderById(searchOrderId);
            if (foundOrder == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Not Found", "Order with ID " + searchOrderId + " not found."));
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Input Required", "Please enter an Order ID to search."));
            foundOrder = null;
        }
        return "searchOrderResults.xhtml"; 
    }
}
