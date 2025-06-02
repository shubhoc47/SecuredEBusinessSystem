/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ebusiness.controllers;

import ebusiness.ejbs.ProductEJB;
import ebusiness.entities.Laptop;
import ebusiness.entities.Product;
import ebusiness.entities.Smartphone;
// import jakarta.annotation.PostConstruct;
// import java.util.Map;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped; // Ensure this is ViewScoped
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 *
 * @author shubh
 */
@Named("productController")
@ViewScoped
public class ProductController implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(ProductController.class.getName());

    @EJB
    private ProductEJB productEJB;

    private Laptop newLaptop = new Laptop();
    private Smartphone newSmartphone = new Smartphone();
    private List<Laptop> laptopList;
    private List<Smartphone> smartphoneList;
    private List<Product> productStockList; // For viewing combined stock

    private String searchModel;
    private List<Laptop> laptopSearchResults;
    private List<Smartphone> smartphoneSearchResults;


    // --- Getters and Setters ---
    public Laptop getNewLaptop() { return newLaptop; }
    public void setNewLaptop(Laptop newLaptop) { this.newLaptop = newLaptop; }

    public Smartphone getNewSmartphone() { return newSmartphone; }
    public void setNewSmartphone(Smartphone newSmartphone) { this.newSmartphone = newSmartphone; }

    public List<Laptop> getLaptopList() {
        if (laptopList == null) { laptopList = productEJB.findAllLaptops(); }
        return laptopList;
    }

    public List<Smartphone> getSmartphoneList() {
        if (smartphoneList == null) { smartphoneList = productEJB.findAllSmartphones(); }
        return smartphoneList;
    }

    public List<Product> getProductStockList() {
         if (productStockList == null) { productStockList = productEJB.findAllProducts(); }
        return productStockList;
    }

    public String getSearchModel() { return searchModel; }
    public void setSearchModel(String searchModel) { this.searchModel = searchModel; }

    // Getters and Setters for new search result lists
    public List<Laptop> getLaptopSearchResults() {
        if (laptopSearchResults == null) { laptopSearchResults = new ArrayList<>(); }
        return laptopSearchResults;
    }
    public void setLaptopSearchResults(List<Laptop> laptopSearchResults) { this.laptopSearchResults = laptopSearchResults; }

    public List<Smartphone> getSmartphoneSearchResults() {
        if (smartphoneSearchResults == null) { smartphoneSearchResults = new ArrayList<>(); }
        return smartphoneSearchResults;
    }
    public void setSmartphoneSearchResults(List<Smartphone> smartphoneSearchResults) { this.smartphoneSearchResults = smartphoneSearchResults; }


    // --- Action Methods ---
    public String doCreateLaptop() {
        try {
            productEJB.createLaptop(newLaptop);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Laptop '" + newLaptop.getBrand() + " " + newLaptop.getModel() + "' created."));
            newLaptop = new Laptop(); // Reset form
            laptopList = null; // Refresh list
            productStockList = null; // Refresh combined list
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not create laptop: " + e.getMessage()));
        }
        return "listLaptops.xhtml?faces-redirect=true"; // Or a specific laptop stock page
    }

    public String doCreateSmartphone() {
        try {
            productEJB.createSmartphone(newSmartphone);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Smartphone '" + newSmartphone.getBrand() + " " + newSmartphone.getModel() + "' created."));
            newSmartphone = new Smartphone(); // Reset form
            smartphoneList = null; // Refresh list
            productStockList = null; // Refresh combined list
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not create smartphone: " + e.getMessage()));
        }
        return "listSmartphones.xhtml?faces-redirect=true"; // Or a specific phone stock page
    }
    
    public String doSearchLaptopsByModel() {
        LOGGER.info("ProductController: doSearchLaptopsByModel called with searchModel: '" + searchModel + "'");
        FacesContext context = FacesContext.getCurrentInstance();
        this.laptopSearchResults = new ArrayList<>(); // Clear previous laptop results

        if (searchModel != null && !searchModel.trim().isEmpty()) {
            String modelToSearch = searchModel.trim();
            List<Laptop> found = productEJB.findLaptopsByModel(modelToSearch); // Uses EJB method
            if (found != null) {
                this.laptopSearchResults.addAll(found);
            }
            LOGGER.info("Found " + this.laptopSearchResults.size() + " laptops matching model: '" + modelToSearch + "'");
            if (this.laptopSearchResults.isEmpty()) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "No Results", "No laptops found matching model: '" + modelToSearch + "'"));
            }
        } else {
            // If search model is empty, list all laptops (as per screenshot behavior when page loads after search)
            // Or you could add a message "Please enter a model"
            this.laptopSearchResults.addAll(productEJB.findAllLaptops());
             if (this.laptopSearchResults.isEmpty()) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "No Laptops", "No laptops currently in stock."));
            }
        }
        return null; // Stay on the same page (searchLaptop.xhtml) to display results
    }

    // --- NEW Action Method for Searching Smartphones ---
    public String doSearchSmartphonesByModel() {
        LOGGER.info("ProductController: doSearchSmartphonesByModel called with searchModel: '" + searchModel + "'");
        FacesContext context = FacesContext.getCurrentInstance();
        this.smartphoneSearchResults = new ArrayList<>(); // Clear previous phone results

        if (searchModel != null && !searchModel.trim().isEmpty()) {
            String modelToSearch = searchModel.trim();
            List<Smartphone> found = productEJB.findSmartphonesByModel(modelToSearch); // Uses EJB method
            if (found != null) {
                this.smartphoneSearchResults.addAll(found);
            }
            LOGGER.info("Found " + this.smartphoneSearchResults.size() + " smartphones matching model: '" + modelToSearch + "'");
            if (this.smartphoneSearchResults.isEmpty()) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "No Results", "No phones found matching model: '" + modelToSearch + "'"));
            }
        } else {
            // If search model is empty, list all phones
            this.smartphoneSearchResults.addAll(productEJB.findAllSmartphones());
             if (this.smartphoneSearchResults.isEmpty()) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "No Phones", "No phones currently in stock."));
            }
        }
        return null; // Stay on the same page (searchPhone.xhtml) to display results
    }
}
