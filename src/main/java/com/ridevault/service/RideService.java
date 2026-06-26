package com.ridevault.service;

import com.ridevault.dao.PricingDAO;
import com.ridevault.dao.RideDAO;
import com.ridevault.dao.WalletDAO;
import com.ridevault.model.FareRule;
import com.ridevault.model.Location;
import com.ridevault.model.Ride;
import java.util.List;

public class RideService {

    private RideDAO rideDAO;
    private PricingDAO pricingDAO;
    private WalletDAO walletDAO;

    public RideService() {
        this.rideDAO = new RideDAO();
        this.pricingDAO = new PricingDAO();
        this.walletDAO = new WalletDAO();
    }

    // --- GEOSPATIAL MATH ---
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth Radius in KM
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; 
    }

    // --- DYNAMIC PRICING ENGINE ---
    public double calculateFare(Location pickup, Location dropoff) {
        FareRule rules = pricingDAO.getFareRuleByCategory("Sedan");
        if (rules == null) {
            rules = new FareRule(0, "Fallback", 50.0, 12.0, 2.0, 100.0);
        }

        double distanceKm = calculateDistance(pickup.getLatitude(), pickup.getLongitude(), 
                                              dropoff.getLatitude(), dropoff.getLongitude());
        
        int estimatedDurationMin = (int) (distanceKm / 0.5); 
        if (distanceKm == 0) estimatedDurationMin = 1; 

        double surgeMultiplier = pricingDAO.getActiveSurgeMultiplier(pickup.getLatitude(), pickup.getLongitude());

        double distanceCharge = distanceKm * rules.getPerKmRate();
        double timeCharge = estimatedDurationMin * rules.getPerMinRate();
        
        double rawSubtotal = rules.getBaseFare() + distanceCharge + timeCharge;
        double priceAfterMinCheck = Math.max(rawSubtotal, rules.getMinFare());
        double finalFare = priceAfterMinCheck * surgeMultiplier;

        return Math.round(finalFare * 100.0) / 100.0; 
    }

    // --- RIDE LIFECYCLE ---
    public boolean requestRide(int riderId, Location pickup, Location dropoff, double confirmedFare) {
        if (pickup == dropoff) {
            System.out.println("[!] Invalid Request: Pickup and Drop-off locations cannot be the same.");
            return false;
        }

        Ride newRide = new Ride(riderId, pickup.getDisplayName(), pickup.getLatitude(), pickup.getLongitude(), 
                                dropoff.getDisplayName(), dropoff.getLatitude(), dropoff.getLongitude(), confirmedFare);
        
        boolean isCreated = rideDAO.createRide(newRide);
        if (isCreated) {
            System.out.println(com.ridevault.Main.GREEN + "[SUCCESS] Ride booked! Broadcasting to nearby drivers..." + com.ridevault.Main.RESET);
        } else {
            System.out.println(com.ridevault.Main.RED + "[!] Failed to book ride. Please try again." + com.ridevault.Main.RESET);
        }
        return isCreated;
    }

    public void showPendingRides() {
        List<Ride> pendingRides = rideDAO.getPendingRides();

        if (pendingRides.isEmpty()) {
            System.out.println("No rides currently available in your area.");
        } else {
            System.out.println("\n--- NEARBY RIDE REQUESTS ---");
            for (Ride ride : pendingRides) {
                System.out.println("Ride ID: [" + ride.getRideId() + "] | Pickup: " + ride.getPickupAddress() + 
                                   " | Dropoff: " + ride.getDropoffAddress() + " | Est. Fare: Rs. " + ride.getFinalFare());
            }
        }
    }

    public boolean acceptRide(int rideId, int driverId) {
        boolean isAccepted = rideDAO.acceptRideInDB(rideId, driverId);
        if (isAccepted) {
            System.out.println(com.ridevault.Main.GREEN + "Ride Accepted! Please proceed to the pickup location." + com.ridevault.Main.RESET);
        } else {
            System.out.println(com.ridevault.Main.RED + "[!] Failed to accept. Invalid ID, or another driver already took it." + com.ridevault.Main.RESET);
        }
        return isAccepted;
    }

    public boolean completeRide(int rideId, int driverId, double finalFare) {
        boolean isCompleted = rideDAO.completeRideInDB(rideId, driverId);
        
        if (isCompleted) {
            // Driver keeps 80% of the fare, Platform keeps 20%
            double driverEarnings = Math.round((finalFare * 0.80) * 100.0) / 100.0;
            
            // Execute the secure ACID transaction
            boolean paymentSuccess = walletDAO.creditDriverWallet(driverId, rideId, driverEarnings);
            
            if (paymentSuccess) {
                System.out.println(com.ridevault.Main.GREEN + "Ride Completed! Rs. " + driverEarnings + " has been credited to your Wallet." + com.ridevault.Main.RESET);
            } else {
                System.out.println(com.ridevault.Main.RED + "Ride completed, but wallet credit failed. Please contact support." + com.ridevault.Main.RESET);
            }
        } else {
            System.out.println(com.ridevault.Main.RED + "[!] Error: Invalid ID. You can only complete rides assigned to you." + com.ridevault.Main.RESET);
        }
        return isCompleted;
    }
}