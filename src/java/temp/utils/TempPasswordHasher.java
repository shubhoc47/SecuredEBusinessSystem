/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package temp.utils;

/**
 *
 * @author shubh
 */
import Authentication.Beans.AutenticationBean; 

public class TempPasswordHasher {

    public static void main(String[] args) {
        // Create an instance of AutenticationBean to use its HashConvert method.
        // This assumes AutenticationBean has a public no-argument constructor
        // and the HashConvert method is public.
        AutenticationBean authBean = new AutenticationBean();

        String passwordForUserA = "pass!"; // Example password for Alice
        String passwordForUserB = "pass1"; // Example password for Bob
        String passwordForAdmin = "pass2"; // Example for an admin user if needed

        // Hash the passwords
        String hashedPasswordForUserA = authBean.HashConvert(passwordForUserA);
        String hashedPasswordForUserB = authBean.HashConvert(passwordForUserB);
        String hashedPasswordForAdmin = authBean.HashConvert(passwordForAdmin);


        // Print the results to the console
        System.out.println("--- Hashed Passwords ---");

        System.out.println("Plain: " + passwordForUserA);
        System.out.println("Hashed for User A: " + hashedPasswordForUserA);
        System.out.println("--------------------------");

        System.out.println("Plain: " + passwordForUserB);
        System.out.println("Hashed for User B: " + hashedPasswordForUserB);
        System.out.println("--------------------------");

        System.out.println("Plain: " + passwordForAdmin);
        System.out.println("Hashed for Admin: " + hashedPasswordForAdmin);
        System.out.println("--------------------------");
    }
}
