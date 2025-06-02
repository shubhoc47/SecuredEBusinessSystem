/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB40/StatelessEjbClass.java to edit this template
 */
package ebusiness.ejbs;

import ebusiness.entities.Laptop;
import ebusiness.entities.Product;
import ebusiness.entities.Smartphone;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
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
public class ProductEJB {
    private static final Logger LOGGER = Logger.getLogger(ProductEJB.class.getName());

    @PersistenceContext(unitName = "EBusinessPU")
    private EntityManager em;

    // --- Product (Generic) ---
    public List<Product> findAllProducts() {
        TypedQuery<Product> query = em.createNamedQuery("Product.findAll", Product.class);
        return query.getResultList();
    }

    public Product findProductById(Long id) {
        return em.find(Product.class, id);
    }

    public Product updateProductStock(Long productId, int quantityChange) {
        Product product = findProductById(productId);
        if (product != null) {
            int newStock = product.getStockQuantity() + quantityChange;
            if (newStock < 0) {
                throw new IllegalArgumentException("Stock cannot be negative.");
            }
            product.setStockQuantity(newStock);
            return em.merge(product);
        }
        return null;
    }
    
    public List<Product> findProductsByModel(String model) {
        TypedQuery<Product> query = em.createNamedQuery("Product.findByModel", Product.class);
        query.setParameter("model", "%" + model + "%"); // For partial match
        return query.getResultList();
    }


    // --- Laptop Specific ---
    public Laptop createLaptop(Laptop laptop) {
        em.persist(laptop);
        return laptop;
    }

    public List<Laptop> findAllLaptops() {
        TypedQuery<Laptop> query = em.createNamedQuery("Laptop.findAll", Laptop.class);
        return query.getResultList();
    }

    // --- Smartphone Specific ---
    public Smartphone createSmartphone(Smartphone smartphone) {
        em.persist(smartphone);
        return smartphone;
    }

    public List<Smartphone> findAllSmartphones() {
        TypedQuery<Smartphone> query = em.createNamedQuery("Smartphone.findAll", Smartphone.class);
        return query.getResultList();
    }
    
    public List<Laptop> findLaptopsByModel(String modelTerm) {
        LOGGER.info("ProductEJB: Searching for LAPTOPS with model term: '" + modelTerm + "'");
        TypedQuery<Laptop> query = em.createQuery(
            "SELECT l FROM Laptop l WHERE LOWER(l.model) LIKE :modelParam", Laptop.class);
        query.setParameter("modelParam", "%" + modelTerm.trim().toLowerCase() + "%");
        List<Laptop> results = query.getResultList();
        LOGGER.info("ProductEJB: Found " + (results == null ? 0 : results.size()) + " laptops by model.");
        return results != null ? results : new ArrayList<>();
    }

    public List<Smartphone> findSmartphonesByModel(String modelTerm) {
        LOGGER.info("ProductEJB: Searching for SMARTPHONES with model term: '" + modelTerm + "'");
        TypedQuery<Smartphone> query = em.createQuery(
            "SELECT s FROM Smartphone s WHERE LOWER(s.model) LIKE :modelParam", Smartphone.class);
        query.setParameter("modelParam", "%" + modelTerm.trim().toLowerCase() + "%");
        List<Smartphone> results = query.getResultList();
        LOGGER.info("ProductEJB: Found " + (results == null ? 0 : results.size()) + " smartphones by model.");
        return results != null ? results : new ArrayList<>();
    }
    
}
