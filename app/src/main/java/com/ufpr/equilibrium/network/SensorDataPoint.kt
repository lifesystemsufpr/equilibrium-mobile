package com.ufpr.equilibrium.network

import com.google.gson.annotations.SerializedName

/**
 * DTO for merged sensor data (accelerometer + gyroscope).
 * Each data point represents a single time sample with both accel and gyro readings.
 * 
 * IMPORTANT: Fields must match API schema exactly.
 * API expects: timestamp, accel_x, accel_y, accel_z, gyro_x, gyro_y, gyro_z
 */
data class SensorDataPoint(
    @SerializedName("timestamp")
    val timestamp: String,  // ISO 8601 timestamp string
    
    @SerializedName("accel_x")
    val accel_x: Double,
    
    @SerializedName("accel_y")
    val accel_y: Double,
    
    @SerializedName("accel_z")
    val accel_z: Double,
    
    @SerializedName("gyro_x")
    val gyro_x: Double,
    
    @SerializedName("gyro_y")
    val gyro_y: Double,
    
    @SerializedName("gyro_z")
    val gyro_z: Double
)
