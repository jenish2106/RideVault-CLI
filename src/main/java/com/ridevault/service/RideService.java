package com.ridevault.service;

import com.ridevault.DatabaseConnection;
import com.ridevault.dao.WalletDAO;
import com.ridevault.model.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class RideService {

    private final WalletDAO walletDAO = new WalletDAO();
    private static final DecimalFormat df = new DecimalFormat("0.00");

    // ==========================================
    // 1. REAL-WORLD DYNAMIC PRICING ENGINE
    // ==========================================
    public double calculateFare(Location pickup, Location dropoff) {
        // 1. Static Tariff Rules 
        double baseFare = 20.0;
        double ratePerKm = 7.0;
        double ratePerMin = 2.0;
        double minimumFare = 30.0;
        double platformBookingFee = 0.0; // Ready for future monetization

        // 2. Geospatial & Routing Engine (Using actual GPS coordinates)
        double distanceKm = calculateRealWorldDistance(
            pickup.getLat(), pickup.getLng(), 
            dropoff.getLat(), dropoff.getLng()
        ); 
        
        // 3. Traffic & Time Estimation
        double trafficModifier = getRealTimeTrafficModifier(); 
        double estimatedTimeMin = (distanceKm / 0.5) * trafficModifier; // 0.5km/min is base speed
        
        // 4. Algorithmic Surge (Supply vs Demand)
        double surgeMultiplier = calculateSupplyDemandSurge(pickup);

        // --- THE REAL-WORLD FORMULA ---
        
        // Step A: Time & Distance Subtotal
        double routeSubtotal = baseFare + (distanceKm * ratePerKm) + (estimatedTimeMin * ratePerMin);
        
        // Step B: Apply Supply/Demand Surge
        double surgedFare = routeSubtotal * surgeMultiplier;
        
        // Step C: Minimum Fare Floor Protection for Driver
        double fareBeforeFees = Math.max(surgedFare, minimumFare);
        
        // Step D: Add non-surgeable Platform Fees
        double finalCalculatedFare = fareBeforeFees + platformBookingFee;

        // Round to nearest integer for exact billing (Clean UX, no backend logs printed)
        return Math.round(finalCalculatedFare);
    }

    // --- OOP HELPER METHODS ---

    // ==========================================
    // GPS Spherical Distance Engine (Haversine)
    // ==========================================
    private double calculateRealWorldDistance(double lat1, double lon1, double lat2, double lon2) {
        // The average radius of the Earth in kilometers
        final int R = 6371; 

        // Convert GPS degrees to mathematical radians
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        // The Haversine Formula (Calculates the curve over a sphere)
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                 
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // Multiply the curved angle by the Earth's radius to get kilometers
        double distanceKm = R * c; 
        
        // Ensure distance is never exactly 0 if pickup and dropoff are very close
        distanceKm = Math.max(distanceKm, 1.5);
        
        // Round to 1 decimal place (e.g., 7.5 km) for a clean UI
        return Math.round(distanceKm * 10.0) / 10.0;
    }

    private double getRealTimeTrafficModifier() {
        // Simulates fetching live traffic data (1.0 = Clear, 1.5 = Heavy Traffic).
        double traffic = 1.0 + (Math.random() * 0.5);
        return Math.round(traffic * 10.0) / 10.0;
    }

    private double calculateSupplyDemandSurge(Location pickupZone) {
        int activeRiders = 0;
        int activeDrivers = 0;
        
        // 1. REAL DEMAND: Count how many rides are currently PENDING in this exact zone
        String demandSql = "SELECT COUNT(*) FROM Ride WHERE Status = 'PENDING' AND Pickup_Address = ?";
        
        // 2. REAL SUPPLY: Count drivers who are NOT currently busy with an ACCEPTED ride
        String supplySql = "SELECT COUNT(*) FROM Driver WHERE Driver_Id NOT IN (SELECT Driver_Id FROM Ride WHERE Status = 'ACCEPTED')";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmtDemand = conn.prepareStatement(demandSql);
             PreparedStatement pstmtSupply = conn.prepareStatement(supplySql)) {
             
            pstmtDemand.setString(1, pickupZone.getDisplayName());
            ResultSet rsDemand = pstmtDemand.executeQuery();
            if (rsDemand.next()) {
                activeRiders = rsDemand.getInt(1);
            }
            
            ResultSet rsSupply = pstmtSupply.executeQuery();
            if (rsSupply.next()) {
                activeDrivers = rsSupply.getInt(1);
            }
            
        } catch (SQLException e) {
            System.out.println("\u001B[31m[!] Error calculating live surge metrics: " + e.getMessage() + "\u001B[0m");
        }

        // Failsafe: Prevent division by zero if database is completely empty
        if (activeDrivers == 0) activeDrivers = 1;
        
        // Calculate the Supply/Demand Ratio
        double ratio = (double) activeRiders / activeDrivers;
        
        // The Enterprise Surge Math
        double surgeMultiplier = 1.0; 
        
        // Activate surge if demand outpaces supply by 20%
        if (ratio > 1.2) {
            double surgeSensitivity = 0.35; 
            surgeMultiplier = 1.0 + ((ratio - 1.2) * surgeSensitivity);
        }
        
        // Hard Cap to prevent runaway pricing
        double maxSurgeLimit = 3.5; 
        surgeMultiplier = Math.min(surgeMultiplier, maxSurgeLimit);
        
        return Math.round(surgeMultiplier * 10.0) / 10.0;
    }

    // ==========================================
    // 2. REQUEST A RIDE (Rider Portal)
    // ==========================================
    public void requestRide(int riderId, Location pickup, Location dropoff, double estimatedFare) {
        // Omitting Ride_Id so PostgreSQL safely auto-generates it
        String sql = "INSERT INTO Ride (Rider_Id, Status, Pickup_Address, Pickup_Lat, Pickup_Lng, Dropoff_Address, Dropoff_Lat, Dropoff_Lng, Final_Fare) " +
                     "VALUES (?, 'PENDING', ?, ?, ?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, riderId);
            pstmt.setString(2, pickup.getDisplayName());
            pstmt.setDouble(3, pickup.getLat());
            pstmt.setDouble(4, pickup.getLng());
            pstmt.setString(5, dropoff.getDisplayName());
            pstmt.setDouble(6, dropoff.getLat());
            pstmt.setDouble(7, dropoff.getLng());
            pstmt.setDouble(8, estimatedFare);
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("\u001B[32m[SUCCESS] Ride requested successfully! Waiting for a driver to accept.\u001B[0m");
            }
        } catch (SQLException e) {
            System.out.println("\u001B[31m[!] Failed to request ride: " + e.getMessage() + "\u001B[0m");
        }
    }

    // ==========================================
    // 3. SHOW PENDING RIDES (Driver Portal)
    // ==========================================
    public void showPendingRides() {
        String sql = "SELECT Ride_Id, Pickup_Address, Dropoff_Address, Final_Fare FROM Ride WHERE Status = 'PENDING'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
             
            boolean hasRides = false;
            while (rs.next()) {
                hasRides = true;
                System.out.println("Ride ID: [" + rs.getInt("Ride_Id") + "] | Route: " + 
                                   rs.getString("Pickup_Address") + " -> " + 
                                   rs.getString("Dropoff_Address") + " | Fare: Rs. " + 
                                   rs.getDouble("Final_Fare"));
            }
            
            if (!hasRides) {
                System.out.println("\u001B[33mNo pending rides available in your area right now.\u001B[0m");
            }
        } catch (SQLException e) {
            System.out.println("\u001B[31m[!] Error fetching rides: " + e.getMessage() + "\u001B[0m");
        }
    }

    // ==========================================
    // 4. ACCEPT A RIDE (Driver Portal)
    // ==========================================
    public void acceptRide(int rideId, int driverId) {
        String sql = "UPDATE Ride SET Driver_Id = ?, Status = 'ACCEPTED' WHERE Ride_Id = ? AND Status = 'PENDING'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, driverId);
            pstmt.setInt(2, rideId);
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("\u001B[32m[SUCCESS] You have accepted Ride ID " + rideId + "!\u001B[0m");
            } else {
                System.out.println("\u001B[31m[!] Could not accept ride. It may have been taken by another driver or cancelled.\u001B[0m");
            }
        } catch (SQLException e) {
            System.out.println("\u001B[31m[!] Error accepting ride: " + e.getMessage() + "\u001B[0m");
        }
    }

    // ==========================================
    // 5. FETCH EXPECTED FARE (Fraud Prevention)
    // ==========================================
    public double getExpectedFareForRide(int rideId) {
        String sql = "SELECT Final_Fare FROM Ride WHERE Ride_Id = ? AND Status = 'ACCEPTED'"; 
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, rideId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("Final_Fare");
            }
        } catch (SQLException e) {
            System.out.println("\u001B[31m[!] Error fetching ride fare: " + e.getMessage() + "\u001B[0m");
        }
        return -1.0; 
    }

    // ==========================================
    // 6. COMPLETE A RIDE (State Management & Payout)
    // ==========================================
    public void completeRide(int rideId, int driverId, double collectedFare) {
        // Driver takes 80% commission, platform takes 20%
        double driverEarning = Double.parseDouble(df.format(collectedFare * 0.80));

        String sql = "UPDATE Ride SET Status = 'COMPLETED', Final_Fare = ?, Driver_Earning = ?, Completed_At = CURRENT_TIMESTAMP " +
                     "WHERE Ride_Id = ? AND Driver_Id = ? AND Status = 'ACCEPTED'";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setDouble(1, collectedFare);
            pstmt.setDouble(2, driverEarning);
            pstmt.setInt(3, rideId);
            pstmt.setInt(4, driverId);
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // If DB update works, trigger ACID Wallet transaction
                boolean walletSuccess = walletDAO.creditDriverWallet(driverId, rideId, driverEarning);
                
                if (walletSuccess) {
                    System.out.println("\u001B[32m[SUCCESS] Ride Completed! Rs. " + driverEarning + " (80% cut) has been credited to your Wallet.\u001B[0m");
                } else {
                    System.out.println("\u001B[31m[CRITICAL] Ride completed, but wallet credit failed. ACID Rollback triggered.\u001B[0m");
                }
            } else {
                System.out.println("\u001B[31m[!] Could not complete ride. Please verify the Ride ID.\u001B[0m");
            }
        } catch (SQLException e) {
            System.out.println("\u001B[31m[!] Error completing ride: " + e.getMessage() + "\u001B[0m");
        }
    }
}
