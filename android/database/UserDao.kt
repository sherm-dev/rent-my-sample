package com.shermwebdev.rentmy.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.shermwebdev.rentmy.data.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM app_users WHERE app_user_id = :userId")
    fun getUserById(userId: Int): Flow<User?>

    @Query("SELECT * FROM vehicles WHERE vehicle_user_id = :userId")
    fun getUserVehicles(userId: Int): LiveData<List<Vehicle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(vararg user: User)

    @Query("SELECT * FROM user_photos WHERE user_id = :userId")
    fun getUserPhotoById(userId: Int): Flow<UserPhoto?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserPhoto(vararg userPhoto: UserPhoto)

    @Delete
    fun deleteUserPhoto(vararg userPhoto: UserPhoto)

    @Query("SELECT * FROM user_addresses WHERE user_address_id = :userId")
    fun getUserAddress(userId: Int): Flow<UserAddress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserAddress(vararg addresses: UserAddress)

    @Query("SELECT * FROM licenses WHERE user_id = :userId")
    fun getUserLicense(userId: Int): LiveData<License>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserLicense(vararg license: License)

    @Query("SELECT * FROM license_photos WHERE license_owner_id = :licenseId")
    fun getUserLicensePhoto(licenseId: Int): LiveData<LicensePhoto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserLicensePhoto(vararg photo: LicensePhoto)
}