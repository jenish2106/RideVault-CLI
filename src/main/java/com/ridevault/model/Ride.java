package com.ridevault.model;

import java.sql.Timestamp;

public class Ride {
    private int rideId;
    private int riderId;       
    private Integer driverId;  
    
    // Core details
    private String pickupAddress;
    private double pickupLat;  // ADDED
    private double pickupLng;  // ADDED
    
    private String dropoffAddress;
    private double dropoffLat; // ADDED
    private double dropoffLng; // ADDED
    
    private String status;     
    private double finalFare; 
    private Timestamp requestedAt;

    public Ride() {}

    // FULL CONSTRUCTOR (For reading from the database)
    public Ride(int rideId, int riderId, Integer driverId, 
                String pickupAddress, double pickupLat, double pickupLng, 
                String dropoffAddress, double dropoffLat, double dropoffLng, 
                String status, double finalFare, Timestamp requestedAt) {
        this.rideId = rideId;
        this.riderId = riderId;
        this.driverId = driverId;
        this.pickupAddress = pickupAddress;
        this.pickupLat = pickupLat;
        this.pickupLng = pickupLng;
        this.dropoffAddress = dropoffAddress;
        this.dropoffLat = dropoffLat;
        this.dropoffLng = dropoffLng;
        this.status = status;
        this.finalFare = finalFare; 
        this.requestedAt = requestedAt;
    }

    // CREATION CONSTRUCTOR (For inserting a new ride)
    public Ride(int riderId, String pickupAddress, double pickupLat, double pickupLng, 
                String dropoffAddress, double dropoffLat, double dropoffLng, double finalFare) {
        this.riderId = riderId;
        this.driverId = null; 
        this.pickupAddress = pickupAddress;
        this.pickupLat = pickupLat;   // ADDED
        this.pickupLng = pickupLng;   // ADDED
        this.dropoffAddress = dropoffAddress;
        this.dropoffLat = dropoffLat; // ADDED
        this.dropoffLng = dropoffLng; // ADDED
        this.status = "PENDING";
        this.finalFare = finalFare; 
    }

    // --- GETTERS & SETTERS ---
    public int getRideId() { return rideId; }
    public void setRideId(int rideId) { this.rideId = rideId; }

    public int getRiderId() { return riderId; }
    public void setRiderId(int riderId) { this.riderId = riderId; }

    public Integer getDriverId() { return driverId; }
    public void setDriverId(Integer driverId) { this.driverId = driverId; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public double getPickupLat() { return pickupLat; }
    public void setPickupLat(double pickupLat) { this.pickupLat = pickupLat; }

    public double getPickupLng() { return pickupLng; }
    public void setPickupLng(double pickupLng) { this.pickupLng = pickupLng; }

    public String getDropoffAddress() { return dropoffAddress; }
    public void setDropoffAddress(String dropoffAddress) { this.dropoffAddress = dropoffAddress; }

    public double getDropoffLat() { return dropoffLat; }
    public void setDropoffLat(double dropoffLat) { this.dropoffLat = dropoffLat; }

    public double getDropoffLng() { return dropoffLng; }
    public void setDropoffLng(double dropoffLng) { this.dropoffLng = dropoffLng; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getFinalFare() { return finalFare; }
    public void setFinalFare(double finalFare) { this.finalFare = finalFare; }

    public Timestamp getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Timestamp requestedAt) { this.requestedAt = requestedAt; }
}