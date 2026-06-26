package com.ridevault.dao;

import com.ridevault.DatabaseConnection;
import com.ridevault.model.Ride;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RideDAO {

    public boolean createRide(Ride newRide) {
        String sql = "INSERT INTO Ride (rider_id, pickup_address, pickup_lat, pickup_lng, " +
                     "dropoff_address, dropoff_lat, dropoff_lng, final_fare, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newRide.getRiderId());
            pstmt.setString(2, newRide.getPickupAddress());
            pstmt.setDouble(3, newRide.getPickupLat()); 
            pstmt.setDouble(4, newRide.getPickupLng()); 
            pstmt.setString(5, newRide.getDropoffAddress());
            pstmt.setDouble(6, newRide.getDropoffLat()); 
            pstmt.setDouble(7, newRide.getDropoffLng()); 
            pstmt.setDouble(8, newRide.getFinalFare()); 
            pstmt.setString(9, newRide.getStatus()); 

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("[!] Database error requesting ride: " + e.getMessage());
            return false;
        }
    }

    public List<Ride> getPendingRides() {
        String sql = "SELECT * FROM Ride WHERE status = 'PENDING'";
        List<Ride> pendingRides = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet resultSet = pstmt.executeQuery()) {

            while (resultSet.next()) {
                Integer driverId = (Integer) resultSet.getObject("driver_id");

                Ride ride = new Ride(
                    resultSet.getInt("ride_id"),
                    resultSet.getInt("rider_id"),
                    driverId,
                    resultSet.getString("pickup_address"),
                    resultSet.getDouble("pickup_lat"), 
                    resultSet.getDouble("pickup_lng"), 
                    resultSet.getString("dropoff_address"),
                    resultSet.getDouble("dropoff_lat"), 
                    resultSet.getDouble("dropoff_lng"), 
                    resultSet.getString("status"),
                    resultSet.getDouble("final_fare"), 
                    resultSet.getTimestamp("requested_at")
                );
                pendingRides.add(ride); 
            }

        } catch (SQLException e) {
            System.out.println("[!] Database error fetching rides: " + e.getMessage());
        }
        return pendingRides; 
    }

    // --- NEW: STRICT ACCEPTANCE RULE ---
    public boolean acceptRideInDB(int rideId, int driverId) {
        // ONLY updates if the ride is currently PENDING!
        String sql = "UPDATE Ride SET driver_id = ?, status = 'ACCEPTED' WHERE ride_id = ? AND status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, driverId); 
            pstmt.setInt(2, rideId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("[!] Database error accepting ride: " + e.getMessage());
            return false;
        }
    }

    // --- NEW: STRICT COMPLETION RULE ---
    public boolean completeRideInDB(int rideId, int driverId) {
        // ONLY updates if THIS exact driver owns it AND the status is ACCEPTED!
        String sql = "UPDATE Ride SET status = 'COMPLETED' WHERE ride_id = ? AND driver_id = ? AND status = 'ACCEPTED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, rideId);
            pstmt.setInt(2, driverId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("[!] Database error completing ride: " + e.getMessage());
            return false;
        }
    }
}
