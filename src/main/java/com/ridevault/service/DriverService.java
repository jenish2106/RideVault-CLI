package com.ridevault.service;

import com.ridevault.dao.DriverDAO;
import com.ridevault.model.Driver;

public class DriverService {

    private DriverDAO driverDAO;

    public DriverService() {
        this.driverDAO = new DriverDAO();
    }

    // --- DRIVER REGISTRATION LOGIC ---
    public boolean registerNewDriver(String firstName, String middleName, String lastName, 
                                     String email, String phoneNo, String password, String licenseNo) {
        
        if (password == null || password.length() < 6) {
            System.out.println("[!] Registration Failed: Password must be at least 6 characters.");
            return false;
        }

        if (email == null || !email.contains("@") || !email.contains(".")) {
            System.out.println("[!] Registration Failed: Invalid email format.");
            return false;
        }

        if (licenseNo == null || licenseNo.trim().isEmpty()) {
            System.out.println("[!] Registration Failed: A valid License Number is required.");
            return false;
        }

        Driver newDriver = new Driver(firstName, middleName, lastName, email, phoneNo, password, licenseNo);

        boolean isSaved = driverDAO.registerDriver(newDriver);
        
        if (isSaved) {
            System.out.println("Driver Registration Successful! Welcome aboard, " + firstName + ".");
        } else {
            System.out.println("[!] Registration Failed: Email, Phone, or License already exists.");
        }

        return isSaved;
    }

    // --- DRIVER LOGIN LOGIC ---
    public Driver login(String email, String password) {
        return driverDAO.loginDriver(email, password);
    }
}
