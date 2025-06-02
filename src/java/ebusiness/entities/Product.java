/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ebusiness.entities;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 *
 * @author shubh
 */

@Entity
@Inheritance(strategy = InheritanceType.JOINED) // As per specification
@Table(name = "PRODUCTS")
@NamedQueries({
    @NamedQuery(name = "Product.findAll", query = "SELECT p FROM Product p"),
    @NamedQuery(name = "Product.findById", query = "SELECT p FROM Product p WHERE p.id = :productId"),
    @NamedQuery(name = "Product.findByBrand", query = "SELECT p FROM Product p WHERE p.brand = :brand"),
    // Only keep the one for case-insensitive partial matching
    @NamedQuery(name = "Product.findByModel", query = "SELECT p FROM Product p WHERE LOWER(p.model) LIKE :model")
    // The line below was the duplicate and has been removed:
    // @NamedQuery(name = "Product.findByModel", query = "SELECT p FROM Product p WHERE p.model = :model"),
})
public abstract class Product implements Serializable { // Abstract class

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    private String displaySize; // e.g., "15.6 inch", "6.1 inch"
    private Double weightGrams; // Weight in grams
    private String operatingSystem;
    private String cameraSpec; // e.g., "12MP Dual", "WebCam"
    private String wifiSpec; // e.g., "802.11ax", "Wi-Fi 6"
    private Double price;
    private String description;

    @Column(nullable = false)
    private Integer stockQuantity;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getDisplaySize() { return displaySize; }
    public void setDisplaySize(String displaySize) { this.displaySize = displaySize; }

    public Double getWeightGrams() { return weightGrams; }
    public void setWeightGrams(Double weightGrams) { this.weightGrams = weightGrams; }

    public String getOperatingSystem() { return operatingSystem; }
    public void setOperatingSystem(String operatingSystem) { this.operatingSystem = operatingSystem; }

    public String getCameraSpec() { return cameraSpec; }
    public void setCameraSpec(String cameraSpec) { this.cameraSpec = cameraSpec; }

    public String getWifiSpec() { return wifiSpec; }
    public void setWifiSpec(String wifiSpec) { this.wifiSpec = wifiSpec; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    @Override
    public String toString() {
        return brand + " " + model;
    }

    // ===== ADD THESE HELPER METHODS =====
    public boolean getIsLaptop() {
        return this instanceof Laptop;
    }

    public boolean getIsSmartphone() {
        return this instanceof Smartphone;
    }
    // =====================================
}