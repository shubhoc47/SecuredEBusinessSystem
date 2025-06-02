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
@Table(name = "SMARTPHONES")
@PrimaryKeyJoinColumn(name = "PRODUCT_ID") // For JOINED strategy
@NamedQueries({
    @NamedQuery(name = "Smartphone.findAll", query = "SELECT s FROM Smartphone s")
})
public class Smartphone extends Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private String cellularConnectivity; // e.g., "5G NR, 4G LTE"
    private String locationServices; // e.g., "GPS, GLONASS, Galileo, QZSS"
    private String simCardType; // e.g., "Nano-SIM", "Dual SIM (Nano-SIM, dual stand-by)"

    // Getters and Setters
    public String getCellularConnectivity() { return cellularConnectivity; }
    public void setCellularConnectivity(String cellularConnectivity) { this.cellularConnectivity = cellularConnectivity; }

    public String getLocationServices() { return locationServices; }
    public void setLocationServices(String locationServices) { this.locationServices = locationServices; }

    public String getSimCardType() { return simCardType; }
    public void setSimCardType(String simCardType) { this.simCardType = simCardType; }
}