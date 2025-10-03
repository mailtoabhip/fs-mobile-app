package com.delhivery.axle.utils

import com.delhivery.axle.api.response.DriverDataResponse
import com.delhivery.axle.api.response.RecommendedDriverResponse

/**
 * Mock data utility for testing driver autocomplete functionality
 */
object MockDriverData {
    
    /**
     * Generate mock driver data for testing
     */
    fun getMockDriverData(): List<DriverDataResponse> {
        return listOf(
            DriverDataResponse("Rajesh Kumar", "9876543210"),
            DriverDataResponse("Amit Singh", "9876543211"),
            DriverDataResponse("Suresh Patel", "9876543212"),
            DriverDataResponse("Ravi Sharma", "9876543213"),
            DriverDataResponse("Vikram Gupta", "9876543214"),
            DriverDataResponse("Anil Verma", "9876543215"),
            DriverDataResponse("Raj Kumar", "9876543216"),
            DriverDataResponse("Manoj Tiwari", "9876543217"),
            DriverDataResponse("Deepak Yadav", "9876543218"),
            DriverDataResponse("Sunil Kumar", "9876543219"),
            DriverDataResponse("Pradeep Singh", "9876543220"),
            DriverDataResponse("Ramesh Kumar", "9876543221"),
            DriverDataResponse("Suresh Kumar", "9876543222"),
            DriverDataResponse("Amit Kumar", "9876543223"),
            DriverDataResponse("Rajesh Singh", "9876543224")
        )
    }
    
    /**
     * Generate mock recommended driver response
     */
    fun getMockRecommendedDriverResponse(): RecommendedDriverResponse {
        return RecommendedDriverResponse(getMockDriverData())
    }
    
    /**
     * Generate mock driver data for specific vehicle numbers
     */
    fun getMockDriverDataForVehicle(vehicleNumber: String): List<DriverDataResponse> {
        return when (vehicleNumber.uppercase()) {
            "UP93BB4455" -> listOf(
                DriverDataResponse("Rajesh Kumar", "9876543210"),
                DriverDataResponse("Amit Singh", "9876543211"),
                DriverDataResponse("Suresh Patel", "9876543212"),
                DriverDataResponse("Rajnish Kumar", "9876543210")
            )
            "UP18YY9090" -> listOf(
                DriverDataResponse("Rajnish Kumar", "9999988888"),
                DriverDataResponse("Pankaj Sharma", "9999977777"),
                DriverDataResponse("Alok Singh", "9999966666"),
                DriverDataResponse("Vikash Kumar", "9999955555")
            )
            "MP09QT0001" -> listOf(
                DriverDataResponse("Rajesh Kumar", "9876543210"),
                DriverDataResponse("Amit Singh", "9876543211"),
                DriverDataResponse("Suresh Patel", "9876543212")
            )
            "MP09QT0002" -> listOf(
                DriverDataResponse("Ravi Sharma", "9876543213"),
                DriverDataResponse("Vikram Gupta", "9876543214"),
                DriverDataResponse("Anil Verma", "9876543215")
            )
            "MP09QT0003" -> listOf(
                DriverDataResponse("Raj Kumar", "9876543216"),
                DriverDataResponse("Manoj Tiwari", "9876543217"),
                DriverDataResponse("Deepak Yadav", "9876543218")
            )
            "DL01AB1234" -> listOf(
                DriverDataResponse("Sunil Kumar", "9876543219"),
                DriverDataResponse("Pradeep Singh", "9876543220"),
                DriverDataResponse("Ramesh Kumar", "9876543221")
            )
            "MH02CD5678" -> listOf(
                DriverDataResponse("Suresh Kumar", "9876543222"),
                DriverDataResponse("Amit Kumar", "9876543223"),
                DriverDataResponse("Rajesh Singh", "9876543224")
            )
            else -> generateMockDriversForAnyVehicle(vehicleNumber)
        }
    }
    
    /**
     * Generate mock drivers for any vehicle number (fallback)
     */
    private fun generateMockDriversForAnyVehicle(vehicleNumber: String): List<DriverDataResponse> {
        // Generate consistent mock data based on vehicle number hash
        val hash = vehicleNumber.hashCode()
        val drivers = mutableListOf<DriverDataResponse>()
        
        // Always include some common drivers
        val commonDrivers = listOf(
            DriverDataResponse("Rajesh Kumar", "9876543210"),
            DriverDataResponse("Amit Singh", "9876543211"),
            DriverDataResponse("Suresh Patel", "9876543212"),
            DriverDataResponse("Ravi Sharma", "9876543213"),
            DriverDataResponse("Vikram Gupta", "9876543214"),
            DriverDataResponse("Anil Verma", "9876543215"),
            DriverDataResponse("Raj Kumar", "9876543216"),
            DriverDataResponse("Manoj Tiwari", "9876543217"),
            DriverDataResponse("Deepak Yadav", "9876543218"),
            DriverDataResponse("Sunil Kumar", "9876543219"),
            DriverDataResponse("Pradeep Singh", "9876543220"),
            DriverDataResponse("Ramesh Kumar", "9876543221"),
            DriverDataResponse("Suresh Kumar", "9876543222"),
            DriverDataResponse("Amit Kumar", "9876543223"),
            DriverDataResponse("Rajesh Singh", "9876543224"),
            DriverDataResponse("Rajnish Kumar", "9999988888"),
            DriverDataResponse("Pankaj Sharma", "9999977777"),
            DriverDataResponse("Alok Singh", "9999966666"),
            DriverDataResponse("Vikash Kumar", "9999955555"),
            DriverDataResponse("Rohit Kumar", "9999944444")
        )
        
        // Select 4-6 drivers based on vehicle number hash for consistency
        val numDrivers = 4 + (kotlin.math.abs(hash) % 3) // 4-6 drivers
        val startIndex = kotlin.math.abs(hash) % (commonDrivers.size - numDrivers)
        
        for (i in startIndex until startIndex + numDrivers) {
            drivers.add(commonDrivers[i % commonDrivers.size])
        }
        
        return drivers
    }
    
    /**
     * Generate mock recommended driver response for specific vehicle
     */
    fun getMockRecommendedDriverResponseForVehicle(vehicleNumber: String): RecommendedDriverResponse {
        return RecommendedDriverResponse(getMockDriverDataForVehicle(vehicleNumber))
    }
    
    /**
     * Filter mock drivers by name query
     */
    fun filterMockDrivers(query: String, vehicleNumber: String? = null): List<DriverDataResponse> {
        val drivers = if (vehicleNumber != null) {
            getMockDriverDataForVehicle(vehicleNumber)
        } else {
            getMockDriverData()
        }
        
        return drivers.filter { driver ->
            driver.driverName?.lowercase()?.contains(query.lowercase()) == true
        }
    }
    
    /**
     * Get mock drivers for any vehicle number with query filtering
     */
    fun getMockDriversForAnyVehicle(vehicleNumber: String, query: String? = null): List<DriverDataResponse> {
        val drivers = getMockDriverDataForVehicle(vehicleNumber)
        
        return if (query.isNullOrEmpty()) {
            drivers
        } else {
            drivers.filter { driver ->
                driver.driverName?.lowercase()?.contains(query.lowercase()) == true
            }
        }
    }
    
    /**
     * Check if mock data is available for a vehicle
     */
    fun hasMockDataForVehicle(vehicleNumber: String): Boolean {
        return true // Always return true since we have fallback data for all vehicles
    }
}
