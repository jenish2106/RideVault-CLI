package com.ridevault.dao;

import com.ridevault.DatabaseConnection;
import com.ridevault.model.FareRule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PricingDAO {

    // --- FETCH STANDARD FARE RULES ---
    public FareRule getFareRuleByCategory(String category) {
        String sql = "SELECT * FROM FARE_RULES WHERE Vehicle_Category = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new FareRule(
                    rs.getInt("fare_id"),
                    rs.getString("vehicle_category"),
                    rs.getDouble("base_fare"),
                    rs.getDouble("per_km_rate"),
                    rs.getDouble("per_min_rate"),
                    rs.getDouble("min_fare")
                );
            }
        } catch (SQLException e) {
            System.out.println("[!] Error fetching fare rules: " + e.getMessage());
        }
        return null;
    }

    // --- FETCH SURGE MULTIPLIER BASED ON LOCATION ---
    public double getActiveSurgeMultiplier(double lat, double lng) {
        // Checks if the user's coordinates fall exactly within a defined surge box
        String sql = "SELECT Multiplier FROM SURGE_PRICING WHERE Is_Active = TRUE " +
                     "AND Lat_Min <= ? AND Lat_Max >= ? " +
                     "AND Lng_Min <= ? AND Lng_Max >= ?";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, lat);
            pstmt.setDouble(2, lat);
            pstmt.setDouble(3, lng);
            pstmt.setDouble(4, lng);
            
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("multiplier"); 
            }
        } catch (SQLException e) {
            System.out.println("[!] Error fetching surge pricing: " + e.getMessage());
        }
        // If no surge zone matches, return standard 1.0x multiplier
        return 1.0; 
    }
}
