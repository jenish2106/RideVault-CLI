package com.ridevault.model;

public class Vehicle {
    private int vehicleId;
    private int driverId; // Foreign Key linking to Driver!
    private String plateNo;
    private String model;
    private String color;

    public Vehicle() {}

    public Vehicle(int vehicleId, int driverId, String plateNo, String model, String color) {
        this.vehicleId = vehicleId;
        this.driverId = driverId;
        this.plateNo = plateNo;
        this.model = model;
        this.color = color;
    }

    public Vehicle(int driverId, String plateNo, String model, String color) {
        this.driverId = driverId;
        this.plateNo = plateNo;
        this.model = model;
        this.color = color;
    }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public int getDriverId() { return driverId; }
    public void setDriverId(int driverId) { this.driverId = driverId; }

    public String getPlateNo() { return plateNo; }
    public void setPlateNo(String plateNo) { this.plateNo = plateNo; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}