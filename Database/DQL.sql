-- 1. Total revenue and ride count per vehicle category
SELECT
    v.Category AS VehicleCategory,
    COUNT(r.Ride_Id) AS TotalRides,
    SUM(r.Final_Fare) AS TotalRevenue
FROM
    VEHICLE v
JOIN
    Ride r ON v.Vehicle_Id = r.Vehicle_Id
WHERE
    r.Status = 'completed'
GROUP BY v.Category
ORDER BY TotalRevenue DESC;


-- 2. Top 5 highest earning drivers
SELECT
    d.Driver_Id,
    d.FirstName,
    d.LastName,
    COUNT(r.Ride_Id) AS CompletedRides,
    SUM(r.Driver_Earning) AS TotalEarnings
FROM
    Driver d
JOIN
    Ride r ON d.Driver_Id = r.Driver_Id
WHERE
    r.Status = 'completed'
GROUP BY d.Driver_Id, d.FirstName, d.LastName
ORDER BY TotalEarnings DESC
LIMIT 5;


-- 3. Total amount spent by each rider
SELECT
    ri.Rider_Id,
    ri.FirstName,
    ri.LastName,
    COUNT(r.Ride_Id) AS RidesTaken,
    SUM(p.Amount) AS TotalSpent
FROM
    Rider ri
JOIN
    Ride r ON ri.Rider_Id = r.Rider_Id
JOIN
    Payment p ON r.Ride_Id = p.Ride_Id
WHERE
    p.Status = 'SUCCESS'
GROUP BY ri.Rider_Id, ri.FirstName, ri.LastName
ORDER BY TotalSpent DESC;


-- 4. Currently online and verified drivers with their vehicle details
SELECT
    d.Driver_Id,
    d.FirstName,
    d.LastName,
    ds.Last_Known_Lat,
    ds.Last_Known_Lng,
    v.Model,
    v.Plate_No
FROM
    Driver d
JOIN
    Driver_Session ds ON d.Driver_Id = ds.Driver_Id
JOIN
    VEHICLE v ON d.Driver_Id = v.Driver_Id
WHERE
    ds.Status = 'ONLINE' 
    AND d.Is_Verified = TRUE 
    AND v.Is_Verified = TRUE;


-- 5. Payment method distribution and total amounts
SELECT
    p.Method AS PaymentMethod,
    COUNT(p.Payment_Id) AS TotalTransactions,
    SUM(p.Amount) AS TotalVolume
FROM
    Payment p
WHERE
    p.Status = 'SUCCESS'
GROUP BY p.Method
ORDER BY TotalVolume DESC;


-- 6. Promo code usage effectiveness
SELECT
    o.Promo_Code,
    o.Discount_Pct,
    o.Flat_Discount,
    COUNT(r.Ride_Id) AS TimesUsed,
    SUM(r.Final_Fare) AS RevenueGenerated
FROM
    Offers o
JOIN
    Ride r ON o.offer_id = r.offer_id
WHERE
    r.Status = 'completed'
GROUP BY o.Promo_Code, o.Discount_Pct, o.Flat_Discount
ORDER BY TimesUsed DESC;


-- 7. Cancelled rides breakdown with reasons and rider names
SELECT
    r.Ride_Id,
    ri.FirstName AS RiderName,
    r.Cancel_Reason,
    r.Requested_At,
    r.Cancelled_At
FROM
    Ride r
JOIN
    Rider ri ON r.Rider_Id = ri.Rider_Id
WHERE
    r.Status = 'cancelled' 
    AND r.Cancel_Reason IS NOT NULL
ORDER BY r.Cancelled_At DESC;


-- 8. Riders with more than 5 completed rides (Loyal Riders)
SELECT
    ri.Rider_Id,
    ri.FirstName,
    ri.Email,
    COUNT(r.Ride_Id) AS TotalCompletedRides
FROM
    Rider ri
JOIN
    Ride r ON ri.Rider_Id = r.Rider_Id
WHERE
    r.Status = 'completed'
GROUP BY ri.Rider_Id, ri.FirstName, ri.Email
HAVING COUNT(r.Ride_Id) > 5
ORDER BY TotalCompletedRides DESC;


-- 9. Drivers with expiring vehicle insurance (within next 30 days)
SELECT
    d.Driver_Id,
    d.FirstName,
    d.Phone_No,
    v.Model,
    v.insurance_policy_no,
    v.insurance_expiry_date
FROM
    Driver d
JOIN
    VEHICLE v ON d.Driver_Id = v.Driver_Id
WHERE
    v.insurance_expiry_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '30 days'
ORDER BY v.insurance_expiry_date ASC;


-- 10. Completed rides missing a driver rating from the rider
SELECT
    r.Ride_Id,
    ri.FirstName AS RiderName,
    d.FirstName AS DriverName,
    r.Completed_At
FROM
    Ride r
JOIN
    Rider ri ON r.Rider_Id = ri.Rider_Id
JOIN
    Driver d ON r.Driver_Id = d.Driver_Id
LEFT JOIN
    Rating rt ON r.Ride_Id = rt.Ride_Id
WHERE
    r.Status = 'completed' 
    AND r.Rated_By_Rider = FALSE
ORDER BY r.Completed_At DESC;


-- 11. Average distance and duration of completed rides
SELECT
    AVG(r.Distance_Km) AS AvgDistanceKm,
    AVG(r.Duration_Min) AS AvgDurationMinutes,
    MAX(r.Distance_Km) AS LongestRideKm
FROM
    Ride r
WHERE
    r.Status = 'completed';


-- 12. Total credits and debits per driver wallet
SELECT
    w.Driver_Id,
    d.FirstName,
    w.Balance AS CurrentBalance,
    wt.Movement,
    SUM(wt.Amount) AS TotalAmount
FROM
    WALLET w
JOIN
    WALLET_TRANSACTIONS wt ON w.Wallet_Id = wt.Wallet_Id
JOIN
    Driver d ON w.Driver_Id = d.Driver_Id
GROUP BY w.Driver_Id, d.FirstName, w.Balance, wt.Movement
ORDER BY w.Driver_Id, wt.Movement;


-- 13. Investigating extremely low ratings (2 stars or fewer)
SELECT
    r.Ride_Id,
    ri.FirstName AS RiderName,
    d.FirstName AS DriverName,
    rt.Rating_For_Driver,
    rt.Rider_Comment
FROM
    Rating rt
JOIN
    Ride r ON rt.Ride_Id = r.Ride_Id
JOIN
    Rider ri ON r.Rider_Id = ri.Rider_Id
JOIN
    Driver d ON r.Driver_Id = d.Driver_Id
WHERE
    rt.Rating_For_Driver <= 2
ORDER BY rt.Rating_For_Driver ASC;


-- 14. Rides affected by surge pricing and the zone details
SELECT
    r.Ride_Id,
    sp.Zone_Name,
    sp.Multiplier,
    r.Final_Fare,
    r.Requested_At
FROM
    Ride r
JOIN
    SURGE_PRICING sp ON r.Surge_Id = sp.Surge_Id
WHERE
    r.Status = 'completed'
ORDER BY sp.Multiplier DESC, r.Requested_At DESC;


-- 15. Lifecycle tracking of the most recent ride
SELECT
    rsl.Log_Id,
    rsl.Ride_Id,
    rsl.Status,
    rsl.Change_By,
    rsl.Created_At
FROM
    Ride_Status_Log rsl
WHERE
    rsl.Ride_Id = (SELECT MAX(Ride_Id) FROM Ride)
ORDER BY rsl.Created_At ASC;
