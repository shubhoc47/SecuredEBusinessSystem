/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ebusiness.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author shubh
 */
@Entity
@Table(name = "PRODUCT_ORDERS") // PRODUCT_ORDERS to be more specific
@NamedQueries({
    @NamedQuery(name = "POrder.findAll", query = "SELECT o FROM POrder o"),
    @NamedQuery(name = "POrder.findById", query = "SELECT o FROM POrder o WHERE o.id = :orderId"),
    @NamedQuery(name = "POrder.findByCustomer", query = "SELECT o FROM POrder o WHERE o.customer = :customer")
})
public class POrder implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER) // Eager fetch product to display details
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product; // Each order is for one product item

    @Column(nullable = false)
    private Integer quantity;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date orderDate;

    private Double unitPriceAtOrder; // Price of product when ordered
    private Double totalPrice;

    @PrePersist
    protected void onCreate() {
        orderDate = new Date();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public Double getUnitPriceAtOrder() { return unitPriceAtOrder; }
    public void setUnitPriceAtOrder(Double unitPriceAtOrder) { this.unitPriceAtOrder = unitPriceAtOrder; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
}