package com.shermwebdev.rentmy.repository

import android.content.Context
import com.shermwebdev.data.Modification
import com.shermwebdev.rentmy.data.*
import com.shermwebdev.rentmy.database.CacheDatabase
import com.shermwebdev.rentmy.database.getDatabase
import com.shermwebdev.rentmy.datastore.DataStoreManager
import com.shermwebdev.rentmy.network.AppAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VehicleRepository(val context: Context) {
    val database: CacheDatabase = getDatabase(context)
    val dataStoreManager = DataStoreManager(context)

    suspend fun refreshVehicleAddressesByLocation(coordinateWindow: CoordinateWindow){
        withContext(Dispatchers.IO){
            AppAPI.retrofitService(context).getVehicleAddressesByLocation(coordinateWindow)?.let{
                database.vehicleDao().insertAllVehicleAddresses(it)
            }
        }
    }

    suspend fun refreshVehiclesByLocation(coordinateWindow: CoordinateWindow){
        withContext(Dispatchers.IO){
            AppAPI.retrofitService(context).getVehiclesByLocation(coordinateWindow)?.let{
                database.vehicleDao().insertAllVehicles(it)
            }
        }
    }

    suspend fun refreshVehicleById(vehicleId: Int){
        withContext(Dispatchers.IO){
            AppAPI.retrofitService(context).getVehicle(vehicleId)?.let{
                database.vehicleDao().insertVehicle(it)
            }
        }
    }

    suspend fun refreshUserVehicles(userId: Int){
        withContext(Dispatchers.IO){
            AppAPI.retrofitService(context).getUserVehicles(userId).let{
                if(it.isNotEmpty())
                    database.vehicleDao().insertAllVehicles(it)
            }
        }
    }

    suspend fun refreshVehicleAddress(vehicleId: Int){
        withContext(Dispatchers.IO){
            //TODO: Implement
        }
    }

    suspend fun refreshVehiclePhotos(vehicleId: Int){
        withContext(Dispatchers.IO){
            AppAPI.retrofitService(context).getVehiclePhotos(vehicleId).let{
                database.vehicleDao().insertVehiclePhotos(it!!)
            }
        }
    }

    suspend fun refreshVehicleMainPhoto(vehicleId: Int){
        withContext(Dispatchers.IO){
            AppAPI.retrofitService(context).getVehicleMainPhoto(vehicleId)?.let{
                database.vehicleDao().insertVehiclePhoto(it)
            }
        }
    }

    suspend fun refreshDrivetrainOptions(){
        withContext(Dispatchers.IO){
            AppAPI.retrofitService(context).getVehicleOptionsDrivetrainOptions().let{
                database.vehicleOptionDao().insertDrivetrainOptions(it)
            }
        }
    }

    suspend fun refreshEngineOptions(){
        withContext(Dispatchers.IO){
            AppAPI.retrofitService(context).getVehicleOptionsEngineOptions().let{
                database.vehicleOptionDao().insertEngineOptions(it)
            }
        }
    }

    suspend fun refreshInductionOptions(){
        withContext(Dispatchers.IO){
            AppAPI.retrofitService(context).getVehicleOptionsInductionOptions().let{
                database.vehicleOptionDao().insertInductionOptions(it)
            }
        }
    }

    suspend fun refreshModificationOptions(){
        withContext(Dispatchers.IO){
            AppAPI.retrofitService(context).getVehicleOptionsModificationOptions().let{
                database.vehicleOptionDao().insertModificationOptions(it)
            }
        }
    }

    suspend fun refreshPowertrainConfigOptions(){
        withContext(Dispatchers.IO){
            AppAPI.retrofitService(context).getVehicleOptionsPowertrainConfigOptions().let{
                database.vehicleOptionDao().insertPowertrainConfigOptions(it)
            }
        }
    }

    suspend fun refreshTransmissionOptions() = withContext(Dispatchers.IO){

            AppAPI.retrofitService(context).getVehicleOptionsTransmissionOptions().let{
                database.vehicleOptionDao().insertTransmissionOptions(it)
            }

    }

    suspend fun refreshVehicleModifications(vehicleId: Int) = withContext(Dispatchers.IO){
        withContext(Dispatchers.IO){
            AppAPI.retrofitService(context).getVehicleModifications(vehicleId).let{
                database.vehicleDao().insertVehicleModifications(it)
            }
        }
    }

    suspend fun refreshVehicleMakes() = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).getMakes().let{
            database.vehicleMakeModelDao().insertVehicleMakes(it)
        }
    }

    suspend fun refreshVehicleOptions(vehicleId: Int) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).getVehicleOptions(vehicleId)?.let{
            database.vehicleOptionDao().insertVehicleOptions(it)
        }
    }
    suspend fun refreshVehicleModels(makeId: Int) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).getModels(makeId).let{
            database.vehicleMakeModelDao().insertVehicleModels(it)
        }
    }

    suspend fun refreshAllVehicleModels() = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).getAllModels().let{
            database.vehicleMakeModelDao().insertVehicleModels(it)
        }
    }

    suspend fun addVehicleAddress(vehicleId: Int, vehicleAddress: VehicleAddress) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).addVehicleLocation(vehicleId, vehicleAddress)?.let{
            database.vehicleDao().insertVehicleAddress(it)
        }
    }

    suspend fun updateVehicleAddress(vehicleId: Int, vehicleAddress: VehicleAddress) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).updateVehicleLocation(vehicleId, vehicleAddress)?.let{
            database.vehicleDao().insertVehicleAddress(it)
        }
    }

    suspend fun addVehicleModifications(vehicleId: Int, modifications: List<Modification>) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).addVehicleModifications(vehicleId, modifications).let{
            database.vehicleDao().insertVehicleModifications(it)
        }
    }

    suspend fun deleteVehicleModification(modification: Modification) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).deleteVehicleModification(modification.modVehicleId, modification.modId).let{
            if(it)
                database.vehicleDao().deleteVehicleModification(modification)
        }
    }

    suspend fun addVehicleOptions(vehicleOptions: VehicleOptions) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).addVehicleOptions(vehicleOptions.vehicleOptionsVehicleId, vehicleOptions)?.let{
            database.vehicleOptionDao().insertVehicleOptions(it)
        }
    }

    suspend fun updateVehicleOptions(vehicleOptions: VehicleOptions) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).updateVehicleOptions(vehicleOptions.vehicleOptionsVehicleId, vehicleOptions)?.let{
            database.vehicleOptionDao().insertVehicleOptions(it)
        }
    }

    suspend fun addVehicle(vehicle: Vehicle) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).addUserVehicle(vehicle.userId, vehicle)?.let{
            dataStoreManager.updateVehicleIdPref(it.vehicleId)
            database.vehicleDao().insertVehicle(it)
        }
    }

    suspend fun updateVehicle(vehicle: Vehicle) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).updateUserVehicle(vehicle.userId, vehicle)?.let{
            database.vehicleDao().insertVehicle(it)
        }
    }

    suspend fun deleteVehicle(vehicle: Vehicle) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).deleteVehicle(vehicle.vehicleId).let{
            if(it)
                database.vehicleDao().deleteVehicle(vehicle)
        }
    }

    suspend fun deleteVehiclePhoto(vehicleId: Int, vehiclePhoto: VehiclePhoto) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).deleteVehiclePhoto(vehicleId, vehiclePhoto.vehiclePhotoId).let{
            if(it)
                database.vehicleDao().deleteVehiclePhoto(vehiclePhoto)
        }
    }

    suspend fun addVehiclePhotos(vehicleId: Int, photoReqs: List<VehiclePhotoRequest>) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context)
            .addVehiclePhotos(
                vehicleId,
                photoReqs
            ).let{
                database.vehicleDao().insertVehiclePhotos(it)
            }
    }

    suspend fun updateVehiclePhotos(vehicleId: Int, photos: List<VehiclePhoto>) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).updateVehiclePhotos(vehicleId, photos).let{
            database.vehicleDao().insertVehiclePhotos(it)
        }
    }

    suspend fun addVehiclePhoto(vehicleId: Int, byteData: String, primary: Boolean) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context)
            .addVehiclePhoto(
                vehicleId,
                VehiclePhotoRequest(
                    vehiclePhotoId = -1,
                    vehicleId = vehicleId,
                    data = byteData,
                    primary = primary,
                    name = "vehicle_photo_${vehicleId}.jpg"
                )
            )?.let{
                database.vehicleDao().insertVehiclePhoto(it)
            }
    }
}