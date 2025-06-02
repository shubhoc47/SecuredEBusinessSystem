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
@Table(name = "LAPTOPS")
@PrimaryKeyJoinColumn(name = "PRODUCT_ID") // For JOINED strategy
@NamedQueries({
    @NamedQuery(name = "Laptop.findAll", query = "SELECT l FROM Laptop l")
})
public class Laptop extends Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private String networkInterface; // e.g., "1000G Ethernet LAN"
    private String hardDrive; // e.g., "1TB SSD", "512GB NVMe"
    private String opticalDrive; // e.g., "DVD-RW", "None"
    private String ports; // e.g., "HDMIx1 USB-Cx1 USB-Ax3" (Added for demonstration screenshot)


    // Getters and Setters
    public String getNetworkInterface() { return networkInterface; }
    public void setNetworkInterface(String networkInterface) { this.networkInterface = networkInterface; }

    public String getHardDrive() { return hardDrive; }
    public void setHardDrive(String hardDrive) { this.hardDrive = hardDrive; }

    public String getOpticalDrive() { return opticalDrive; }
    public void setOpticalDrive(String opticalDrive) { this.opticalDrive = opticalDrive; }

    public String getPorts() { return ports; }
    public void setPorts(String ports) { this.ports = ports; }
}