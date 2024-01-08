package com.shermwebdev.rentmy.database

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.shermwebdev.data.Modification
import com.shermwebdev.rentmy.data.Vehicle
import com.shermwebdev.rentmy.data.VehicleAddress
import com.shermwebdev.rentmy.data.VehicleOptions
import com.shermwebdev.rentmy.data.VehiclePhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicle_options WHERE vehicle_options_vehicle_id = :vehicleId")
    fun getVehicleOptionsById(vehicleId: Int): Flow<VehicleOptions>

    @Query(
        "SELECT * FROM vehicle_addresses WHERE vehicle_addresses.lat >= :latitude - 1 AND vehicle_addresses.lat <= :latitude + 1 AND vehicle_addresses.long >= :longitude - 1 AND vehicle_addresses.long <= :longitude + 1"
    )
    fun getAddressesByLocation(latitude: Double, longitude: Double): Flow<List<VehicleAddress>>


    @RawQuery(observedEntities = arrayOf(VehicleAddress::class, VehicleAddress::class, VehicleOptions::class))
    fun getAddressesByLocationWithFilters(sql: SupportSQLiteQuery): Flow<List<VehicleAddress>>

    @Query(
        "SELECT * FROM vehicles JOIN vehicle_addresses ON vehicles.vehicle_id = vehicle_addresses.owner_vehicle_id WHERE vehicle_addresses.lat >= :latitude - 1 AND vehicle_addresses.lat <= :latitude + 1 AND vehicle_addresses.long >= :longitude - 1 AND vehicle_addresses.long <= :longitude + 1"
    )
    fun getVehiclesByLocation(latitude: Double, longitude: Double): Flow<List<Vehicle>>

    @RawQuery(observedEntities = arrayOf(Vehicle::class, VehicleAddress::class, VehicleOptions::class))
    fun getVehiclesByLocationWithFilters(sql: SupportSQLiteQuery): Flow<List<Vehicle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllVehicles(vehicles: List<Vehicle>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle)

    @Query("SELECT * FROM vehicle_photos WHERE photo_owner_vehicle_id = :vehicleId")
    fun getVehiclePhotos(vehicleId: Int): Flow<List<VehiclePhoto>>

    @Query("SELECT * FROM vehicle_photos WHERE photo_owner_vehicle_id = :vehicleId AND primary_photo = 0")
    fun getVehiclePhotosWithoutMain(vehicleId: Int): Flow<List<VehiclePhoto>>

    @Query("SELECT * FROM vehicle_photos WHERE photo_owner_vehicle_id = :vehicleId AND primary_photo = 1")
    fun getVehicleMainPhoto(vehicleId: Int): Flow<VehiclePhoto>

    @Delete
    suspend fun deleteVehicle(vehicle: Vehicle): Int
    @Delete
    suspend fun deleteVehiclePhoto(vehiclePhoto: VehiclePhoto): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehiclePhotos(photos: List<VehiclePhoto>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehiclePhoto(vararg photo: VehiclePhoto)

    @Query("SELECT * FROM vehicle_addresses WHERE owner_vehicle_id = :vehicleId")
    fun getVehicleAddress(vehicleId: Int): Flow<VehicleAddress>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicleAddress(vararg address: VehicleAddress)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllVehicleAddresses(addresses: List<VehicleAddress> )

    @Query("SELECT * FROM modifications WHERE mod_vehicle_id = :vehicleId ORDER BY mod_type_id ASC")
    fun getVehicleModifications(vehicleId: Int): Flow<List<Modification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicleModifications(mods: List<Modification>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicleModification(vararg mod: Modification)

    @Delete
    suspend fun deleteVehicleModification(vararg mod: Modification)

    @Query("SELECT * FROM vehicles WHERE vehicle_id = :vehicleId")
    fun getVehicleById(vehicleId: Int): Flow<Vehicle>

    @Query("SELECT * FROM vehicles WHERE vehicle_user_id = :userId")
    fun getVehiclesByUserId(userId: Int): Flow<List<Vehicle>>
}