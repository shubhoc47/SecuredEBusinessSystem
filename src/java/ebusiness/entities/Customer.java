/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ebusiness.entities;

import Authentication.Beans.Wuser; // Import Wuser
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author shubh
 */
@Entity
@Table(name = "CUSTOMERS")
@NamedQueries({
    @NamedQuery(name = "Customer.findAll", query = "SELECT c FROM Customer c ORDER BY c.name"),
    @NamedQuery(name = "Customer.findById", query = "SELECT c FROM Customer c WHERE c.id = :customerId"),
    // THIS IS THE IMPORTANT ONE FOR THIS PROBLEM:
    @NamedQuery(name = "Customer.findByIdWithOrders", query = "SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.orders WHERE c.id = :customerId"),
    @NamedQuery(name = "Customer.findByWuser", query = "SELECT c FROM Customer c WHERE c.wuser = :wuser"),
    @NamedQuery(name = "Customer.findByName", query = "SELECT c FROM Customer c WHERE LOWER(c.name) LIKE :name ORDER BY c.name")
})

public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false) // Explicit column name, good practice
    private String name; // Full name of the customer

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "PHONENUMBER")
    private String phoneNumber;

    @Column(name = "EMAIL", nullable = false) // Email is usually important
    private String email;

    // Link to the Wuser for authentication details
    @OneToOne(fetch = FetchType.LAZY) // LAZY is good default, fetch Wuser only when needed
    @JoinColumn(name = "WUSER_ID", referencedColumnName = "ID", unique = true) // Ensure WUSER_ID column exists in CUSTOMERS table
    private Wuser wuser;

    // Orders placed by this customer
    // FetchType.LAZY is the default for @OneToMany and generally recommended.
    // We will initialize this collection in the EJB when needed for details view.
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderDate DESC") // Optional: To have orders fetched in a default order
    private List<POrder> orders = new ArrayList<>();

    // Constructors
    public Customer() {
    }

    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
    }


    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Wuser getWuser() { return wuser; }
    public void setWuser(Wuser wuser) { this.wuser = wuser; }

    public List<POrder> getOrders() {
        // It's good practice to return a defensive copy or ensure the list is initialized
        // However, for JPA lazy loading, just returning 'orders' is standard.
        // The initialization to ArrayList in the field declaration handles nulls.
        return orders;
    }
    public void setOrders(List<POrder> orders) {
        this.orders = orders;
    }

    // Convenience methods for managing the bidirectional relationship with POrder
    public void addOrder(POrder order) {
        if (order != null) {
            if (this.orders == null) {
                this.orders = new ArrayList<>();
            }
            this.orders.add(order);
            order.setCustomer(this); // Set the other side of the relationship
        }
    }

    public void removeOrder(POrder order) {
        if (order != null && this.orders != null) {
            this.orders.remove(order);
            order.setCustomer(null); // Unset the other side
        }
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Customer)) {
            return false;
        }
        Customer other = (Customer) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        // Useful for dropdowns and logging
        return "ebusiness.entities.Customer[ id=" + id + ", name=" + name + " ]";
    }
}