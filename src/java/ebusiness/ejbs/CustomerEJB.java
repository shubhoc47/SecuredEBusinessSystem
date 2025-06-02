/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package ebusiness.ejbs;

import Authentication.Beans.Wuser;
import ebusiness.entities.Customer;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
/**
 *
 * @author shubh
 */
@Stateless
public class CustomerEJB {
    
    private static final Logger LOGGER = Logger.getLogger(CustomerEJB.class.getName());

    @PersistenceContext(unitName = "EBusinessPU")
    private EntityManager em;

    public Customer createCustomer(Customer customer) {
        em.persist(customer);
        return customer;
    }

    public Customer updateCustomer(Customer customer) {
        return em.merge(customer);
    }
    
    public Customer findCustomerById(Long id) {
        LOGGER.info("CustomerEJB: findCustomerById called for ID: " + id);
        Customer customer = em.find(Customer.class, id);
        if (customer != null) {
            // Explicitly initialize the LAZY collection while the entity is managed
            // and the session is active. This forces the orders to be loaded.
            customer.getOrders().size(); // Accessing size() or iterating is enough to trigger load
            LOGGER.info("CustomerEJB: Found customer " + customer.getName() + " with " + customer.getOrders().size() + " orders.");
        } else {
            LOGGER.warning("CustomerEJB: No customer found with ID: " + id);
        }
        return customer;
    }

    public List<Customer> findAllCustomers() {
        TypedQuery<Customer> query = em.createNamedQuery("Customer.findAll", Customer.class);
        return query.getResultList();
    }
    
    public Customer findCustomerByWuser(Wuser wuser) {
        try {
            TypedQuery<Customer> query = em.createNamedQuery("Customer.findByWuser", Customer.class);
            query.setParameter("wuser", wuser);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public List<Customer> findCustomersByName(String nameTerm) { // Renamed param for clarity
        LOGGER.info("CustomerEJB: Searching for customers with name term: '" + nameTerm + "'");
        TypedQuery<Customer> query = em.createNamedQuery("Customer.findByName", Customer.class);
        String searchTerm = "%" + nameTerm.trim().toLowerCase() + "%"; // Prepare for case-insensitive LIKE
        query.setParameter("name", searchTerm);
        LOGGER.info("CustomerEJB: Executing JPQL for findCustomersByName with parameter: '" + searchTerm + "'");
        List<Customer> results = query.getResultList();
        LOGGER.info("CustomerEJB: Found " + (results == null ? 0 : results.size()) + " customers by name.");
        return results != null ? results : new ArrayList<>(); // Return empty list if null
    }
    
    
    public Customer findCustomerByIdWithOrders(Long customerId) {
        LOGGER.info("CustomerEJB: findCustomerByIdWithOrders called for ID: " + customerId);
        if (customerId == null) {
            LOGGER.warning("CustomerEJB: findCustomerByIdWithOrders called with null customerId.");
            return null;
        }
        TypedQuery<Customer> query = em.createNamedQuery("Customer.findByIdWithOrders", Customer.class);
        query.setParameter("customerId", customerId);
        try {
            Customer customer = query.getSingleResult();
            // The orders collection should be populated by JPA due to JOIN FETCH
            // Log the size directly from the fetched entity to confirm
            LOGGER.info("CustomerEJB: Found customer " + (customer != null ? customer.getName() : "null") +
                        " with " + (customer != null && customer.getOrders() != null ? customer.getOrders().size() : "0 or null list") +
                        " orders (using JOIN FETCH).");
            return customer;
        } catch (NoResultException e) {
            LOGGER.warning("CustomerEJB: No customer found with ID (using JOIN FETCH): " + customerId);
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "CustomerEJB: Error in findCustomerByIdWithOrders for ID: " + customerId, e);
            return null;
        }
    }
}