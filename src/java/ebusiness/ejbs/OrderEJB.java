/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package ebusiness.ejbs;

import ebusiness.entities.Customer;
import ebusiness.entities.POrder;
import ebusiness.entities.Product;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;    // ADD THIS
import java.util.logging.Logger;

/**
 *
 * @author shubh
 */
@Stateless
public class OrderEJB {
    
    private static final Logger LOGGER = Logger.getLogger(OrderEJB.class.getName());

    @PersistenceContext(unitName = "EBusinessPU")
    private EntityManager em;

    @EJB
    private ProductEJB productEJB; // To update stock

    @TransactionAttribute(TransactionAttributeType.REQUIRED) // Ensure atomicity
    public POrder createOrder(POrder order, Customer customer, Product product, int quantity) {
        if (product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough stock for product: " + product.getBrand() + " " + product.getModel());
        }

        order.setCustomer(customer);
        order.setProduct(product);
        order.setQuantity(quantity);
        order.setOrderDate(new Date());
        order.setUnitPriceAtOrder(product.getPrice());
        order.setTotalPrice(product.getPrice() * quantity);

        em.persist(order);
        //em.flush(); // Ensure order gets an ID if needed immediately, usually not necessary

        // Update stock
        productEJB.updateProductStock(product.getId(), -quantity); // Decrease stock

        return order;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void deleteOrder(Long orderId) {
        POrder order = em.find(POrder.class, orderId);
        if (order != null) {
            Product product = order.getProduct();
            int quantity = order.getQuantity();
            em.remove(order);
            // Update stock
            productEJB.updateProductStock(product.getId(), quantity); // Increase stock
        }
    }
    
    public POrder findOrderById(Long id) {
        return em.find(POrder.class, id);
    }

    public List<POrder> findAllOrders() {
    LOGGER.info("OrderEJB: findAllOrders called."); // Add logger here too
    TypedQuery<POrder> query = em.createQuery(
        "SELECT o FROM POrder o JOIN FETCH o.customer c JOIN FETCH o.product p ORDER BY o.id DESC", POrder.class); // Or o.orderDate
    List<POrder> allOrders = query.getResultList();
    LOGGER.info("OrderEJB: findAllOrders returning " + (allOrders == null ? 0 : allOrders.size()) + " orders.");
    return allOrders;
}

    public List<POrder> findOrdersByCustomer(Customer customer) {
        TypedQuery<POrder> query = em.createNamedQuery("POrder.findByCustomer", POrder.class);
        query.setParameter("customer", customer);
        return query.getResultList();
    }
}